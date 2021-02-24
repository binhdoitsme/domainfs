package com.hanu.domainfs.frontend.models;

class RenderMethod extends InstanceMethod {
    private ViewClass view;
    
    public RenderMethod(ViewClass view) {
        super("render", null);
        this.view = view;
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }

    @Override
    public SourceSegment getBody() {
        return new ReturnStatement(this.view.getLayout());
    }
}
