package com.hanu.domainfs.frontend.models;

class FieldSetter implements SourceSegment, ImplementationStrategy {
    private FieldReference field;
    private ObjectRep newValue;

    public FieldSetter(FieldReference field, ObjectRep newValue) {
        this.field = field;
        this.newValue = newValue;
    }

    @Override
    public String implement(SourceSegment src) {
        FieldSetter fieldSetter = (FieldSetter) src;
        return fieldSetter.field.toSourceCode() + " = " 
            + fieldSetter.newValue.toSourceCode() + ";";
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }
    
}
