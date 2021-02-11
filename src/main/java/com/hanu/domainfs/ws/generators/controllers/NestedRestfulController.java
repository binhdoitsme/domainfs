package com.hanu.domainfs.ws.generators.controllers;

import java.io.Serializable;

import com.hanu.domainfs.ws.generators.models.Page;

/**
 * Represent a nested (level-1) resource endpoint.
 * @param TOuter the outer type
 * @param TInner the inner (nested) type
 */
public interface NestedRestfulController
        <TOuter, IDOuter extends Serializable, 
         TInner, IDInner extends Serializable> {
    
    /**
     * Create an object instance of the inner type as owned by the outer instance.
     * @param outerId
     * @param arguments
     */
    TInner createInner(IDOuter outerId, TInner body);

    /**
     * Retrieve a list of inner object instances owned by the outer.
     * @param outerId
     */
    Page<TInner> getInnerListByOuterId(IDOuter outerId);
}
