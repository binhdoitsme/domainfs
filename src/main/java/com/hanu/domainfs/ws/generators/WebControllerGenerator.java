package com.hanu.domainfs.ws.generators;

import static net.bytebuddy.description.annotation.AnnotationDescription.Builder.ofType;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.hanu.domainfs.utils.IdentifierUtils;
import com.hanu.domainfs.ws.generators.controllers.NestedRestfulController;
import com.hanu.domainfs.ws.generators.controllers.RestfulController;
import com.hanu.domainfs.ws.generators.controllers.RestfulWithInheritanceController;
import com.hanu.domainfs.ws.generators.controllers.SimpleNestedRestfulController;
import com.hanu.domainfs.ws.generators.impl.ImplementationStrategy;
import com.hanu.domainfs.ws.generators.impl.MethodDef;
import com.hanu.domainfs.ws.generators.services.CrudService;
import com.hanu.domainfs.ws.generators.services.InheritedCrudService;
import com.hanu.domainfs.ws.svcdesc.ServiceController;
import com.hanu.domainfs.ws.utils.ClassAssocUtils;
import com.hanu.domainfs.ws.utils.GenericTypeUtils;
import com.hanu.domainfs.utils.NamingUtils;

import org.modeshape.common.text.Inflector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation.Composable;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.implementation.MethodCall;

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

    private static final Class crudServiceClass = CrudService.class;
    private static final Class inheritedCrudServiceClass = InheritedCrudService.class;

    private static final Class<RestfulController> restCtrlClass = RestfulController.class;
    private static final Class<RestfulController> inheritRestCtrlClass = (Class)RestfulWithInheritanceController.class;
    private static final Class nestedRestCtrlClass = NestedRestfulController.class;
    private static final Class nestedRestCtrlImplClass = SimpleNestedRestfulController.class;

    private final Map<String, Class<?>> generatedCrudClasses;
    private final Map<String, Generic> generatedClassFields;

    private WebControllerGenerator() {
        generatedCrudClasses = new HashMap<>();
        generatedClassFields = new HashMap<>();
    }

    /**
     * Get the RESTful controller from a generic type.
     */
    public <T, ID extends Serializable> Class<RestfulController<T, ID>>
            getRestfulController(Class<T> type) {
        try {
            String typeName = type.getName();
            if (!generatedCrudClasses.containsKey(typeName)) {
                Class<ID> idType = (Class<ID>)GenericTypeUtils.getWrapperClass(IdentifierUtils.getIdField(type).getType());
                generatedCrudClasses.put(typeName,
                    generateRestfulController(type, idType));
            }
            return (Class<RestfulController<T, ID>>) generatedCrudClasses.get(typeName);
        } catch (IllegalAccessException | IOException
                | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get a nested RESTful controller from a generic type.
     */
    public <T1, ID1 extends Serializable, T2>
            Class<NestedRestfulController<T1, ID1, T2>>
            getNestedRestfulController(Class<T1> outerType, Class<T2> innerType) {
        try {
            return generateNestedRestfulController(outerType, innerType);
        } catch (IllegalAccessException | IOException
                | NoSuchMethodException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Name cannot have more than 1 '$' token
    private <T, ID extends Serializable> Class<RestfulController<T, ID>>
        generateRestfulController(Class<T> type, Class<ID> idType)
            throws IllegalAccessException, IOException,
                NoSuchMethodException, SecurityException {
        //
        final boolean hasInheritance = Modifier.isAbstract(type.getModifiers());
        final Class<RestfulController> baseClass = hasInheritance ? inheritRestCtrlClass : restCtrlClass;
        final Inflector inflector = Inflector.getInstance();
        final String endpoint = "/" +
                inflector.underscore(
                    inflector.pluralize(type.getSimpleName())).replace("_", "-");
        final String pkg = type.getPackage().getName().replace(".model", "");
        final String name = NamingUtils.classNameFrom(pkg, restCtrlClass, "Controller", type);
        Builder<RestfulController> builder =
            generateControllerType(baseClass, name, endpoint, type, idType)
                .annotateType(
                    ofType(ServiceController.class)
                        .define("endpoint", endpoint)
                        .define("name", "Manage " + inflector.humanize(inflector.pluralize(inflector.underscore(type.getSimpleName()))))
                        .define("className", type.getName())
                    .build());

        try {
            // service field(s)
            builder = generateServiceFields(builder, type, false);

            // autowired constructor
            builder = generateAutowiredConstructor(builder, type, false);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        final String serviceFieldName = type.getSimpleName().toLowerCase().concat("Service");

        final List<MethodDef> methodDefs =
            ImplementationStrategy.getStrategy("restful", serviceFieldName)
                .implementMethods(baseClass);

        for (MethodDef def : methodDefs) {
            AnnotationDescription[] methodAnnotations = AnnotationUtils.getMethodAnnotations(def.getName(), hasInheritance);
            AnnotationDescription[] paramAnnotations = AnnotationUtils.getParameterAnnotations(def.getName(), hasInheritance);
            builder = generatePublicDelegatingMethod(builder, def, methodAnnotations, paramAnnotations);
        }

        Unloaded<RestfulController> unloaded = builder.make();
        return (Class<RestfulController<T, ID>>) saveAndReturnClass(unloaded, name);
    }

    private <T1, ID1 extends Serializable, T2>
        Class<NestedRestfulController<T1, ID1, T2>>
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
        final Class<?> outerIdType = GenericTypeUtils.getWrapperClass(
            IdentifierUtils.getIdField(outerType).getType());
        Builder<NestedRestfulController> builder =
            generateControllerType(nestedRestCtrlImplClass, name, endpoint,
                outerType, outerIdType, innerType);

        try {
            // service field(s)
            builder = generateServiceFields(builder, innerType, true);

            // autowired constructor
            builder = generateAutowiredConstructor(builder, innerType, true);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        builder = builder
                    .method(ElementMatchers.named("createInner"))
                    .intercept(
                        FieldAccessor.ofField("innerType")
                            .setsValue(innerType)
                            .andThen(FieldAccessor.ofField("outerType")
                                .setsValue(outerType))
                            .andThen(MethodCall.invokeSuper().withAllArguments())
                    )
                    .annotateParameter(0,
                        ofType(PathVariable.class).define("value", "id").build())
                    .annotateParameter(1,
                        ofType(RequestBody.class).build())
                    .annotateMethod(
                        ofType(PostMapping.class).build())
                    .method(ElementMatchers.named("getInnerListByOuterId"))
                    .intercept(
                        FieldAccessor.ofField("innerType")
                            .setsValue(innerType)
                        .andThen(FieldAccessor.ofField("outerType")
                            .setsValue(outerType))
                        .andThen(MethodCall.invokeSuper().withAllArguments())
                    )
                    .annotateParameter(0,
                        ofType(PathVariable.class).define("value", "id").build())
                    .annotateMethod(
                        ofType(GetMapping.class).build());


        Unloaded<NestedRestfulController> unloaded = builder.make();
        return (Class<NestedRestfulController<T1, ID1, T2>>)
            saveAndReturnClass(unloaded, name);
    }

    private <T> Builder<T> generateControllerType(
            Class<T> supertype, String name, String endpoint, Class<?>... genericTypes) {
        Builder<T> builder = (Builder<T>) new ByteBuddy()
            .subclass(TypeDescription.Generic.Builder
                    .parameterizedType(supertype, genericTypes).build(),
                ConstructorStrategy.Default.NO_CONSTRUCTORS)
            .annotateType(
                ofType(RestController.class).build()
            ).annotateType(
                ofType(RequestMapping.class)
                    .defineArray("value", endpoint)
                .build()
            ).annotateType(
                ofType(Component.class)
                    .define("value", name)
                .build()
            ).name(name);

        return builder;
    }


    private <U> Builder<U> generateServiceFields(
            Builder<U> source, Class<?> domClass, boolean fetchAssociations) {
        if (!fetchAssociations) {
            return generateServiceField(source, domClass);
        }

        Builder<U> srcCopy = source;
        List<Class<?>> associatedClasses = ClassAssocUtils.getAssociated(domClass);
        associatedClasses.add(domClass);
        for (Class<?> associated : associatedClasses) {
            srcCopy = generateServiceField(srcCopy, associated);
        }
        return srcCopy;
    }

    private <U> Builder<U> generateServiceField(Builder<U> source, Class<?> domClass) {
        final boolean isInherited = Modifier.isAbstract(domClass.getModifiers());
        try {
            return generateServiceField(source, domClass, isInherited);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private <U> Builder<U> generateServiceField(
            Builder<U> source, Class<?> domClass, boolean isInherited)
                throws NoSuchFieldException, SecurityException {
        //
        final String suffix = "Service";
        final String fieldName = domClass.getSimpleName().toLowerCase() + suffix;
        final Class<?> idType = GenericTypeUtils.getWrapperClass(
            IdentifierUtils.getIdField(domClass).getType());
        Generic serviceType;
        if (generatedClassFields.containsKey(fieldName)) {
            serviceType = generatedClassFields.get(fieldName);
        } else
        if (isInherited) {
            serviceType = TypeDescription.Generic.Builder
                .parameterizedType(inheritedCrudServiceClass, domClass, idType).build();
        } else {
            serviceType = TypeDescription.Generic.Builder
                .parameterizedType(crudServiceClass, domClass, idType).build();
        }
        generatedClassFields.putIfAbsent(fieldName, serviceType);

        return source.defineField(fieldName, serviceType, Visibility.PRIVATE);
    }

    private <U> Builder<U> generateAutowiredConstructor(
            Builder<U> source, Class<?> domClass, boolean withAssociations)
                throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        List<Class<?>> associated = new LinkedList<>();
        if (withAssociations) {
            associated.addAll(ClassAssocUtils.getAssociated(domClass));
        } else {
            associated.add(domClass);
        }

        final List<TypeDefinition> params = new LinkedList<>();

        final boolean isInterface = Modifier.isInterface(domClass.getModifiers());
        String superName = source.toTypeDescription().getSuperClass().asErasure().getName();
        Constructor<?> superCtor = isInterface ? Object.class.getConstructor()
                : Class.forName(superName).getConstructor();
        Composable body = MethodCall.invoke(superCtor);

        final Map<Integer, AnnotationDescription> annotations = new HashMap<>();

        final AtomicInteger counter = new AtomicInteger();
        for (final Class<?> cls : associated) {
            final String suffix = "Service";
            final String fieldName = cls.getSimpleName().toLowerCase() + suffix;

            Generic serviceType = generatedClassFields.get(fieldName);
            params.add(serviceType);

            body = body.andThen(
                FieldAccessor.ofField(fieldName)
                    .setsArgumentAt(counter.get()));

            annotations.putIfAbsent(
                counter.getAndIncrement(),
                ofType(Qualifier.class)
                    .define("value", fieldName)
                    .build());
        }

        MethodDefinition<U> srcCopy = source
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(params)
                .intercept(body);

        for (final int paramIndex : annotations.keySet()) {
            final AnnotationDescription annotation = annotations.get(paramIndex);
            srcCopy = srcCopy.annotateParameter(paramIndex, annotation);
        }

        return srcCopy.annotateMethod(
            AnnotationDescription.Builder.ofType(Autowired.class).build()
        );
    }

    private <U> Builder<U> generatePublicDelegatingMethod(
            Builder<U> source, MethodDef def,
            AnnotationDescription[] annotations,
            AnnotationDescription[] paramAnnotations) {
        // System.out.println("..." + def);
        MethodDefinition<U> desc = source
                .defineMethod(def.getName(), def.getReturnType(), Visibility.PUBLIC)
                .withParameters(def.getParameters())
                .intercept(def.getBody())
                .annotateMethod(annotations);
        final AtomicInteger counter = new AtomicInteger();
        for (AnnotationDescription paramAnnotation : paramAnnotations) {
            desc = desc.annotateParameter(counter.getAndIncrement(), paramAnnotation);
        }
        return desc;
    }

    private static Class saveAndReturnClass(Unloaded<?> unloaded, String name) {
        try {
            unloaded.saveIn(new File("target/classes"));
            return Class.forName(name);
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    // TODO: use self-defined annotations to bridge with framework-dependent
    private static final class AnnotationUtils {
        private static final Map<Boolean, Map<String, AnnotationDescription[][]>> annotationMap = new HashMap<>();

        public static AnnotationDescription[] getMethodAnnotations(String methodName, boolean hasInherit) {
            try {
                return annotationMap.get(hasInherit).get(methodName)[0];
            } catch (NullPointerException ex) {
                return new AnnotationDescription[] {};
            }
        }

        public static AnnotationDescription[] getParameterAnnotations(String methodName, boolean hasInherit) {
            try {
                return annotationMap.get(hasInherit).get(methodName)[1];
            } catch (NullPointerException ex) {
                return new AnnotationDescription[] {};
            }
        }

        private static void addNoInheritMappings() {
            final Map<String, AnnotationDescription[][]> noInheritMap = new HashMap<>();
            noInheritMap.put("createEntity", new AnnotationDescription[][] {
                { ofType(PostMapping.class).build() },
                { ofType(RequestBody.class).build() }
            });
            noInheritMap.put("getEntityListByPage", new AnnotationDescription[][] {
                { ofType(GetMapping.class).build() },
                { ofType(RequestParam.class)
                    .define("value", "page").define("defaultValue", "1").build(),
                  ofType(RequestParam.class)
                    .define("value", "count").define("defaultValue", "20").build() }
            });
            noInheritMap.put("getEntityById", new AnnotationDescription[][] {
                { ofType(GetMapping.class).defineArray("value", "/{id}").build() },
                { ofType(PathVariable.class).define("value", "id").build() }
            });
            noInheritMap.put("updateEntity", new AnnotationDescription[][] {
                { ofType(PatchMapping.class).defineArray("value", "/{id}").build() },
                { ofType(PathVariable.class).define("value", "id").build(),
                  ofType(RequestBody.class).build() }
            });
            noInheritMap.put("deleteEntityById", new AnnotationDescription[][] {
                { ofType(DeleteMapping.class).defineArray("value", "/{id}").build() },
                { ofType(PathVariable.class).define("value", "id").build() }
            });
            annotationMap.put(false, noInheritMap);
        }

        private static void addInheritMappings() {
            final Map<String, AnnotationDescription[][]> inheritMap = new HashMap<>();
            inheritMap.put("createEntity", new AnnotationDescription[][] {
                { ofType(PostMapping.class).build() },
                { ofType(RequestBody.class).build() }
            });
            inheritMap.put("getEntityListByTypeAndPage", new AnnotationDescription[][] {
                { ofType(GetMapping.class).build() },
                { ofType(RequestParam.class)
                    .define("value", "type").define("required", false).build() ,
                ofType(RequestParam.class)
                    .define("value", "page").define("defaultValue", "1").build(),
                ofType(RequestParam.class)
                    .define("value", "count").define("defaultValue", "20").build()}
            });
            inheritMap.put("getEntityById", new AnnotationDescription[][] {
                { ofType(GetMapping.class).defineArray("value", "/{id}").build() },
                { ofType(PathVariable.class).define("value", "id").build() }
            });
            inheritMap.put("updateEntity", new AnnotationDescription[][] {
                { ofType(PatchMapping.class).defineArray("value", "/{id}").build() },
                { ofType(PathVariable.class).define("value", "id").build(),
                  ofType(RequestBody.class).build() }
            });
            inheritMap.put("deleteEntityById", new AnnotationDescription[][] {
                { ofType(DeleteMapping.class).defineArray("value", "/{id}").build() },
                { ofType(PathVariable.class).define("value", "id").build() }
            });
            annotationMap.put(true, inheritMap);
        }

        static {
            addNoInheritMappings();
            addInheritMappings();
        }

    }
}
