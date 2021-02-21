package com.hanu.domainfs.frontend.models;

import java.util.List;
import java.util.Map;

public interface ViewComponent extends SourceSegment {
    String tagName();
    Map<String, Object> attributes();
    String content();
    List<ViewComponent> innerComponents();

    @Override
    default String toSourceCode() {
        return getImplementationStrategy().implement(this);
    }
}
