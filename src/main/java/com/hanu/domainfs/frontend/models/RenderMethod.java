package com.hanu.domainfs.frontend.models;

import static com.hanu.domainfs.frontend.models.IndentationUtils.*;

class RenderMethod implements SourceSegment, ImplementationStrategy {
    private ViewClass view;
    
    public RenderMethod(ViewClass view) {
        this.view = view;
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }
    
    @Override
    public String implement(SourceSegment src) {
        RenderMethod renderMethod = (RenderMethod) src;
        StringBuilder result = new StringBuilder();
        result.append("render() {").append("\n").append("\s\s").append("return (\n");
        result.append(indentStringBy(2, renderMethod.view.toSourceCode()));
        result.append("\s\s);").append("\n}");
        return result.toString();
    }
}
