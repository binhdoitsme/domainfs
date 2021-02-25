package com.hanu.domainfs.frontend.models.components;

import java.util.Map;

class FormLabel extends LeafViewComponent {

    public FormLabel(String content, Map<String, Object> attributes) {
        super("Form.Label", content, attributes, false);
    }
    
}
