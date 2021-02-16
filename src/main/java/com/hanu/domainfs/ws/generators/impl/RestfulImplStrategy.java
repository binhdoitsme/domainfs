package com.hanu.domainfs.ws.generators.impl;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

final class RestfulImplStrategy implements ImplementationStrategy {

    private final String serviceFieldName;

    RestfulImplStrategy(String serviceField) {
        this.serviceFieldName = serviceField;
    }

    @Override
    public List<MethodDef> implementMethods(Class<?> abstractType) {
        final Method[] abstractMethods = abstractType.getMethods();
        final List<MethodDef> methodDefs = new LinkedList<>();
        for (Method method : abstractMethods) {
            final String name = method.getName();
            final Class<?> returnType = method.getReturnType();
            final Class<?>[] params = method.getParameterTypes();
            final MethodDef def = new FieldDelegateMethodDef(
                name, returnType, params, name, serviceFieldName);
            methodDefs.add(def);
        }
        return methodDefs;
    }
    
}
