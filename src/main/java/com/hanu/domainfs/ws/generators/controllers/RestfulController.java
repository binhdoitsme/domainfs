package com.hanu.domainfs.ws.generators.controllers;

import java.io.Serializable;

import com.hanu.domainfs.ws.generators.models.Page;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Represent a RESTful Web API endpoint.
 * Operations are grouped as C-R-U-D
 * @param T the generic type
 */
public interface RestfulController<T, ID extends Serializable> {
    /**
     * Create a new entity based on submitted input.
     * @param inputEntity
     * @return the persisted entity
     */
    T createEntity(@RequestBody T inputEntity);

    /**
     * Retrieve a paginated list of entities of type T.
     * @param pageNumber
     * @param count
     */
    Page<T> getEntityListByPage(
        @RequestParam(value = "page", defaultValue =  "1") int pageNumber,
        @RequestParam(value = "count", defaultValue = "20") int count);

    /**
     * Retrieve an entity instance by its identifier.
     * @param id
     */
    T getEntityById(@RequestParam("id") ID id);

    /**
     * Update an entity instance
     * @param id
     * @param updatedInstance
     */
    T updateEntity(@PathVariable("id") ID id, @RequestBody T updatedInstance);


    /**
     * Delete an entity instance
     * @param id
     */
    void deleteEntityById(@PathVariable("id") ID id);
}
