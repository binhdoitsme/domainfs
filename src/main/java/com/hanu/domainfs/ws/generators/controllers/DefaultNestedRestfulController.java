package com.hanu.domainfs.ws.generators.controllers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.hanu.domainfs.ws.generators.models.Identifier;
import com.hanu.domainfs.ws.generators.services.CrudService;

import domainapp.basics.model.meta.DOpt;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class DefaultNestedRestfulController<T1, T2>
        implements NestedRestfulController<T1, T2> {

    protected Class<T2> innerType = (Class) ((ParameterizedType) getClass()
        .getGenericSuperclass()).getActualTypeArguments()[1];
    protected Class<T1> outerType = (Class) ((ParameterizedType) getClass()
        .getGenericSuperclass()).getActualTypeArguments()[0];

    protected <X> CrudService<X> getServiceOfGenericType(String clsName) {
        return ServiceRegistry.getInstance().get(clsName);
    }

    @Override
    public T2 createInner(Identifier<?> outerId, Map<String, Object> requestBody) {

        final Map<String, Object> inputs = new HashMap<>();
        for (String key : requestBody.keySet()) {
            String type = key.replace("Id", "");
            Object value = getServiceOfGenericType(type)
                    .getEntityById(new Identifier<>(requestBody.get(key)));
            if (value == null) throw new RuntimeException();
            inputs.put(type, value);
        }

        Constructor<?> requiredConstructor = getRequiredConstructor(innerType);

        Class<?>[] parameterTypes = requiredConstructor.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        final AtomicInteger counter = new AtomicInteger();
        for (Class<?> paramType : parameterTypes) {
            String paramTypeName = paramType.getSimpleName();
            arguments[counter.get()] = inputs.get(Character.toLowerCase(paramTypeName.charAt(0)) + paramTypeName.substring(1));
            counter.incrementAndGet();
        }

        try {
            T2 instance = (T2) requiredConstructor.newInstance(arguments);
            CrudService<T2> svc = getServiceOfGenericType(innerType.getCanonicalName());
            return svc.createEntity(instance);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException ex) {
            throw new RuntimeException(ex);
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
    public Collection<T2> getInnerListByOuterId(Identifier<?> outerId) {
        // reflection-based solution -- needs HEAVY optimization
        CrudService<T1> svc = getServiceOfGenericType(outerType.getCanonicalName());
        T1 outerById = svc.getEntityById(outerId);
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
