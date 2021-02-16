package com.hanu.domainfs.ws.generators.controllers;

import com.hanu.domainfs.ws.generators.models.Page;

import java.io.Serializable;

public interface RestfulWithInheritanceController<T, ID extends Serializable>
        extends RestfulController<T, ID> {
    /**
     * Retrieve a paginated list of entities of type T by one of its subtype
     * (if specified).
     * @param page
     * @param count
     */
    Page<T> getEntityListByTypeAndPage(String type, int page, int count);
}
