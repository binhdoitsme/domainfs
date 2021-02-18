package com.hanu.domainfs.ws.utils;

import static java.lang.Character.toLowerCase;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class NamingUtils {
    public static String fieldNameFrom(String typeName, String suffix) {
        return toLowerCase(typeName.charAt(0)) + typeName.substring(1) + suffix;
    }

    public static String classNameFrom(String pkg, Class<?> superClass, 
                                       String suffix, Class<?>... classes) {
        List<String> classNames = Stream.of(classes)
            .map(c -> c.getSimpleName()).collect(Collectors.toList());
        return String.format("%s.%s$%s%s",
                pkg,
                superClass.getSimpleName(),
                String.join("", classNames),
                suffix);
    }
}
