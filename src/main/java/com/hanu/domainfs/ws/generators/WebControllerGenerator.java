package com.hanu.domainfs.ws.generators;

import static net.bytebuddy.description.annotation.AnnotationDescription.Builder.ofType;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.hanu.domainfs.ws.generators.controllers.NestedRestfulController;
import com.hanu.domainfs.ws.generators.controllers.RestfulController;
import com.hanu.domainfs.ws.generators.services.CrudService;
import com.hanu.domainfs.ws.generators.services.InheritedCrudService;
import com.hanu.domainfs.ws.generators.utils.ClassAssocUtils;
import com.hanu.domainfs.ws.generators.utils.GenericTypeUtils;
import com.hanu.domainfs.ws.generators.utils.InterfaceUtils;
import com.hanu.domainfs.ws.generators.utils.InterfaceUtils.FieldDelegatingMethodDef;
import com.hanu.domainfs.ws.generators.utils.InterfaceUtils.MethodDef;
import com.hanu.domainfs.ws.svcdesc.ServiceController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation.Composable;
import net.bytebuddy.implementation.MethodCall;

/**
 * Generate web controllers based on domain models and their relationships.
 * @author binh_dh
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class WebControllerGenerator {

    private static final WebControllerGenerator instance = new WebControllerGenerator();

    public static WebControllerGenerator instance() {
        return instance;
    }

    private static final ClassPool classPool = ClassPool.getDefault();
    private static final Class crudServiceClass = CrudService.class;
    private static final Class inheritedCrudServiceClass = InheritedCrudService.class;
    private static final Class serviceCtrlClass = ServiceController.class;
    private static final Class<RestfulController> restCtrlClass = RestfulController.class;
    private static final Class nestedRestCtrlClass = NestedRestfulController.class;

    private final Map<String, Class<?>> generatedCrudClasses;
    private final Map<String, Generic> generatedClassFields;

    private WebControllerGenerator() {
        generatedCrudClasses = new HashMap<>();
        generatedClassFields = new HashMap<>();
    }

    public Collection<Class<?>> getGeneratedCrudClasses() {
        return generatedCrudClasses.values();
    }

    public <T, ID extends Serializable> Class<RestfulController<T, ID>> 
            getRestfulController(Class<T> type, Class<ID> idType) {
        try {
            String typeName = type.getName();
            if (!generatedCrudClasses.containsKey(typeName)) {
                generatedCrudClasses.put(typeName, 
                    generateRestfulController(type, idType));
            }
            return (Class<RestfulController<T, ID>>) 
                generatedCrudClasses.get(typeName);
        } catch (NotFoundException | CannotCompileException |
                IllegalAccessException | IOException
                | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }
    private <T, ID extends Serializable> Class<RestfulController<T, ID>> 
            generateRestfulController(Class<T> type, Class<ID> idType)
                throws NotFoundException, CannotCompileException, 
                    IllegalAccessException, IOException, NoSuchMethodException, SecurityException {
        //
        final String endpoint = "/" + type.getSimpleName().toLowerCase() + "s";
        final String name = restCtrlClass.getName() + "$$" 
            + type.getSimpleName() + "Controller";
        Builder<RestfulController> builder = new ByteBuddy()
            .subclass(restCtrlClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
            .annotateType(
                AnnotationDescription.Builder.ofType(RestController.class).build()
            ).annotateType(
                ofType(RequestMapping.class)
                    .defineArray("value", endpoint)
                    .build()
            ).annotateType(
                ofType(ServiceController.class)
                    .define("endpoint", endpoint)
                    .define("name", "Manage " + endpoint.substring(1))
                    .build()
            )
            .name(name);

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
        // TODO: extract this annotation array
        final AnnotationDescription[][] methodAnnotations = {
            { ofType(PostMapping.class).build() },
            { ofType(GetMapping.class).defineArray("value", "/{id}").build() },
            { ofType(GetMapping.class).build() },
            { ofType(PatchMapping.class).defineArray("value", "/{id}").build() },
            { ofType(DeleteMapping.class).defineArray("value", "/{id}").build() }
        };
        final AtomicInteger counter = new AtomicInteger();
        for (MethodDef def : methodDefs) {
            builder = generatePublicDelegatingMethod(
                builder, (FieldDelegatingMethodDef)def, 
                methodAnnotations[counter.getAndIncrement()]);
        }

        Unloaded<RestfulController> unloaded = builder.make();
        Map<TypeDescription, File> saved = unloaded
            .saveIn(new File("target/classes"));
        // no aux type so return the first key
        try {
            String className = saved.keySet().stream().findFirst().get().getTypeName();
            return (Class<RestfulController<T, ID>>) Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
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

        final AtomicInteger counter = new AtomicInteger();
        for (Class<?> cls : associated) {
            final String suffix = "Service";
            final String fieldName = cls.getSimpleName().toLowerCase() + suffix;
            System.out.println(fieldName);
            Generic serviceType = generatedClassFields.get(fieldName);
            params.add(serviceType);
            body = body.andThen(
                FieldAccessor.ofField(fieldName).setsArgumentAt(counter.getAndIncrement()));
        }

        return source.defineConstructor(Visibility.PUBLIC)
                .withParameters(params)
                .intercept(body)
                .annotateMethod(
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

    
}
