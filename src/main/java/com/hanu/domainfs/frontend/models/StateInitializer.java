package com.hanu.domainfs.frontend.models;

import java.util.Map;

public final class StateInitializer extends FieldSetter {

    public StateInitializer(Map<String, Object> initialStates) {
        super(new FieldReference("state", "this"), new ObjectRep(initialStates));
    }

}
