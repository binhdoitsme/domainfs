package com.hanu.domainfs.frontend.models.components;

import java.util.Map;

class Button extends LeafViewComponent {

    public Button(String content) {
        super("ViewLib.Button", content);
    }

    public Button(String content, Map<String, Object> attributes) {
        super("ViewLib.Button", content, attributes, false);
    }
}
