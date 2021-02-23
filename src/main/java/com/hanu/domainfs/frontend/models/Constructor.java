package com.hanu.domainfs.frontend.models;

public class Constructor implements SourceSegment, ImplementationStrategy {

    private SourceSegment stateInitializer;

    public Constructor(SourceSegment stateInitializer) {
        this.stateInitializer = stateInitializer;
    }

    @Override
    public String implement(SourceSegment src) {
        StringBuilder result = new StringBuilder();
        result.append("constructor (props) {").append("\n");
        result.append(IndentationUtils.indentStringBy(1, stateInitializer.toSourceCode()));
        result.append("}");
        return result.toString();
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }
    
}
