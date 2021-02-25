package com.hanu.domainfs.frontend.models;

import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("unchecked")
public class ObjectRep implements SourceSegment, ImplementationStrategy {
    private Object instance;

    public ObjectRep(Object instance) {
        this.instance = instance;
    }

    @Override
    public String implement(SourceSegment src) {
        return toImplString(((ObjectRep) src).instance);
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }

    private static String toImplString(Object arg) {
        if (arg == null) {
            return "null";
        } else if (arg instanceof String || arg instanceof Byte 
                || arg instanceof Short || arg instanceof Boolean
                || arg instanceof Integer || arg instanceof Long
                || arg instanceof Float || arg instanceof Double) {
            return arg.toString();
        } else if (arg instanceof Map) {
            final Map<String, Object> map = (Map<String, Object>) arg;
            final StringBuilder result = new StringBuilder();
            result.append("{\n");
            final String entryStr = String.join(",\n", 
                    map.entrySet().stream()
                        .map(ObjectRep::mapEntryToImplString)
                        .collect(Collectors.toList()));
            result.append(IndentationUtils.indentStringBy(1, entryStr));
            result.append("}");
            return result.toString();
        } else {
            try {
                return new ObjectMapper().writeValueAsString(arg)
                    .replaceAll("\"(\\w+)\":", "$1:");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String mapEntryToImplString(Map.Entry<String, Object> entry) {
        return entry.getKey() + ": " + toImplString(entry.getValue());
    }
    
}
