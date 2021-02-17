package com.hanu.domainfs.ws.generators.controllers;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.hanu.domainfs.ws.generators.services.CrudService;

import domainapp.basics.model.meta.DOpt;

@SuppressWarnings("unchecked")
public class SimpleNestedRestfulController<T1, ID1 extends Serializable, T2>
        implements NestedRestfulController<T1, ID1, T2> {

    private final Class<?> clazz = getClass();
    protected Class<T2> innerType;
    protected Class<T1> outerType;

    @Override
    public T2 createInner(ID1 outerId, Map<String, String> requestBody) {
        // reflection-based solution -- needs HEAVY optimization
        Method getEntityById = getMethodByName(CrudService.class, "getEntityById", Serializable.class);

        final Map<String, Object> inputs = new HashMap<>();
        for (String key : requestBody.keySet()) {
            String type = key.replace("Id", "");
            String serviceField = type + "Service";
            try {
                Object value = getEntityById.invoke(clazz.getDeclaredField(serviceField).get(this),
                        requestBody.get(key));
                inputs.put(type, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchFieldException | SecurityException ex) {
                throw new RuntimeException(ex);
            }
        }

        Constructor<?> requiredConstructor = getRequiredConstructor(innerType);

        Class<?>[] parameterTypes = requiredConstructor.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        final AtomicInteger counter = new AtomicInteger();
        for (Class<?> paramType : parameterTypes) {
            arguments[counter.get()] = inputs.get(paramType.getSimpleName().toLowerCase());
            counter.incrementAndGet();
        }

        try {
            T2 instance = (T2) requiredConstructor.newInstance(arguments);
            return getServiceOfGenericType(innerType).createEntity(instance);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private <U, ID extends Serializable> CrudService<U, ID> getServiceOfGenericType(Class<U> type) {
        String serviceFieldName = type.getSimpleName().toLowerCase().concat("Service");
        try {
            Field serviceField = getClass().getDeclaredField(serviceFieldName);
            serviceField.setAccessible(true);
            return (CrudService<U, ID>) serviceField.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method getMethodByName(Class<?> cls, String name, Class<?>... parameters) {
        try {
            return cls.getMethod(name, parameters);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Constructor<?> getRequiredConstructor(Class<?> cls) {
        for (Constructor<?> c : cls.getConstructors()) {
            boolean isReqConstructor = false;
            DOpt[] dopts = c.getAnnotationsByType(DOpt.class);
            for (DOpt d : dopts) {
                if (d.type().equals(DOpt.Type.RequiredConstructor)) {
                    isReqConstructor = true;
                    break;
                }
            }
            if (isReqConstructor) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Collection<T2> getInnerListByOuterId(ID1 outerId) {
        // reflection-based solution -- needs HEAVY optimization

        T1 outerById = getServiceOfGenericType(outerType).getEntityById(outerId);
        String getInnerMethodName = "get" + innerType.getSimpleName() + "s";
        Method getInnersFromOuter = getMethodByName(outerType, getInnerMethodName);
        try {
            return (Collection<T2>) getInnersFromOuter.invoke(outerById);
        } catch (IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
