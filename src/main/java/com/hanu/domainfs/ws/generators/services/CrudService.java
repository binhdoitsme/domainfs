package com.hanu.domainfs.ws.generators.services;

import java.util.Collection;

import com.hanu.domainfs.ws.generators.models.Identifier;
import com.hanu.domainfs.ws.generators.models.Page;
import com.hanu.domainfs.ws.generators.models.PagingModel;

public interface CrudService<T> {
    T createEntity(T entity);
    T getEntityById(Identifier<?> id);
    Page<T> getEntityListByPage(PagingModel pagingModel);
    Collection<T> getAllEntities();
    T updateEntity(Identifier<?> id, T entity);
    void deleteEntityById(Identifier<?> id);
}
