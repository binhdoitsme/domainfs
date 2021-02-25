package com.hanu.domainfs.frontend.models;

/**
 * Unsafe source segment. Use this at your own risk.
 */
public class StringBasedSourceSegment implements SourceSegment {
    private final String source;

    public StringBasedSourceSegment(String source) {
        this.source = source;
    }

    @Override
    public String toSourceCode() {
        return source;
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return null;
    }
    
}
