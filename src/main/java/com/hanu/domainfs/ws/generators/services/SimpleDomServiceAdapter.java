package com.hanu.domainfs.ws.generators.services;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.hanu.domainfs.utils.IdentifierUtils;
import com.hanu.domainfs.ws.generators.models.Identifier;
import com.hanu.domainfs.ws.generators.models.Page;
import com.hanu.domainfs.ws.generators.models.PagingModel;

import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.basics.model.query.Expression.Op;
import domainapp.softwareimpl.SoftwareImpl;

@SuppressWarnings("unchecked")
public class SimpleDomServiceAdapter<T> implements CrudService<T> {
    protected final SoftwareImpl sw;
    protected Class<T> type;

    // autowired constructor
    protected SimpleDomServiceAdapter(SoftwareImpl sw) {
        this(null, sw);
    }

    public SimpleDomServiceAdapter(Class<T> type, SoftwareImpl sw) {
        this.type = type;
        this.sw = sw;
    }

    public Class<T> getType() {
        return type;
    }

    protected void setType(Class<T> type) {
        this.type = type;
    }

    @Override
    public T createEntity(T entity) {
        try {
            sw.addObject((Class<T>) entity.getClass(), entity);
            return entity;
        } catch (DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T getEntityById(Identifier<?> id) {
        try {
            return sw.retrieveObjectById(type, id.getId());
        } catch (NotFoundException | DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<T> getEntityListByPage(PagingModel pagingModel) {
        Collection<T> entities = this.getAllEntities();
        return paginate(entities, pagingModel);
    }

    protected Page<T> paginate(Collection<T> entities, PagingModel pagingModel) {
        final int pageNumber = pagingModel.getPage();
        final int itemPerPage = pagingModel.getCount();

        if (entities == null || entities.isEmpty()) {
            return Page.empty();
        }
        final int size = entities.size();
        final int skip = (pageNumber - 1) * itemPerPage;
        if (skip > size) {
            throw new NoSuchElementException("Not found: Page #" + pageNumber);
        }
        final int pageCount = size / itemPerPage + size % itemPerPage > 0 ? 1 : 0;
        final Collection<T> pageContent = entities.stream().skip(skip).limit(itemPerPage).collect(Collectors.toList());
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
    public T updateEntity(Identifier<?> id, T entity) {
        try {
            if (!id.getId().equals(
                IdentifierUtils.getIdField(getType()).get(entity))) return null;
            sw.updateObject(type, entity);
            return entity;
        } catch (NotPossibleException | NotFoundException
                | DataSourceException | IllegalArgumentException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntityById(Identifier<?> id) {
        try {
            T toDelete = sw.retrieveObjectById(type, id.getId());
            sw.deleteObject(toDelete, type);
        } catch (DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

}
