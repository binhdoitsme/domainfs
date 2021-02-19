package com.hanu.domainfs.ws.generators;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.hanu.domainfs.ws.utils.InheritanceUtils;
import com.hanu.domainfs.ws.utils.NamingUtils;
import com.hanu.domainfs.ws.utils.PackageUtils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.description.annotation.AnnotationDescription.Builder.*;

final class AnnotationGenerator {
    private static AnnotationGenerator INSTANCE;

    public static AnnotationGenerator instance() {
        if (INSTANCE == null) {
            INSTANCE = new AnnotationGenerator();
        }
        return INSTANCE;
    }

    private static final ClassReloadingStrategy DEFAULT_RELOADING_STRATEGY;
    private final Map<String, List<String>> ignoredFields;

    static {
        ByteBuddyAgent.install();
        DEFAULT_RELOADING_STRATEGY = ClassReloadingStrategy.fromInstalledAgent();
    }

    private AnnotationGenerator() {
        ignoredFields = new LinkedHashMap<>();
    }

    /**
     * Generate @JsonIgnoreProperties to handle circular dependencies.
     * @param cls
     * @param defined
     */
    void generateCircularAnnotations(Class<?> cls, Class<?>[] defined) {
        ByteBuddyAgent.install();
        Builder<?> builder = new ByteBuddy().rebase(cls);
        final String[] ignoredFields = getIgnoredFields(defined);
        for (Field f : cls.getDeclaredFields()) {
            if (!isDefinedTypeField(f)) continue;
            builder = builder.field(is(f))
                .annotateField(
                    ofType(JsonIgnoreProperties.class)
                    .defineArray("value", ignoredFields)
                    .build());
        }
        builder.make()
            .load(cls.getClassLoader(), DEFAULT_RELOADING_STRATEGY);
    }

    /**
     * Generate @JsonSubTypes, @JsonTypeInfo, @JsonTypeName to handle inheritance.
     * @param cls
     */
    void generateInheritanceAnnotations(Class<?> cls) {
        List<Class<?>> subtypes = InheritanceUtils.getSubtypesOf(cls);
        if (subtypes.isEmpty()) return;
        for (Class<?> subtype : subtypes) {
            new ByteBuddy().decorate(subtype)
                .annotateType(ofType(JsonTypeName.class)
                        .define("value", NamingUtils.subtypeShortNameFrom(subtype))
                        .build())
                .make()
                .load(cls.getClassLoader(), DEFAULT_RELOADING_STRATEGY);
        }
        // decorate super
        List<AnnotationDescription> subtypeAnnotations = 
            subtypes.stream()
                .map(c -> ofType(JsonSubTypes.Type.class)
                    .define("value", c)
                    .define("name", NamingUtils.subtypeShortNameFrom(c))
                    .build())
                .collect(Collectors.toList());
        new ByteBuddy().decorate(cls)
            .annotateType(
                ofType(JsonTypeInfo.class)
                    .define("use", JsonTypeInfo.Id.NAME)
                    .define("include", JsonTypeInfo.As.PROPERTY)
                    .define("property", "type")
                    .build(),
                ofType(JsonSubTypes.class)
                    .defineAnnotationArray("value", 
                        ForLoadedType.of(JsonSubTypes.Type.class), 
                        subtypeAnnotations.toArray(
                            new AnnotationDescription[subtypeAnnotations.size()]))
                    .build())
            .make()
            .load(cls.getClassLoader(), DEFAULT_RELOADING_STRATEGY);
    }

    private String[] getIgnoredFields(Class<?>[] defined) {
        final List<String> result = new ArrayList<>();
        for (Class<?> cls : defined) {
            final String className = cls.getName();
            if (ignoredFields.containsKey(className)) {
                result.addAll(ignoredFields.get(className));
                continue;
            }
            final List<String> definedTypeFields = Stream
                .of(cls.getDeclaredFields())
                .filter(AnnotationGenerator::isDefinedTypeField)
                .map(f -> f.getName())
                .collect(Collectors.toList());
            ignoredFields.put(className, definedTypeFields);
            result.addAll(definedTypeFields);
        }
        return result.toArray(new String[result.size()]);
    }

    private static boolean isDefinedTypeField(Field f) {
        Class<?> type = f.getType();
        Class<?> declaringType = f.getDeclaringClass();
        String rootPackage = PackageUtils.basePackageOf(declaringType);
        return type.getName().contains(rootPackage);
    }
}
