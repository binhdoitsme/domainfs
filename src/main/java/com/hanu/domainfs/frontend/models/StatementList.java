package com.hanu.domainfs.frontend.models;

import java.util.LinkedList;
import java.util.List;

class StatementList implements SourceSegment, ImplementationStrategy {
    private List<SourceSegment> statementList;

    public StatementList(SourceSegment... statements) {
        this.statementList = new LinkedList<>();
        for (SourceSegment statement : statements) {
            this.statementList.add(statement);
        }
    }

    @Override
    public String implement(SourceSegment src) {
        final StatementList source = (StatementList) src;
        StringBuilder result = new StringBuilder();
        for (SourceSegment statement : source.statementList) {
            result.append(statement.toSourceCode()).append("\n");
        }
        return result.toString();
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }
    
}
