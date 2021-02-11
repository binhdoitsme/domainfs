package com.hanu.domainfs.ws.generators;

import static net.bytebuddy.description.annotation.AnnotationDescription.Builder.ofType;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.hanu.domainfs.ws.generators.controllers.NestedRestfulController;
import com.hanu.domainfs.ws.generators.controllers.RestfulController;
import com.hanu.domainfs.ws.generators.services.AbstractCrudService;
import com.hanu.domainfs.ws.generators.services.AbstractInheritedCrudService;
import com.hanu.domainfs.ws.generators.services.CrudService;
import com.hanu.domainfs.ws.generators.utils.ClassAssocUtils;
import com.hanu.domainfs.ws.generators.utils.GenericTypeUtils;
import com.hanu.domainfs.ws.generators.utils.InterfaceUtils;
import com.hanu.domainfs.ws.generators.utils.InterfaceUtils.FieldDelegatingMethodDef;
import com.hanu.domainfs.ws.generators.utils.InterfaceUtils.MethodDef;
import com.hanu.domainfs.ws.svcdesc.ServiceController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import domainapp.softwareimpl.SoftwareImpl;
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
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Generate web controllers based on domain models and their relationships.
 * 
 * @author binh_dh
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class WebControllerGenerator {

    private static final WebControllerGenerator instance = new WebControllerGenerator();
    
    public static WebControllerGenerator instance() {
        return instance;
    }
    
    private static final Class crudServiceClass = CrudService.class;
    private static final Class absCrudServiceClass = AbstractCrudService.class;
    private static final Class inheritedCrudServiceClass = AbstractInheritedCrudService.class;
    private static final Class serviceCtrlClass = ServiceController.class;
    private static final Class<RestfulController> restCtrlClass = RestfulController.class;
    private static final Class nestedRestCtrlClass = NestedRestfulController.class;
    private static final Class<SoftwareImpl> swImplClass = SoftwareImpl.class;

    private final Map<String, Class<?>> generatedCrudClasses;
    private final Map<String, Class<?>> generatedServices;
    private final Map<String, Generic> generatedClassFields;

    private WebControllerGenerator() {
        generatedCrudClasses = new HashMap<>();
        generatedClassFields = new HashMap<>();
        generatedServices = new HashMap<>();
    }

    /**
     * Get the RESTful controller from a generic type.
     */
    public <T, ID extends Serializable> Class<RestfulController<T, ID>> 
            getRestfulController(Class<T> type) {
        try {
            String typeName = type.getName();
            if (!generatedCrudClasses.containsKey(typeName)) {
                Class<ID> idType = (Class<ID>) type.getDeclaredField("id").getType();
                generatedCrudClasses.put(typeName, 
                    generateRestfulController(type, idType));
            }
            return (Class<RestfulController<T, ID>>) generatedCrudClasses.get(typeName);
        } catch (IllegalAccessException | IOException 
                | NoSuchMethodException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    private <T, ID extends Serializable> Class<RestfulController<T, ID>> generateRestfulController(Class<T> type,
            Class<ID> idType) throws IllegalAccessException, IOException, NoSuchMethodException, SecurityException {
        //
        final String endpoint = "/" + type.getSimpleName().toLowerCase() + "s";
        final String name = restCtrlClass.getName() + "$$" + type.getSimpleName() + "Controller";
        Builder<RestfulController> builder = new ByteBuddy()
                .subclass(restCtrlClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .annotateType(
                    ofType(RestController.class).build()
                ).annotateType(
                    ofType(RequestMapping.class)
                        .defineArray("value", endpoint)
                    .build()
                ).annotateType(
                    ofType(ServiceController.class)
                        .define("endpoint", endpoint)
                        .define("name", "Manage " + endpoint.substring(1))
                    .build()
                ).annotateType(
                    ofType(Component.class)
                        .define("value", name)
                    .build()
                ).name(name);

        try {
            // service field(s)
            builder = generateServiceFields(builder, type, false);

            // autowired constructor
            builder = generateAutowiredConstructor(builder, type, false);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }

        final String serviceFieldName = type.getSimpleName().toLowerCase().concat("Service");

        final List<MethodDef> methodDefs = InterfaceUtils.implementMethods(restCtrlClass, serviceFieldName);
        // TODO: use self-defined annotations to bridge with framework-dependent
        final AnnotationDescription[][] methodAnnotations = {
            { ofType(PostMapping.class).build() },
            { ofType(GetMapping.class).defineArray("value", "/{id}").build() },
            { ofType(GetMapping.class).build() },
            { ofType(PatchMapping.class).defineArray("value", "/{id}").build() },
            { ofType(DeleteMapping.class).defineArray("value", "/{id}").build() }
        };
        final AtomicInteger counter = new AtomicInteger();
        for (MethodDef def : methodDefs) {
            builder = generatePublicDelegatingMethod(builder, (FieldDelegatingMethodDef) def,
                    methodAnnotations[counter.getAndIncrement()]);
        }

        Unloaded<RestfulController> unloaded = builder.make();
        return (Class<RestfulController<T, ID>>) saveAndReturnClass(unloaded, name);
    }

    /**
     * Generate service types for autowiring.
     */
    public <T, ID extends Serializable> Class<CrudService<T, ID>> 
            generateAutowiredServiceType(Class<T> type) {
        //
        String genericTypeName = type.getName();

        if (generatedServices.containsKey(genericTypeName)) {
            return (Class<CrudService<T, ID>>) 
                generatedServices.get(genericTypeName);
        }

        final String name = crudServiceClass.getName() + "$$" 
                                + type.getSimpleName() + "Service";

        final String simpleName = type.getSimpleName().toLowerCase() + "Service";

        // 
        Unloaded unloaded = null;
        Builder<Object> builder = null;
        final boolean hasInherit = Modifier.isAbstract(type.getModifiers());
        if (hasInherit) {
            builder = new ByteBuddy().subclass(inheritedCrudServiceClass);
        } else {
            builder = new ByteBuddy().subclass(absCrudServiceClass);
        }
        builder = builder.annotateType(
                    ofType(Service.class)
                        .define("value", simpleName)
                    .build());
        try {
            System.out.println(absCrudServiceClass.getDeclaredConstructors()[0]);
            if (hasInherit) {
                int numOfParams = inheritedCrudServiceClass
                        .getDeclaredConstructors()[0].getParameterTypes().length;
                unloaded = builder
                    .constructor(ElementMatchers.isConstructor()
                            .and(ElementMatchers.takesArguments(numOfParams)))
                    .intercept(MethodCall
                    .invoke(inheritedCrudServiceClass
                        .getDeclaredConstructors()[0])
                    .withAllArguments())
                .annotateMethod(ofType(Autowired.class).build())
                .annotateParameter(1, 
                        ofType(Qualifier.class)
                        .define("value", type.getName())
                        .build())
                .name(name)
                .make();
            } else {
                int numOfParams = absCrudServiceClass
                        .getDeclaredConstructors()[0].getParameterTypes().length;
                unloaded = builder
                    .constructor(ElementMatchers.isConstructor()
                            .and(ElementMatchers.takesArguments(numOfParams)))
                    .intercept(MethodCall
                    .invoke(absCrudServiceClass
                        .getDeclaredConstructors()[0])
                    .withAllArguments())
                    .annotateMethod(ofType(Autowired.class).build())
                    .name(name)
                    .make();
            }
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
        Class returning = saveAndReturnClass(unloaded, name);
        generatedServices.put(genericTypeName, returning);
        return (Class<CrudService<T, ID>>) returning;
    }

    private <U> Builder<U> generateServiceFields(
            Builder<U> source, Class<?> domClass, boolean fetchAssociations) {
        if (!fetchAssociations) {
            return generateServiceField(source, domClass);
        }
        
        Builder<U> srcCopy = source;
        List<Class<?>> associatedClasses = ClassAssocUtils.getAssociations(domClass);
        for (Class<?> associated : associatedClasses) {
            try {
                srcCopy = generateServiceField(source, associated, false);
            } catch (NoSuchFieldException ex) {
                throw new RuntimeException(ex);
            }
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
            domClass.getDeclaredField("id").getType());
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

        return source.defineField(fieldName, serviceType);
    }

    private <U> Builder<U> generateAutowiredConstructor(
            Builder<U> source, Class<?> domClass, boolean withAssociations)
                throws NoSuchMethodException, SecurityException {
        List<Class<?>> associated = Arrays.asList(domClass);
        if (withAssociations) {
            associated.addAll(ClassAssocUtils.getAssociations(domClass));
        } 

        final List<TypeDefinition> params = new LinkedList<>();
        Composable body = MethodCall.invoke(Object.class.getConstructor());

        final Map<Integer, AnnotationDescription> annotations = new HashMap<>();

        final AtomicInteger counter = new AtomicInteger();
        for (final Class<?> cls : associated) {
            final String suffix = "Service";
            final String fieldName = cls.getSimpleName().toLowerCase() + suffix;
            System.out.println(fieldName);
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
            Builder<U> source, FieldDelegatingMethodDef def,
            AnnotationDescription[] annotations) {
        System.out.println("..." + def);
        return source.defineMethod(def.getName(), def.getReturnType(), Visibility.PUBLIC)
                .withParameters(def.getParameters())
                .intercept(def.getBody())
                .annotateMethod(annotations);
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
