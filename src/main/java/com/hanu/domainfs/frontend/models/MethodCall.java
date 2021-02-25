package com.hanu.domainfs.frontend.models;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class MethodCall implements SourceSegment, ImplementationStrategy {
    private String methodName;
    private String callee;
    private List<ObjectRep> arguments;

    public MethodCall(String methodName, String callee, Object... arguments) {
        this.methodName = methodName;
        this.callee = callee;
        this.arguments = argumentsToRep(arguments);
    }

    @Override
    public String implement(SourceSegment src) {
        MethodCall methodCall = (MethodCall) src;
        final StringBuilder result = new StringBuilder();
        result.append(methodCall.callee).append(".")
            .append(methodCall.methodName)
            .append("(")
            .append(String.join(", ", methodCall.arguments.stream()
                .map(arg -> arg.toSourceCode()).collect(Collectors.toList())))
            .append(");");
        return result.toString();
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }
    
    private static List<ObjectRep> argumentsToRep(Object[] arguments) {
        List<ObjectRep> list = new LinkedList<>();
        for (Object arg : arguments) {
            list.add(new ObjectRep(arg));
        }
        return list;
    }
}

final class SelfMethodCall extends MethodCall {

    public SelfMethodCall(String methodName, Object... arguments) {
        super(methodName, "this", arguments);
    }
    
}
