package com.hanu.domainfs.frontend.models;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StateInitializer implements SourceSegment {

    private Map<String, Object> initialStates;

    public StateInitializer(Map<String, Object> initialStates) {
        this.initialStates = initialStates;
    }

    @Override
    public String toSourceCode() {
        return getImplementationStrategy().implement(this);
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return new Implementation();
    }

    private class Implementation implements ImplementationStrategy {
        private static final String TEMPLATE = "this.state = {\n%s\n};";

        @Override
        public String implement(SourceSegment src) {
            final String states = initialStates.entrySet().stream()
                    .map(entry -> "  " + toStateString(entry.getKey(), entry.getValue()))
                    .reduce((s1, s2) -> s1 + ",\n" + s2).orElse("");
            return String.format(TEMPLATE, states);
        }

    }

    static final String toStateString(String key, Object value) {
        String valueStr;
        if (value instanceof Byte || value instanceof Short 
            || value instanceof Integer || value instanceof Long
            || value instanceof Float || value instanceof Double 
            || value == null || value.equals("undefined")) {
            valueStr = value == null ? "null" : value.toString();
        } else if (value instanceof String || value instanceof Character) {
            valueStr = "\"" + value.toString() + "\"";
        } else {
            // to JSON here
            try {
                valueStr = new ObjectMapper().writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return key + ": " + valueStr;
    }
}
