package com.hanu.domainfs.frontend.models;

public class DefaultInstanceMethod extends InstanceMethod {
    private SourceSegment body;

    public DefaultInstanceMethod(String name, String[] paramNames, SourceSegment body) {
        super(name, paramNames);
        this.body = body;
    }

    @Override
    public SourceSegment getBody() {
        return body;
    }
}
