package com.hanu.domainfs.ws.generators.controllers;

import java.io.Serializable;

import com.hanu.domainfs.ws.generators.models.Page;

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
    T createEntity(T inputEntity);

    /**
     * Retrieve a paginated list of entities of type T.
     * @param pageNumber
     * @param count
     */
    Page<T> getEntityListByPage(int pageNumber, int count);

    /**
     * Retrieve an entity instance by its identifier.
     * @param id
     */
    T getEntityById(ID id);

    /**
     * Update an entity instance
     * @param id
     * @param updatedInstance
     */
    T updateEntity(ID id, T updatedInstance);


    /**
     * Delete an entity instance
     * @param id
     */
    void deleteEntityById(ID id);
}
