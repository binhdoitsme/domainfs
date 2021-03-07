package com.hanu.domainfs.ws.generators.controllers;

import java.lang.reflect.ParameterizedType;

import com.hanu.domainfs.ws.generators.models.Identifier;
import com.hanu.domainfs.ws.generators.models.Page;
import com.hanu.domainfs.ws.generators.models.PagingModel;
import com.hanu.domainfs.ws.generators.services.CrudService;

/**
 * Default implementation of {@link #RestfulController}
 */
@SuppressWarnings("unchecked")
public abstract class DefaultRestfulController<T> implements RestfulController<T> {

    private final Class<T> genericType = 
        (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];

    protected Class<T> getGenericType() {
        return genericType;
    }

    protected <U> CrudService<U> getServiceOfGenericType(Class<U> cls) {
        return ServiceRegistry.getInstance().get(cls.getName());
    }

    @Override
    public T createEntity(T inputEntity) {
        return getServiceOfGenericType(genericType)
            .createEntity(inputEntity);
    }

    @Override
    public Page<T> getEntityListByPage(PagingModel pagingModel) {
        return getServiceOfGenericType(genericType)
            .getEntityListByPage(pagingModel);
    }

    @Override
    public T getEntityById(Identifier<?> id) {
        return getServiceOfGenericType(genericType)
            .getEntityById(id);
    }

    @Override
    public T updateEntity(Identifier<?> id, T updatedInstance) {
        return getServiceOfGenericType(genericType)
            .updateEntity(id, updatedInstance);
    }

    @Override
    public void deleteEntityById(Identifier<?> id) {
        getServiceOfGenericType(genericType).deleteEntityById(id);
    }

}
