package com.hanu.domainfs.frontend.models;

import java.util.List;

/**
 * Represent a View (a page) in frontend application.
 */
public interface View extends Source, ViewLayout {

    /**
     * Get the initializer for the state of this.
     */
    SourceSegment getStateInitializer();

    /**
     * Get custom methods/handlers defined in this.
     */
    List<SourceSegment> getMethods();

    /**
     * 
     * @param layout
     */
    void setLayout(ViewLayout layout);

    /**
     * 
     */
    ViewLayout getLayout();

    /**
     * 
     * @param component
     */
    default void add(ViewComponent component) {
        getLayout().add(component);
    }
}
