package com.hanu.domainfs.ws.generators.impl;

import java.util.stream.IntStream;

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

final class FieldDelegateMethodDef extends MethodDef {

    public FieldDelegateMethodDef(String name, Class<?> returnType, 
                Class<?>[] params, String targetMethodName, String targetField) {
        super(name, returnType, params, 
            makeImplementation(params, targetMethodName, targetField));
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
    
}
