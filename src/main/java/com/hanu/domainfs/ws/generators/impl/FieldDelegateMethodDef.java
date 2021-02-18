package com.hanu.domainfs.ws.generators.impl;

import java.lang.reflect.ParameterizedType;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paranamer.ParanamerModule;
import com.hanu.domainfs.ws.generators.controllers.RestfulController;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

@SuppressWarnings("rawtypes")
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
            return Advice.to(SecondParamRequestBodyAdvice.class)
                .wrap(MethodCall.invoke(ElementMatchers.named(targetMethodName))
                .onField(targetField)
                .withArgument(argIndices));
        } else if (targetMethodName.contains("create")) {
            return Advice.to(FirstParamRequestBodyAdvice.class)
                    .wrap(MethodCall.invoke(ElementMatchers.named(targetMethodName))
                        .onField(targetField)
                        .withAllArguments());
        } else {
            return MethodCall.invoke(ElementMatchers.named(targetMethodName))
                    .onField(targetField)
                    .withAllArguments();
        }
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.registerModule(new ParanamerModule());
    }

    public static final class FirstParamRequestBodyAdvice {
        
        public static ObjectMapper getMapper() {
            return mapper;
        }

        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(value = 0, readOnly = false) Object body,
                          @Advice.This RestfulController<?, ?> instance) {
            try {
                final ObjectMapper mapper = getMapper();
                String json = mapper.writeValueAsString(body);
                Class<?> genericClass = (Class) ((ParameterizedType) instance.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
                body = mapper.readValue(json, genericClass);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static final class SecondParamRequestBodyAdvice {
        
        public static ObjectMapper getMapper() {
            return mapper;
        }

        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(value = 1, readOnly = false) Object body,
                          @Advice.This RestfulController<?, ?> instance) {
            try {
                final ObjectMapper mapper = getMapper();
                String json = mapper.writeValueAsString(body);
                Class<?> genericClass = (Class) ((ParameterizedType) instance.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
                body = mapper.readValue(json, genericClass);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
