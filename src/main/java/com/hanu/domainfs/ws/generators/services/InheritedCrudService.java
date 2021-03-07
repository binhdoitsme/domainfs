package com.hanu.domainfs.ws.generators.services;

import java.util.Collection;

import com.hanu.domainfs.ws.generators.models.Page;
import com.hanu.domainfs.ws.generators.models.PagingModel;

public interface InheritedCrudService<T> 
        extends CrudService<T> {
    Collection<T> getEntityListByType(String type);
    Page<T> getEntityListByTypeAndPage(String type, PagingModel pagingModel);
}