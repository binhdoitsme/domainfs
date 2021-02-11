package com.hanu.domainfs.ws.generators.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.hanu.domainfs.ws.generators.models.Page;

import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.model.query.Expression.Op;
import domainapp.softwareimpl.SoftwareImpl;

public class AbstractCrudService<T, ID extends Serializable> 
        implements CrudService<T, ID> {
    protected final SoftwareImpl sw;
    private final Class<T> type;

    // autowired constructor
    protected AbstractCrudService(SoftwareImpl sw) {
        this(null, sw);
    }

    public AbstractCrudService(Class<T> type, SoftwareImpl sw) {
        this.type = type;
        this.sw = sw;
    }

    @Override
    public T createEntity(T entity) {
        try {
            sw.addObject(type, entity);
            return entity;
        } catch (DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T getEntityById(ID id) {
        try {
            return sw.retrieveObjectById(type, id);
        } catch (NotFoundException | DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<T> getEntityListByPage(int pageNumber, int itemPerPage) {
        Collection<T> entities = getAllEntities();
        final int size = entities.size();
        if (pageNumber * itemPerPage < size) {
            throw new NoSuchElementException("Not found");
        }
        final int skip = (pageNumber - 1) * itemPerPage;
        final int pageCount = size / itemPerPage + size % itemPerPage > 0 ? 1 : 0;
        final Collection<T> pageContent = entities.stream().skip(skip)
                    .limit(itemPerPage).collect(Collectors.toList());
        return new Page<>(pageNumber, pageCount, pageContent);
    }

    @Override
    public Collection<T> getAllEntities() {
        try {
            return sw.retrieveObjects(type, "id", Op.GT, "0");
        } catch (NotFoundException | DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T updateEntity(T entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteEntityById(ID id) {
        try {
            T toDelete = sw.retrieveObjectById(type, id);
            sw.deleteObject(toDelete, type);
        } catch (DataSourceException e) {
            throw new RuntimeException(e);
        }
    }
    
}
