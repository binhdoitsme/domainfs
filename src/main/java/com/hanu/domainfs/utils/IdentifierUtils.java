package com.hanu.domainfs.utils;

import java.lang.reflect.Field;

import domainapp.basics.model.meta.DAttr;

public final class IdentifierUtils {
    public static Field getIdField(Class<?> clazz) {
        for (Field f : clazz.getDeclaredFields()) {
            DAttr attrInfo = f.getAnnotation(DAttr.class);
            if (attrInfo == null) continue;
            if (attrInfo.id()) return f; // because only 1 ID field
        }
        return null; // no ID field
    }
}
