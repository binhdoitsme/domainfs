package com.hanu.domainfs.frontend.models;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MethodCall extends MethodReference {
    private List<ObjectRep> arguments;

    public MethodCall(String methodName, String callee, Object... arguments) {
        super(methodName, callee);
        this.arguments = argumentsToRep(arguments);
    }

    public MethodCall(MethodReference ref, String methodName, Object... arguments) {
        this(methodName, ref.getCallee() + "." + ref.getMethodName(), arguments);
    }

    @Override
    public String implement(SourceSegment src) {
        MethodCall methodCall = (MethodCall) src;
        final StringBuilder result = new StringBuilder(super.implement(src));
        result.append("(")
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
