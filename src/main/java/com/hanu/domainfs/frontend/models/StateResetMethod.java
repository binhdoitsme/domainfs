package com.hanu.domainfs.frontend.models;

import java.util.Map;

public class StateResetMethod extends InstanceMethod {

    private final Map<String, Object> initialStates;

    public StateResetMethod(Map<String, Object> initialStates) {
        super("resetState", null);
        this.initialStates = initialStates;
    }

    @Override
    public SourceSegment getBody() {
        return new SelfMethodCall("setState", initialStates);
    }
    
}
