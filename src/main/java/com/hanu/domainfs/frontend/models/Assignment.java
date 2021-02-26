package com.hanu.domainfs.frontend.models;

class Assignment implements SourceSegment, ImplementationStrategy {
    private SourceSegment left;
    private SourceSegment right;

    public Assignment(SourceSegment left, SourceSegment right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String implement(SourceSegment src) {
        Assignment assignment = (Assignment) src;
        return assignment.left.toSourceCode() + " = " + assignment.right.toSourceCode();
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }
    
}
