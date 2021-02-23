package com.hanu.domainfs.frontend.models;

import java.util.List;

/**
 * Represent a View (a page) in frontend application.
 */
public interface ViewClass extends ClassComponent {

    /**
     * Get the initializer for the state of this.
     */
    SourceSegment getStateInitializer();

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

abstract class AbstractViewClass extends SourceImpl implements ViewClass {
    private String name;
    private List<SourceSegment> methods;
    private SourceSegment stateInitializer;

    public AbstractViewClass(String name, SourceSegment stateInitializer,
                             List<SourceSegment> methods) {
        this.name = name;
        this.methods = methods;
        this.stateInitializer = stateInitializer;
    }

    protected abstract SourceSegment getRenderMethod();

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<SourceSegment> getMethods() {
        final SourceSegment renderMethod = getRenderMethod();
        if (!methods.contains(renderMethod)) {
            methods.add(renderMethod);
        }
        return methods;
    }

    @Override
    public SourceSegment getStateInitializer() {
        return stateInitializer;
    }
}
