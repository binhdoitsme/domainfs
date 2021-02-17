package com.hanu.domainfs.ws.generators.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hanu.domainfs.ws.generators.models.Page;

import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.model.query.Expression.Op;
import domainapp.softwareimpl.SoftwareImpl;

@SuppressWarnings({ "unchecked" })
public class InheritedDomServiceAdapter<T, ID extends Serializable> extends SimpleDomServiceAdapter<T, ID>
        implements InheritedCrudService<T, ID> {

    private Map<String, String> subtypes;

    // autowired constructor
    // adapts SoftwareImpl to Service
    public InheritedDomServiceAdapter(final SoftwareImpl sw, final Map<String, String> subtypes) {
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
            if (type == null) {
                Collection<T> collection = new LinkedList<>();
                for (String subtype : subtypes.keySet()) {
                    Class<T> cls = (Class<T>) Class.forName(subtype);
                    collection.addAll(sw.retrieveObjects(cls, "id", Op.GT, "0"));
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
    public Page<T> getEntityListByTypeAndPage(String type, int page, int count) {
        // TODO ignoring page and count for ease of development
        Collection<T> retrieved = getEntityListByType(type);
        return new Page<>(1, 1, retrieved);
    }

}
