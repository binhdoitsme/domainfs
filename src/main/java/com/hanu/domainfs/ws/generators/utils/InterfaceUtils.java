package com.hanu.domainfs.ws.generators.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Utilities to implement an interface.
 */
public final class InterfaceUtils {

    public static class MethodDef {
        private String name;
        private Class<?> returnType;
        private Class<?>[] parameters;
        private Implementation body;

        public MethodDef(String name, Class<?> returnType, Class<?>[] params,
                Implementation body) {
            this.name = name;
            this.returnType = returnType;
            this.parameters = params;
            this.body = body;
        }

        public String getName() {
            return name;
        }

        public Implementation getBody() {
            return body;
        }

        public Class<?>[] getParameters() {
            return parameters;
        }

        public Class<?> getReturnType() {
            return returnType;
        }

        @Override
        public String toString() {
            return name + " " + Arrays.toString(parameters);
        }
    }

    public static class FieldDelegatingMethodDef extends MethodDef {
        public FieldDelegatingMethodDef(String name, Class<?> returnType, 
                Class<?>[] params, String targetMethodName, String targetField) {
            super(name, returnType, params, 
                makeImplementation(params, targetMethodName, targetField));
        }
    }

    /** Stub implementation */
    private static Implementation makeImplementation(Class<?>[] params,
            String targetMethodName, String targetField) {
        if (targetMethodName.contains("update")) {
            int[] argIndices = IntStream.range(1, params.length).toArray();
            return MethodCall.invoke(ElementMatchers.named(targetMethodName))
                .onField(targetField)
                .withArgument(argIndices);
        } else {
            return MethodCall.invoke(ElementMatchers.named(targetMethodName))
                    .onField(targetField)
                    .withAllArguments();
        }
    }

    /**
     * Implementing by delegating the call to some field.
     * @param interfaceType
     * @return
     */
    public static List<MethodDef> implementMethods(Class<?> interfaceType, String targetField) {
        // TODO: Implement a better version of this!
        final Method[] abstractMethods = interfaceType.getMethods();
        final List<MethodDef> methodDefs = new LinkedList<>();
        for (Method method : abstractMethods) {
            final String name = method.getName();
            final Class<?> returnType = method.getReturnType();
            final Class<?>[] params = method.getParameterTypes();

            final MethodDef def = new FieldDelegatingMethodDef(
                name, returnType, params, name, targetField);
            methodDefs.add(def);
        }
        return methodDefs;
    }
}
