package com.hanu.domainfs.ws.generators;

import static net.bytebuddy.description.annotation.AnnotationDescription.Builder.ofType;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.hanu.domainfs.utils.IdentifierUtils;
import com.hanu.domainfs.utils.NamingUtils;
import com.hanu.domainfs.ws.generators.annotations.NestedResourceController;
import com.hanu.domainfs.ws.generators.annotations.bridges.AnnotationRep;
import com.hanu.domainfs.ws.generators.annotations.bridges.RestAnnotationAdapter;
import com.hanu.domainfs.ws.generators.annotations.bridges.TargetType;
import com.hanu.domainfs.ws.generators.controllers.DefaultNestedRestfulController;
import com.hanu.domainfs.ws.generators.controllers.DefaultRestfulController;
import com.hanu.domainfs.ws.generators.controllers.DefaultRestfulWithInheritanceController;
import com.hanu.domainfs.ws.generators.controllers.NestedRestfulController;
import com.hanu.domainfs.ws.generators.controllers.RestfulController;
import com.hanu.domainfs.ws.generators.controllers.RestfulWithInheritanceController;
import com.hanu.domainfs.ws.svcdesc.ServiceController;
import com.hanu.domainfs.ws.utils.GenericTypeUtils;

import org.modeshape.common.text.Inflector;
import org.springframework.stereotype.Component;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Generate web controllers based on domain models and their relationships.
 *
 * @author binh_dh
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
class WebControllerGenerator {

    private static final WebControllerGenerator instance = new WebControllerGenerator();

    public static WebControllerGenerator instance() {
        return instance;
    }

    private static final Class<RestfulController> restCtrlClass = (Class) RestfulController.class;
    private static final Class<RestfulController> restCtrlClassImpl = (Class) DefaultRestfulController.class;
    private static final Class<RestfulController> inheritRestCtrlClass = (Class) RestfulWithInheritanceController.class;
    private static final Class<RestfulController> inheritRestCtrlClassImpl = (Class) DefaultRestfulWithInheritanceController.class;
    private static final Class nestedRestCtrlClass = NestedRestfulController.class;
    private static final Class nestedRestCtrlImplClass = DefaultNestedRestfulController.class;

    private final Map<String, Class<?>> generatedCrudClasses;
    private final RestAnnotationAdapter annotationAdapter;

    private WebControllerGenerator() {
        generatedCrudClasses = new HashMap<>();
        annotationAdapter = RestAnnotationAdapter.adaptTo(TargetType.SPRING);
    }

    WebControllerGenerator(TargetType targetType) {
        generatedCrudClasses = new HashMap<>();
        annotationAdapter = RestAnnotationAdapter.adaptTo(targetType);
    }

    /**
     * Get the RESTful controller from a generic type.
     */
    public <T> Class<RestfulController<T>> getRestfulController(Class<T> type) {
        try {
            String typeName = type.getName();
            if (!generatedCrudClasses.containsKey(typeName)) {
                Class<?> idType = (Class<?>) GenericTypeUtils
                        .getWrapperClass(IdentifierUtils.getIdField(type).getType());
                generatedCrudClasses.put(typeName, generateRestfulController(type, idType));
            }
            return (Class<RestfulController<T>>) generatedCrudClasses.get(typeName);
        } catch (IllegalAccessException | IOException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get a nested RESTful controller from a generic type.
     */
    public <T1, T2> Class<NestedRestfulController<T1, T2>> getNestedRestfulController(Class<T1> outerType,
            Class<T2> innerType) {
        try {
            return generateNestedRestfulController(outerType, innerType);
        } catch (IllegalAccessException | IOException | NoSuchMethodException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Name cannot have more than 1 '$' token
    private <T> Class<RestfulController<T>> generateRestfulController(Class<T> type, Class<?> idType)
            throws IllegalAccessException, IOException, NoSuchMethodException, SecurityException {
        //
        final boolean hasInheritance = Modifier.isAbstract(type.getModifiers());
        final Class<RestfulController> baseImplClass = hasInheritance ? inheritRestCtrlClassImpl : restCtrlClassImpl;
        final Class<RestfulController> baseClass = hasInheritance ? inheritRestCtrlClass : restCtrlClass;
        final Inflector inflector = Inflector.getInstance();
        final String endpoint = "/" + inflector.underscore(inflector.pluralize(type.getSimpleName())).replace("_", "-");
        final String pkg = type.getPackage().getName().replace(".model", "");
        final String name = NamingUtils.classNameFrom(pkg, restCtrlClass, "Controller", type);
        Builder<RestfulController> builder = generateControllerType(baseClass, baseImplClass, name, endpoint, type)
                .annotateType(ofType(ServiceController.class).define("endpoint", endpoint).define("name",
                        "Manage " + inflector.humanize(inflector.pluralize(inflector.underscore(type.getSimpleName()))))
                        .define("className", type.getName()).build());

        builder = adaptAnnotationsOnBuilder(builder, baseClass, name);

        Unloaded<RestfulController> unloaded = builder.make();
        return (Class<RestfulController<T>>) saveAndReturnClass(unloaded, name);
    }

    private static AnnotationDescription from(AnnotationRep annRep) {
        Class<? extends Annotation> annType = (Class) annRep.getAnnotationClass();
        AnnotationDescription.Builder builder = ofType(annType);
        for (Entry<String, Object> entry : annRep.getValues().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Class type = value.getClass();
            if (type.isAssignableFrom(String.class)) {
                builder = builder.define(key, (String) value);
            } else if (type == Boolean.class || type == Boolean.TYPE) {
                builder = builder.define(key, (boolean) value);
            } else if (type == Short.class || type == Short.TYPE) {
                builder = builder.define(key, (boolean) value);
            } else if (type == Long.class || type == Long.TYPE) {
                builder = builder.define(key, (boolean) value);
            } else if (type == Integer.class || type == Integer.TYPE) {
                builder = builder.define(key, (boolean) value);
            } else if (type == Float.class || type == Float.TYPE) {
                builder = builder.define(key, (boolean) value);
            } else if (type == Double.class || type == Double.TYPE) {
                builder = builder.define(key, (boolean) value);
            } else if (type == Byte.class || type == Byte.TYPE) {
                builder = builder.define(key, (boolean) value);
            } else if (type == String[].class) {
                builder = builder.defineArray(key, (String[]) value);
            } else {
                // not working yet
            }
        }
        return builder.build();
    }

    private List<AnnotationRep> adaptAnnotations(Annotation[] annotations, String className) {
        List<AnnotationRep> adaptedAnnotations = new LinkedList<>();
        for (Annotation ann : annotations) {
            List<AnnotationRep> annReps = adaptAnnotation(ann, className);
            if (annReps == null)
                continue;
            adaptedAnnotations.addAll(annReps);
        }
        return adaptedAnnotations;
    }

    private List<AnnotationRep> adaptAnnotation(Annotation ann, String className) {
        String _name = className == null ? "" : className.substring(className.lastIndexOf("$") + 1);
        _name = _name.replace("Controller", "");
        _name = Inflector.getInstance().underscore(_name).replace("_", "-");

        Class<Annotation> annType = (Class) ann.annotationType();
        AnnotationRep annRep = new AnnotationRep(annType);
        for (Method m : annType.getDeclaredMethods()) {
            try {
                annRep.setValueOf(m.getName(), m.invoke(ann));
            } catch (IllegalAccessException | IllegalArgumentException 
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        annRep.setValueOf("declaredOn", className);
        this.annotationAdapter.addSourceAnnotation(annRep);
        return annotationAdapter.getTargetAnnotations(annType);
    }

    private <T> Builder<T> adaptAnnotationsOnBuilder(Builder<T> builder, Class baseClass, String currentName) {
        for (Method m : baseClass.getMethods()) {
            List<AnnotationDescription> adaptedAnnotations = 
                adaptAnnotations(m.getAnnotations(), currentName)
                    .stream()
                    .map(WebControllerGenerator::from)
                    .distinct()
                    .collect(Collectors.toList());

            builder = builder.method(ElementMatchers.definedMethod(ElementMatchers.named(m.getName())))
                            .intercept(MethodCall.invokeSuper().withAllArguments()
                                .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                            .annotateMethod(adaptedAnnotations);

            Parameter[] parameters = m.getParameters();
            final AtomicInteger counter = new AtomicInteger();
            for (Parameter p : parameters) {
                builder = ((MethodDefinition)builder)
                    .annotateParameter(counter.getAndIncrement(),
                        adaptAnnotations(p.getAnnotations(), currentName)
                            .stream().map(WebControllerGenerator::from)
                            .collect(Collectors.toSet()));
            }
        }
        return builder;
    }

    private <T1, T2> Class<NestedRestfulController<T1, T2>>
        generateNestedRestfulController(Class<T1> outerType, Class<T2> innerType)
            throws IllegalAccessException, IOException,
                NoSuchMethodException, SecurityException, NoSuchFieldException {
        //
        final Inflector inflector = Inflector.getInstance();
        final String endpoint =
            "/" + inflector.underscore(inflector.pluralize(outerType.getSimpleName())).replace("_", "-")
                + "/{id}/" + inflector.underscore(inflector.pluralize(innerType.getSimpleName())).replace("_", "-");
        final String pkg = outerType.getPackage().getName().replace(".model", "");
        final String name = NamingUtils.classNameFrom(pkg, nestedRestCtrlClass, "Controller", outerType, innerType);

        final Class<NestedRestfulController> baseClass = nestedRestCtrlClass;
        final Class<NestedRestfulController> baseImplClass = nestedRestCtrlImplClass;
        Builder<NestedRestfulController> builder =
            generateControllerType(baseClass, baseImplClass, name, endpoint,
                outerType, innerType);

        // add annotation
        AnnotationRep ann = new AnnotationRep(NestedResourceController.class);
        ann.setValueOf("innerType", inflector.underscore(inflector.pluralize(innerType.getSimpleName())).replace("_", "-"));
        ann.setValueOf("outerType", inflector.underscore(inflector.pluralize(outerType.getSimpleName())).replace("_", "-"));
        annotationAdapter.addSourceAnnotation(ann);
        builder = builder.annotateType(
            annotationAdapter.getTargetAnnotations(ann.getAnnotationClass())
                .stream().map(WebControllerGenerator::from)
                .collect(Collectors.toList()));

        builder = adaptAnnotationsOnBuilder(builder, baseClass, name);

        Unloaded<NestedRestfulController> unloaded = builder.make();
        return (Class<NestedRestfulController<T1, T2>>)
            saveAndReturnClass(unloaded, name);
    }

    private <T> Builder<T> generateControllerType(
            Class<T> superInterface, Class<T> supertypeImpl, String name, String endpoint, Class<?>... genericTypes) {
        String _name = name.substring(name.lastIndexOf("$") + 1);
        _name = _name.replace("Controller", "");
        final Inflector inflector = Inflector.getInstance();
        _name = inflector.underscore(inflector.pluralize(_name)).replace("_", "-");

        List<AnnotationDescription> annoDescs = adaptAnnotations(superInterface.getAnnotations(), _name)
                .stream().map(WebControllerGenerator::from).collect(Collectors.toList());
        annoDescs.addAll(adaptAnnotations(supertypeImpl.getAnnotations(), _name)
                .stream().map(WebControllerGenerator::from).collect(Collectors.toList()));
        
        Builder<T> builder = (Builder<T>) new ByteBuddy()
            .subclass(TypeDescription.Generic.Builder
                    .parameterizedType(supertypeImpl, genericTypes).build(),
                ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
            .annotateType(annoDescs)
            .annotateType(
                ofType(Component.class)
                    .define("value", name)
                .build()
            ).name(name);

        return builder;
    }

    private static Class saveAndReturnClass(Unloaded<?> unloaded, String name) {
        try {
            unloaded.saveIn(new File("target/classes"));
            return Class.forName(name);
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    
}
