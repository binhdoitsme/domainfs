package com.hanu.domainfs.ws.generators.utils;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

public final class AnnotationUtils {
    /**
     * Build javassist attribute from annotations.
     * @param constpool
     * @param annotations
     */
    public static AnnotationsAttribute attrFromAnnotations(
            ConstPool constpool, Annotation... annotations) {
        AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        if (annotations == null) return attr;
        
        for (Annotation annotation : annotations) {
            attr.addAnnotation(annotation);
        }

        return attr;
    }
}
