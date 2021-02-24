package com.hanu.domainfs.frontend.models;

abstract class InstanceMethod implements SourceSegment, ImplementationStrategy {
    private final String name;
    private final String[] paramNames;

    public InstanceMethod(String name, String[] paramNames) {
        this.name = name;
        this.paramNames = paramNames;
    }

    public String getName() {
        return name;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public abstract SourceSegment getBody();

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }

    @Override
    public String implement(SourceSegment src) {
        StringBuilder sb = new StringBuilder();
        sb.append(name)
            .append("(")
                .append(getParamNames() == null ? 
                        "" : String.join(", ", getParamNames()))
                .append(")")
            .append(" {").append("\n")
            .append(IndentationUtils.indentStringBy(1, getBody().toSourceCode()))
            .append("}");
        return sb.toString();
    }
}
