package com.hanu.domainfs.ws.generators.controllers;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hanu.domainfs.ws.generators.services.CrudService;

@SuppressWarnings({"rawtypes"})
public final class ServiceRegistry {
    private static ServiceRegistry INSTANCE;
    public static ServiceRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServiceRegistry();
        }
        return INSTANCE;
    }

    private final Map<String, CrudService> serviceTypeMap;

    private ServiceRegistry() {
        this.serviceTypeMap = new ConcurrentHashMap<>();
    }

    public CrudService get(String type) {
        for (Map.Entry<String, CrudService> entry : serviceTypeMap.entrySet()) {
            if (entry.getKey().toLowerCase(Locale.ROOT).contains(type.toLowerCase(Locale.ROOT))) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void put(String genericType, CrudService serviceInstance) {
        this.serviceTypeMap.put(genericType, serviceInstance);
    }
}
