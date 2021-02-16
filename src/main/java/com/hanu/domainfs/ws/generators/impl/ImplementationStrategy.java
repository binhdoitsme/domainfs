package com.hanu.domainfs.ws.generators.impl;

import java.util.List;

/**
 * Define a common interface between implementation strategies.
 */
public interface ImplementationStrategy {
    List<MethodDef> implementMethods(Class<?> abstractType);

    /**
     * Get a strategy instance by its name and construct it by {@link#args}.
     * @param name name of the strategy
     * @param args arguments to construct the strategy
     * @return the strategy
     */
    public static ImplementationStrategy getStrategy(String name, Object... args) {
        switch (name.toLowerCase()) {
            case "restful": return new RestfulImplStrategy((String) args[0]);
            default: throw new IllegalArgumentException("Invalid implementation strategy: " + name);
        }
    }
}
