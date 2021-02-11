package com.hanu.domainfs.ws.svcdesc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The utility class looking into Web service classes and returning their
 * descriptions.
 * @author binh_dh
 */
final class ServiceDescriptor {
    private final Map<String, ServiceDescription> descriptions;

    ServiceDescriptor() {
        descriptions = new HashMap<>();
    }

    /**
     * Describe a web service class.
     */
    public ServiceDescription describe(Class<?> serviceClass) {
        ServiceController annotation = 
            serviceClass.getAnnotation(ServiceController.class);
        return ServiceDescription.from(annotation);
    }

    /**
     * Describe a list of service classes.
     */
    public List<ServiceDescription> describeList(Collection<Class<?>> serviceClasses) {
        List<ServiceDescription> serviceDescriptions = new ArrayList<>();
        for (Class<?> serviceClass : serviceClasses) {
            String fqClassName = serviceClass.getName();
            if (!descriptions.containsKey(fqClassName)) {
                ServiceDescription description = describe(serviceClass);
                if (description == null) continue;
                descriptions.put(fqClassName, description);
            }
            serviceDescriptions.add(descriptions.get(fqClassName));
        }
        return serviceDescriptions;
    }

    /**
     * Get descriptions of all annotated service controllers within a package.
     */
    public List<ServiceDescription> describePackage(String packageName) {
        try {
            List<Class<?>> serviceClasses = ClassUtils.getClasses(packageName);
            return this.describeList(serviceClasses);
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalArgumentException("Invalid package name: " + packageName);
        }
        
    }

    private static ServiceDescriptor instance;
    public static ServiceDescriptor getDescriber() {
        if (instance == null) instance = new ServiceDescriptor();
        return instance;
    }
}
