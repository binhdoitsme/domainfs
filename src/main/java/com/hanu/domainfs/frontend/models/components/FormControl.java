package com.hanu.domainfs.frontend.models.components;

import java.util.Map;

class FormControl extends LeafViewComponent {

    public FormControl(InputType type, Map<String, Object> attributes) {
        super("FormControl", null, attributes, true);
        addAttribute("type", type.toTypeString());
    }
    
}