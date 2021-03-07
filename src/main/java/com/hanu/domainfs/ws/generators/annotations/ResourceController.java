package com.hanu.domainfs.ws.generators.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a type to be a controller of a resource type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ResourceController {
    /**
     * Name of the resource controlled by the annotated class.
     * Must be in human-readable, pluralized form.
     * If no name is found, the name will be inferred from the class name.
     */
    String name() default "";
}
