package com.hanu.domainfs.frontend.models;

public class ImportStatement implements SourceSegment, ImplementationStrategy {
    private final String path;
    private final String defaultAlias;
    private final String[] components;

    public ImportStatement(String path, Object defaultAlias, String... components) {
        this.path = path;
        this.defaultAlias = (String)
        defaultAlias;
        this.components = components;
    }

    public ImportStatement(String path, String... components) {
        this.path = path;
        this.defaultAlias = "";
        this.components = components;
    }

    @Override
    public String implement(SourceSegment src) {
        final StringBuilder result = new StringBuilder("import ");
        if (!defaultAlias.isEmpty()) {
            result.append(defaultAlias).append(", ");
        }
        result.append("{ ")
            .append(String.join(", ", components))
            .append(" }");
        result.append(" from ").append("\"").append(path).append("\"").append(";");
        return result.toString();
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return this;
    }
}
