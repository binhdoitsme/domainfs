package com.hanu.domainfs.ws.generators.controllers;

import com.hanu.domainfs.ws.generators.annotations.Create;
import com.hanu.domainfs.ws.generators.annotations.Delete;
import com.hanu.domainfs.ws.generators.annotations.ID;
import com.hanu.domainfs.ws.generators.annotations.Modifying;
import com.hanu.domainfs.ws.generators.annotations.PagingCondition;
import com.hanu.domainfs.ws.generators.annotations.ResourceController;
import com.hanu.domainfs.ws.generators.annotations.Retrieve;
import com.hanu.domainfs.ws.generators.annotations.Update;
import com.hanu.domainfs.ws.generators.models.Identifier;
import com.hanu.domainfs.ws.generators.models.Page;
import com.hanu.domainfs.ws.generators.models.PagingModel;

/**
 * Represent a RESTful Web API endpoint.
 * Operations are grouped as C-R-U-D
 * @param T the generic type
 */
@ResourceController
public interface RestfulController<T> {
    /**
     * Create a new entity based on submitted input.
     * @param inputEntity
     * @return the persisted entity
     */
    @Create
    T createEntity(@Modifying T inputEntity);

    /**
     * Retrieve a paginated list of entities of type T.
     * @param pageNumber
     * @param count
     */
    @Retrieve 
    Page<T> getEntityListByPage(@PagingCondition PagingModel pagingModel);

    /**
     * Retrieve an entity instance by its identifier.
     * @param id
     */
    @Retrieve(byId = true)
    T getEntityById(@ID Identifier<?> id);

    /**
     * Update an entity instance
     * @param id
     * @param updatedInstance
     */
    @Update(byId = true)
    T updateEntity(@ID Identifier<?> id, @Modifying T updatedInstance);


    /**
     * Delete an entity instance
     * @param id
     */
    @Delete(byId = true)
    void deleteEntityById(@ID Identifier<?> id);
}
