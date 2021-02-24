package com.hanu.domainfs.frontend.models;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class MethodCall implements SourceSegment, ImplementationStrategy {
    private String methodName;
    private List<String> arguments;

    public MethodCall(String methodName, Object... arguments) {
        this.methodName = methodName;
        this.arguments = argumentsToString(arguments);
    }

    @Override
    public String implement(SourceSegment src) {
        MethodCall methodCall = (MethodCall) src;
        final StringBuilder result = new StringBuilder();
        result.append(methodCall.methodName)
            .append("(")
            .append(String.join(", ", methodCall.arguments))
            .append(");");
        return result.toString();
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }
    
    private static List<String> argumentsToString(Object[] arguments) {
        List<String> list = new LinkedList<>();
        for (Object arg : arguments) {
            if (arg == null) {
                list.add("null");
            } else if (arg instanceof String || arg instanceof Byte 
                    || arg instanceof Short || arg instanceof Boolean
                    || arg instanceof Integer || arg instanceof Long
                    || arg instanceof Float || arg instanceof Double) {
                list.add(arg.toString());
            } else {
                try {
                    list.add(new ObjectMapper().writeValueAsString(arg));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return list;
    }
}
