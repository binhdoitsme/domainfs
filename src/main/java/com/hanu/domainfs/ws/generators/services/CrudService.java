package com.hanu.domainfs.ws.generators.services;

import java.io.Serializable;
import java.util.Collection;

import com.hanu.domainfs.ws.generators.models.Page;

public interface CrudService<T, ID extends Serializable> {
    T createEntity(T entity);
    T getEntityById(ID id);
    Page<T> getEntityListByPage(int pageNumber, int itemPerPage);
    Collection<T> getAllEntities();
    T updateEntity(T entity);
    void deleteEntityById(ID id);
}
