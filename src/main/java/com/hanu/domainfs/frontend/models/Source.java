package com.hanu.domainfs.frontend.models;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Represent the binding from Java classes to frontend source code.
 */
public interface Source extends SourceSegment {

    /**
     * Get the target file for implementing <code>this</code>.
     */
    String getTarget();

    /**
     * Set the target file for implementing <code>this</code>.
     * @param target
     */
    default void setTarget(String target) { throw new UnsupportedOperationException(); }
}

abstract class SourceImpl implements Source {
    private BufferedWriter writer;

    @Override
    public String toSourceCode() {
        String src = getImplementationStrategy().implement(this);
        if (getTarget() != null && writer == null) try {
            writer = new BufferedWriter(new FileWriter(getTarget()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        if (writer != null) try {
            writer.write(src);
            writer.flush();
            System.out.println("wrote to " + getTarget());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return src;
    }
}