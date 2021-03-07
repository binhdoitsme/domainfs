package com.hanu.domainfs.ws.generators.controllers;

import com.hanu.domainfs.ws.generators.annotations.ResourceController;
import com.hanu.domainfs.ws.generators.annotations.PagingCondition;
import com.hanu.domainfs.ws.generators.annotations.Retrieve;
import com.hanu.domainfs.ws.generators.annotations.Subtype;
import com.hanu.domainfs.ws.generators.models.Page;
import com.hanu.domainfs.ws.generators.models.PagingModel;

@ResourceController
public interface RestfulWithInheritanceController<T>
        extends RestfulController<T> {
    /**
     * Retrieve a paginated list of entities of type T by one of its subtype
     * (if specified).
     * @param page
     * @param count
     */
    @Retrieve
    Page<T> getEntityListByTypeAndPage(
        @Subtype String type, @PagingCondition PagingModel pagingModel);

    /**
     * Retrieve a paginated list of entities of type T.
     * @param pageNumber
     * @param count
     */
    @Retrieve(ignored = true)
    @Override
    Page<T> getEntityListByPage(@PagingCondition PagingModel pagingModel);
}
