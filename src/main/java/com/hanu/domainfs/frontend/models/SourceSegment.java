package com.hanu.domainfs.frontend.models;

public interface SourceSegment {
    /**
     * Transform and persist this to source code form.
     */
    default String toSourceCode() {
        return getImplementationStrategy().implement(this);
    }

    /**
     * Get the implementation strategy used to implement <code>this</code>.
     */
    ImplementationStrategy getImplementationStrategy();

    /** Not yet supported */
    default void fromSource() { throw new UnsupportedOperationException(); }
}
