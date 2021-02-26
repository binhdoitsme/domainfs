package com.hanu.domainfs.frontend.models;

public class MethodReference implements SourceSegment, ImplementationStrategy {
    private String methodName;
    private String callee;

    public MethodReference(String methodName, String callee) {
        this.methodName = methodName;
        this.callee = callee;
    }

    protected String getCallee() {
        return callee;
    }

    protected String getMethodName() {
        return methodName;
    }

    @Override
    public String implement(SourceSegment src) {
        MethodReference methodRef = (MethodReference) src;
        final StringBuilder result = new StringBuilder();
        result.append(methodRef.callee);
        if (!methodRef.methodName.isEmpty())
            result.append(".").append(methodRef.methodName);
        return result.toString();
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }
}
