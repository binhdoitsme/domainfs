package com.hanu.domainfs.ws.generators.impl;

import java.util.Arrays;

import net.bytebuddy.implementation.Implementation;

public class MethodDef {
    private String name;
    private Class<?> returnType;
    private Class<?>[] parameters;
    private Implementation body;

    public MethodDef(String name, Class<?> returnType,
            Class<?>[] params, Implementation body) {
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
