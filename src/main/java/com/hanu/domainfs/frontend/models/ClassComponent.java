package com.hanu.domainfs.frontend.models;

import java.util.List;

interface ClassComponent extends Source {

    /**
     * Name of this class component.
     */
    String getName();

    /**
     * Get import statements defined in this.
     */
    List<SourceSegment> getImportStatements();

    /**
     * Get custom methods/handlers defined in this.
     */
    List<SourceSegment> getMethods();

    @Override
    default ImplementationStrategy getImplementationStrategy() {
        return new Implementation();
    }

    static class Implementation implements ImplementationStrategy {

        @Override
        public String implement(SourceSegment src) {
            ClassComponent classComponent = (ClassComponent) src;
            StringBuilder result = new StringBuilder();
            // implement imports
            for (SourceSegment importStatement : classComponent.getImportStatements()) {
                result.append(importStatement.toSourceCode()).append("\n");
            }
            result.append("\n");

            // export default
            result.append("export default class " + classComponent.getName() + " {\n");

            // implement methods
            for (SourceSegment method : classComponent.getMethods()) {
                result.append(IndentationUtils.indentStringBy(1, method.toSourceCode()));
            }
            result.append("};");

            return result.toString();
        }

    }
}