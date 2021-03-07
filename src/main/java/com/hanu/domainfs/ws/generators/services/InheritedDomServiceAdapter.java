package com.hanu.domainfs.ws.generators.services;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.hanu.domainfs.ws.generators.models.Page;
import com.hanu.domainfs.ws.generators.models.PagingModel;

import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.model.query.Expression.Op;
import domainapp.softwareimpl.SoftwareImpl;

@SuppressWarnings({ "unchecked" })
public class InheritedDomServiceAdapter<T> extends SimpleDomServiceAdapter<T>
        implements InheritedCrudService<T> {

    private Map<String, String> subtypes;

    // autowired constructor
    // adapts SoftwareImpl to Service
    public InheritedDomServiceAdapter(final SoftwareImpl sw,
                                      final Map<String, String> subtypes) {
        super(sw);
        this.subtypes = subtypes;
    }

    public void setSubtypes(Map<String, String> subtypes) {
        this.subtypes = subtypes;
    }

    @Override
    public Collection<T> getAllEntities() {
        List<T> result = new LinkedList<>();
        for (String subtypeId : subtypes.keySet()) {
            result.addAll(getEntityListByType(subtypeId));
        }
        return result;
    }

    @Override
    public Collection<T> getEntityListByType(String type) {
        try {
            if (type == null || type.isEmpty()) {
                Collection<T> collection = new LinkedList<>();
                for (String subtype : subtypes.keySet()) {
                    Class<T> cls = (Class<T>) Class.forName(subtypes.get(subtype));
                    Collection<T> subtypeList = sw.retrieveObjects(cls, "id", Op.GT, "0");
                    if (subtypeList != null) collection.addAll(subtypeList);
                }
                return collection;
            }
            String fqTypeName = subtypes.get(type);
            Class<T> cls = (Class<T>) Class.forName(fqTypeName);
            return sw.retrieveObjects(cls, "id", Op.GT, "0");
        } catch (ClassNotFoundException | DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<T> getEntityListByTypeAndPage(String type, PagingModel pagingModel) {
        Collection<T> entities = getEntityListByType(type);
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

}
