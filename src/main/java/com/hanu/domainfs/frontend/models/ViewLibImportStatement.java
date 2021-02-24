package com.hanu.domainfs.frontend.models;

class ViewLibImportStatement implements SourceSegment, ImplementationStrategy {
    private final String path;

    public ViewLibImportStatement(String path) {
        this.path = path;
    }

    @Override
    public String implement(SourceSegment src) {
        return "import ViewLib from \"" + path + "\";";
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }
}
