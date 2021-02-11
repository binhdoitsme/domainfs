package com.hanu.domainfs.ws.generators.utils;

import java.util.concurrent.atomic.AtomicInteger;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.SignatureAttribute;

@SuppressWarnings("rawtypes")
public final class GenericTypeUtils {
    public static String genericFieldSignatureFrom(Class outer, Class... inners) {
        try {
            String signature = getGenericSignatureFrom(true, outer, inners);
            return SignatureAttribute.toFieldSignature(signature).encode();
        } catch (BadBytecode ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String genericTypeSignatureFrom(Class outer, Class... inners) {
        try {
            String signature = getGenericSignatureFrom(false, outer, inners);
            return SignatureAttribute.toClassSignature(signature).encode();
        } catch (BadBytecode ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getGenericSignatureFrom(boolean hasOuterFirst, Class outer, Class... inners) {
        // <T:Ljava/lang/Object;ID:Ljava/lang/Object;>Ljava/lang/Object; class
        // 
        StringBuilder nameBuilder = new StringBuilder();
        if (hasOuterFirst) {
            nameBuilder.append(getClassNameInByteForm(outer));
        }
        if (inners != null) {
            nameBuilder.append("<");
            final AtomicInteger counter = new AtomicInteger();
            for (Class cls : inners) {
                if (!hasOuterFirst) {
                    nameBuilder.append("$")
                        .append(counter.incrementAndGet())
                        .append(":");
                }
                nameBuilder.append(getClassNameInByteForm(cls)).append(";");
            }
            nameBuilder.append(">");
        }
        if (!hasOuterFirst) {
            nameBuilder.append(getClassNameInByteForm(outer));
        }
        nameBuilder.append(";");
        return nameBuilder.toString();
    }

    private static String getClassNameInByteForm(Class cls) {
        return "L" + getWrapperClass(cls).getName().replace(".", "/");
    }

    public static Class getWrapperClass(Class clazz) {
        if (!clazz.isPrimitive())
            return clazz;

        if (clazz == Integer.TYPE)
            return Integer.class;
        if (clazz == Long.TYPE)
            return Long.class;
        if (clazz == Boolean.TYPE)
            return Boolean.class;
        if (clazz == Byte.TYPE)
            return Byte.class;
        if (clazz == Character.TYPE)
            return Character.class;
        if (clazz == Float.TYPE)
            return Float.class;
        if (clazz == Double.TYPE)
            return Double.class;
        if (clazz == Short.TYPE)
            return Short.class;
        if (clazz == Void.TYPE)
            return Void.class;

        return clazz;
    }
}
