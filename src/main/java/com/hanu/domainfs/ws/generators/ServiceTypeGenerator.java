package com.hanu.domainfs.ws.generators;

import static net.bytebuddy.description.annotation.AnnotationDescription.Builder.ofType;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hanu.domainfs.ws.generators.services.SimpleDomServiceAdapter;
import com.hanu.domainfs.ws.generators.services.InheritedDomServiceAdapter;
import com.hanu.domainfs.ws.generators.services.CrudService;

import com.hanu.domainfs.ws.utils.InheritanceUtils;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class ServiceTypeGenerator {
    private static final Class crudServiceClass = CrudService.class;
    private static final Class absCrudServiceClass = SimpleDomServiceAdapter.class;
    private static final Class absInheritedCrudServiceClass = InheritedDomServiceAdapter.class;

    private final Map<String, Class<?>> generatedServices;

    public ServiceTypeGenerator() {
        generatedServices = new ConcurrentHashMap<>();
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
        Unloaded unloaded;
        final boolean hasInherit = Modifier.isAbstract(type.getModifiers());
        Class<CrudService> superClass = hasInherit ? absInheritedCrudServiceClass : absCrudServiceClass;
        Builder<?> builder = new ByteBuddy()
                    .subclass(superClass)
                    .annotateType(
                    ofType(Service.class)
                        .define("value", simpleName)
                    .build());
        if (hasInherit) {
            unloaded = generateServiceTypeWithInherit(builder, type, name);
        } else {
            unloaded = generateServiceType(builder, type, name);
        }

        Class returning = saveAndReturnClass(unloaded, name);
        generatedServices.put(genericTypeName, returning);
        return (Class<CrudService<T, ID>>) returning;
    }

    private static <T> Unloaded<T> generateServiceType(
        Builder<T> builder, Class<?> type, String name) {

        int numOfParams = absCrudServiceClass
            .getDeclaredConstructors()[0].getParameterTypes().length;
        return builder
            .constructor(ElementMatchers.isConstructor()
                    .and(ElementMatchers.takesArguments(numOfParams)))
            .intercept(MethodCall
                .invoke(absCrudServiceClass
                    .getDeclaredConstructors()[0])
                .withAllArguments()
                .andThen(MethodCall
                    .invoke(ElementMatchers.named("setType"))
                    .onSuper()
                    .with(type)))
            .annotateMethod(ofType(Autowired.class).build())
            .name(name)
            .make();
    }

    private static <T> Unloaded<T> generateServiceTypeWithInherit(
            Builder<T> builder, Class<?> type, String name) {

        int numOfParams = absInheritedCrudServiceClass
                        .getDeclaredConstructors()[0].getParameterTypes().length;

        return builder
            .constructor(ElementMatchers.isConstructor()
                    .and(ElementMatchers.takesArguments(numOfParams)))
            .intercept(Advice.to(InheritDomServiceAdapterConstructorAdvice.class).wrap(
                MethodCall
                    .invoke(absInheritedCrudServiceClass
                        .getDeclaredConstructors()[0])
                    .withAllArguments()
                .andThen(MethodCall
                    .invoke(ElementMatchers.named("setType"))
                    .onSuper()
                    .with(type))
                ))
            .annotateMethod(ofType(Autowired.class).build())
            .annotateParameter(1,
                    ofType(Qualifier.class)
                    .define("value", type.getName())
                    .build())
            .name(name)
            .make();
    }

    private static Class saveAndReturnClass(Unloaded<?> unloaded, String name) {
        try {
            unloaded.saveIn(new File("target/classes"));
            return Class.forName(name);
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    static class InheritDomServiceAdapterConstructorAdvice {
        @Advice.OnMethodExit
        static void exit(@Advice.This InheritedDomServiceAdapter instance) {
            instance.setSubtypes(InheritanceUtils.getSubtypeMapFor(instance.getType()));
        }
    }

}
