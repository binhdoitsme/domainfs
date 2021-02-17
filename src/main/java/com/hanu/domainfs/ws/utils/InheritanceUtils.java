package com.hanu.domainfs.ws.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Utilities related to inheritance (subtype-mapping).
 */
public final class InheritanceUtils {
    private static final Map<String, Map<String, String>> cachedSubtypes;

    static {
        cachedSubtypes = new HashMap<>();
    }

    /**
     * Returns the subtype map of {@link#supertype}.
     * @param supertype
     */
    public static Map<String, String> getSubtypeMapFor(Class<?> supertype) {
        String supertypeName = supertype.getName();

        if (cachedSubtypes.containsKey(supertypeName)) {
            return cachedSubtypes.get(supertypeName);
        }

        String classPkg = supertype.getPackageName();
        String basePkg = Stream.of(supertype.getClassLoader().getDefinedPackages())
                                .map(p -> p.getName())
                                .filter(p -> classPkg.contains(p))
                                .findAny().orElse(null);

        List<Class<?>> classes;
        try {
            classes = ClassUtils.getClasses(basePkg);
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> subtypes = new HashMap<>();

        for (Class<?> cls : classes) {
            if (supertype.isAssignableFrom(cls) && cls != supertype) {
                subtypes.put(cls.getSimpleName().toLowerCase(), cls.getName());
            }
        }

        cachedSubtypes.put(supertypeName, subtypes);

        return subtypes;
    }
}
