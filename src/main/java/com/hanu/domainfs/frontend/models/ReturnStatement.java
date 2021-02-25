package com.hanu.domainfs.frontend.models;

public class ReturnStatement implements SourceSegment, ImplementationStrategy {
    private SourceSegment expr;

    public ReturnStatement(SourceSegment expression) {
        this.expr = expression;
    }

    @Override
    public String implement(SourceSegment src) {
        ReturnStatement source = (ReturnStatement) src;
        return "return (\n" + 
            IndentationUtils.indentStringBy(1, source.expr.toSourceCode()) + "\n);";
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }


}
