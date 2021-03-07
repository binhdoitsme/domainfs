package com.hanu.domainfs.ws.generators.controllers;

import java.util.Collection;
import java.util.Map;

import com.hanu.domainfs.ws.generators.annotations.Create;
import com.hanu.domainfs.ws.generators.annotations.ID;
import com.hanu.domainfs.ws.generators.annotations.Modifying;
import com.hanu.domainfs.ws.generators.annotations.Retrieve;
import com.hanu.domainfs.ws.generators.models.Identifier;

/**
 * Represent a nested (level-1) resource endpoint.
 * @param <TOuter> the outer type
 * @param <TInner> the inner (nested) type
 */
public interface NestedRestfulController<TOuter, TInner> {

    /**
     * Create an object instance of the inner type as owned by the outer instance.
     * @param outerId
     */
    @Create
    TInner createInner(@ID Identifier<?> outerId,
                       @Modifying Map<String, Object> requestBody);

    /**
     * Retrieve a list of inner object instances owned by the outer.
     * @param outerId
     */
    @Retrieve
    Collection<TInner> getInnerListByOuterId(@ID Identifier<?> outerId);
}
