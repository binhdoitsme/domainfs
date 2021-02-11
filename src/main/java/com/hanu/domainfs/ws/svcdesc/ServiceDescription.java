package com.hanu.domainfs.ws.svcdesc;

/**
 * Represent the description of a Web Service
 * @author binh_dh
 */
final class ServiceDescription {
    private final String name;
    private final String endpoint;

    private ServiceDescription(String name, String endpoint) {
        this.name = name;
        this.endpoint = endpoint;
    }

    public String getName() {
        return name;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public static ServiceDescription from(ServiceController annotation) {
        if (annotation == null) return null;
        return new ServiceDescription(annotation.name(), annotation.endpoint());
    }
}
