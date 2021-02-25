package com.hanu.domainfs.frontend.models.components;

import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.ViewComponent;

class Form extends NestableViewComponent {

    public Form(Map<String, Object> attributes, List<ViewComponent> innerComponents) {
        super("Form", attributes, innerComponents);
    }
    
}
