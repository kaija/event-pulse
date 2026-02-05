package com.example.flink.processor;

import com.example.flink.api.ApiException;
import com.example.flink.api.ProfileApiClient;
import com.example.flink.model.*;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * UserStateManager manages user checkpoints using Flink Keyed State with TTL.
 * 
 * This KeyedProcessFunction:
 * - Maintains user state (checkpoint) in Flink's Keyed State
 * - Automatically expires state after 10 minutes of inactivity using TTL
 * - Fetches user profile from external API when state is missing or expired
 * - Enriches events with user profile, visit, and event history
 * 
 * Requirements: 5.1, 5.4, 6.1, 6.2, 6.3
 */
public class UserStateManager extends KeyedProcessFunction<String, UserEvent, EnrichedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(UserStateManager.class);
    private static final long serialVersionUID = 1L;

    // Flink Keyed State with TTL for user checkpoints
    private transient ValueState<UserCheckpoint> userCheckpointState;
    
    // Profile API client for fetching user data
    private final ProfileApiClient profileApiClient;

    /**
     * Constructor
     * @param profileApiClient Client for fetching user profile data
     */
    public UserStateManager(ProfileApiClient profileApiClient) {
        this.profileApiClient = profileApiClient;
    }

    /**
     * Initialize the Flink state with TTL configuration.
     * 
     * Configures:
     * - TTL: 10 minutes (automatically expires after 10 minutes of inactivity)
     * - Update Type: OnCreateAndWrite (TTL resets on create and update)
     * - State Visibility: NeverReturnExpired (expired state returns null)
     * - Cleanup: Full snapshot cleanup during checkpointing
     * 
     * Requirements: 5.1, 5.4, 6.1, 6.2, 6.3
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        
        // Configure State TTL - 10 minutes after last write
        StateTtlConfig ttlConfig = StateTtlConfig
            .newBuilder(Time.minutes(10))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .cleanupFullSnapshot()  // Clean up expired state during checkpoint
            .build();
        
        // Create ValueState descriptor for UserCheckpoint
        ValueStateDescriptor<UserCheckpoint> descriptor = 
            new ValueStateDescriptor<>("user-checkpoint", UserCheckpoint.class);
        
        // Enable TTL on the state
        descriptor.enableTimeToLive(ttlConfig);
        
        // Get the state from runtime context
        userCheckpointState = getRuntimeContext().getState(descriptor);
        
        logger.info("UserStateManager initialized with 10-minute TTL");
    }

    /**
     * Process each user event.
     * 
     * Flow:
     * 1. Try to get checkpoint from Flink State
     * 2. If checkpoint is null (doesn't exist or expired):
     *    - Call Profile API to fetch user data
     *    - Create new checkpoint and store in state
     * 3. If checkpoint exists:
     *    - Update last activity time
     *    - Store updated checkpoint (this resets TTL)
     * 4. Create enriched event with user data
     * 5. Emit enriched event downstream
     * 
     * Requirements: 2.1, 2.2, 2.3, 2.5, 5.2, 5.3
     */
    @Override
    public void processElement(UserEvent event, Context ctx, Collector<EnrichedEvent> out) 
            throws Exception {
        String userId = event.getUserId();
        
        // Try to get checkpoint from Flink State
        UserCheckpoint checkpoint = userCheckpointState.value();
        
        if (checkpoint == null) {
            // Checkpoint doesn't exist or has expired (TTL)
            // Need to fetch user data from Profile API
            logger.info("Checkpoint not found or expired for userId: {}, fetching from API", userId);
            
            try {
                // Fetch user profile data from external API
                UserProfile profile = profileApiClient.getUserProfile(userId);
                Visit currentVisit = profileApiClient.getCurrentVisit(userId);
                List<EventHistory> history = profileApiClient.getEventHistory(userId);
                
                // Create new checkpoint with fetched data
                checkpoint = new UserCheckpoint(userId, profile, currentVisit, history);
                
                // Store checkpoint in Flink State (starts TTL timer)
                userCheckpointState.update(checkpoint);
                
                logger.info("Created new checkpoint for userId: {}", userId);
            } catch (ApiException e) {
                // API call failed - create empty checkpoint to allow processing to continue
                logger.error("Failed to fetch user profile for userId: {}, using empty checkpoint", userId, e);
                checkpoint = UserCheckpoint.empty(userId);
                
                // Store empty checkpoint in state
                userCheckpointState.update(checkpoint);
            }
        } else {
            // Checkpoint exists - update last activity time and reset TTL
            checkpoint.setLastActivityTime(System.currentTimeMillis());
            
            // Update state (this resets the TTL timer)
            userCheckpointState.update(checkpoint);
            
            logger.debug("Updated checkpoint for userId: {}", userId);
        }
        
        // Create enriched event with user data from checkpoint
        EnrichedEvent enrichedEvent = new EnrichedEvent(
            event,
            checkpoint.getUserProfile(),
            checkpoint.getCurrentVisit(),
            checkpoint.getEventHistory()
        );
        
        // Emit enriched event downstream
        out.collect(enrichedEvent);
    }
}
