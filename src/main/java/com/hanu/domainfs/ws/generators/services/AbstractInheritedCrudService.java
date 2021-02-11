package com.hanu.domainfs.ws.generators.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.hanu.domainfs.ws.generators.models.Page;

import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.model.query.Expression.Op;
import domainapp.softwareimpl.SoftwareImpl;

@SuppressWarnings({ "unchecked" })
public class AbstractInheritedCrudService<T, ID extends Serializable> extends AbstractCrudService<T, ID>
        implements InheritedCrudService<T, ID> {

    protected final Map<String, String> subtypes;

    // autowired constructor
    public AbstractInheritedCrudService(final SoftwareImpl sw, final Map<String, String> subtypes) {
        super(sw);
        this.subtypes = subtypes;
    }

    @Override
    public Collection<T> getEntityListByType(String type) {
        try {
            String fqTypeName = subtypes.get(type);
            Class<T> cls = (Class<T>) Class.forName(fqTypeName);
            return sw.retrieveObjects(cls, "id", Op.GT, "0");
        } catch (ClassNotFoundException | DataSourceException e) {
            throw new RuntimeException(e);
        }        
    }

    @Override
    public Page<T> getEntityListByTypeAndPage(String type, int page, int count) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
