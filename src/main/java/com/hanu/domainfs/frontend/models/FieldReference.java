package com.hanu.domainfs.frontend.models;

class FieldReference implements SourceSegment, ImplementationStrategy {
    private String fieldName;
    private String callSite;

    public FieldReference(String fieldName, String callSite) {
        this.fieldName = fieldName;
        this.callSite = callSite;
    }

    @Override
    public String implement(SourceSegment src) {
        FieldReference ref = (FieldReference) src;
        return ref.callSite + "." + ref.fieldName;
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }
    
}
