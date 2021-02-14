package com.hanu.domainfs.ws.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;

/**
 * Utilities to extract class association from domain models.
 * Depends on jDomainApp.
 */
public final class ClassAssocUtils {

    private static final Map<String, List<Class<?>>> associations;
    private static final Class<DAssoc> dAssocType = DAssoc.class;

    static {
        associations = new HashMap<>();
    }

    /**
     * Get list of classes associated to {@link#cls}.
     * @param cls
     * @return
     */
    public static List<Class<?>> getAssociations(Class<?> cls) {
        String className = cls.getName();
        if (!associations.containsKey(className)) {
            // get associations
            findAssociations(cls);
        }
        return associations.get(className);  
    }

    /**
     * Check if {@link#cls} has any associated classes.
     * @param cls
     * @return
     */
    public static boolean hasAssociations(Class<?> cls) {
        return !getAssociations(cls).isEmpty();
    }

    private static void findAssociations(Class<?> cls) {
        List<Class<?>> associatedTypes = new LinkedList<>();
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(dAssocType)) {
                DAssoc assoc = field.getAnnotation(dAssocType);
                
                if (assoc.ascType() == AssocType.One2Many
                        && assoc.endType() == AssocEndType.One) {
                    associatedTypes.add(assoc.associate().type());
                }
            }
        }
        associations.put(cls.getName(), associatedTypes);
    }
}
