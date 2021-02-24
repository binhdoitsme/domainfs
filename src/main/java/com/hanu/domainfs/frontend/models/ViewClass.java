package com.hanu.domainfs.frontend.models;

import java.util.LinkedList;
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
    private SourceSegment renderMethod;

    public AbstractViewClass(String name, SourceSegment stateInitializer,
                             List<SourceSegment> methods) {
        this.name = name;
        this.methods = new LinkedList<>(methods);
        this.stateInitializer = stateInitializer;
        this.renderMethod = new RenderMethod(this);
    }

    protected SourceSegment getRenderMethod() {
        return renderMethod;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<SourceSegment> getMethods() {
        final SourceSegment renderMethod = getRenderMethod();
        if (!methods.contains(renderMethod) && renderMethod != null) {
            methods.add(renderMethod);
        }
        return methods;
    }

    @Override
    public SourceSegment getStateInitializer() {
        return stateInitializer;
    }
}
