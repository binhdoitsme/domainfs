package com.hanu.domainfs.frontend.models.components;

import java.util.Map;

class FormControl extends LeafViewComponent {

    public FormControl(InputType type, Map<String, Object> attributes) {
        super(toTagName(type), null, attributes, true);
        addAttribute("type", type.toTypeString());
    }

    private static String toTagName(InputType type) {
        if (type == InputType.Type.CHECKBOX) {
            return "Form.Check";
        } else {
            return "FormControl";
        }
    }
    
}