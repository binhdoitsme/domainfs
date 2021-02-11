package com.hanu.domainfs.ws.generators.services;

import java.io.Serializable;
import java.util.Collection;

import com.hanu.domainfs.ws.generators.models.Page;

public interface InheritedCrudService<T, ID extends Serializable> 
        extends CrudService<T, ID> {
    Collection<T> getEntityListByType(String type);
    Page<T> getEntityListByTypeAndPage(String type, int page, int count);
}