package com.example.flink.filter;

import com.example.flink.model.EnrichedEvent;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Filter function that uses AviatorScript to evaluate event filtering conditions.
 * Implements requirements 3.1 and 3.2.
 */
public class EventFilterFunction extends RichFilterFunction<EnrichedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(EventFilterFunction.class);
    private static final long serialVersionUID = 1L;

    private final String filterScript;
    private transient Expression compiledExpression;

    /**
     * Creates a new EventFilterFunction with the specified filter script.
     *
     * @param filterScript The AviatorScript filter expression to evaluate
     */
    public EventFilterFunction(String filterScript) {
        this.filterScript = filterScript;
    }

    /**
     * Initializes the AviatorEvaluator and compiles the filter script.
     * This method is called once when the function is initialized.
     *
     * @param parameters Configuration parameters
     * @throws Exception if initialization fails
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        
        try {
            // Initialize AviatorScript engine and compile the filter script
            logger.info("Initializing EventFilterFunction with script: {}", filterScript);
            compiledExpression = AviatorEvaluator.compile(filterScript, true);
            logger.info("Filter script compiled successfully");
        } catch (Exception e) {
            logger.error("Failed to compile filter script: {}", filterScript, e);
            throw new RuntimeException("Failed to compile filter script", e);
        }
    }

    /**
     * Evaluates the filter condition for the given enriched event.
     * Returns true if the event should pass through, false if it should be filtered out.
     *
     * @param event The enriched event to evaluate
     * @return true if the event passes the filter, false otherwise
     * @throws Exception if evaluation fails
     */
    @Override
    public boolean filter(EnrichedEvent event) throws Exception {
        try {
            // Prepare the script execution environment with event data
            Map<String, Object> env = new HashMap<>();
            env.put("event", event.getEvent());
            env.put("user", event.getUserProfile());
            env.put("visit", event.getCurrentVisit());
            env.put("history", event.getEventHistory());

            // Execute the compiled script and get the result
            Object result = compiledExpression.execute(env);

            // Convert result to boolean (handle null as false)
            boolean passes = result != null && (Boolean) result;

            if (logger.isDebugEnabled()) {
                logger.debug("Filter evaluation for event {}: {}", 
                    event.getEvent() != null ? event.getEvent().getEventId() : "null", 
                    passes);
            }

            return passes;
        } catch (Exception e) {
            // Log error and reject the event (fail-safe behavior)
            logger.error("Filter script execution failed for event: {}", 
                event.getEvent() != null ? event.getEvent().getEventId() : "null", e);
            // Reject event on error (requirement 3.6)
            return false;
        }
    }

    /**
     * Gets the filter script used by this function.
     *
     * @return The filter script
     */
    public String getFilterScript() {
        return filterScript;
    }
}
