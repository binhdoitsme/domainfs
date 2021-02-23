package com.hanu.domainfs.frontend.models.components;

import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.ViewComponent;

class Form extends NestableViewComponent {

    public Form(String content, Map<String, Object> attributes, List<ViewComponent> innerComponents,
            boolean contentFirst) {
        super("ViewLib.Form", content, attributes, innerComponents, contentFirst);
    }
    
}
