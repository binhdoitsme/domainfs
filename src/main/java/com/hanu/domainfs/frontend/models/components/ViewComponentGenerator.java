package com.hanu.domainfs.frontend.models.components;

import java.util.Map;

import com.hanu.domainfs.frontend.models.ViewComponent;

public final class ViewComponentGenerator {
    public static ViewComponent generate(String type, 
            Map<String, Object> attributes, String content, Object... extras) {
        switch (type) {
            case "button":
                return new Button(content, attributes);
            case "input":
                return new FormControl((InputType)extras[0], attributes);
            case "label":
                return new FormLabel(content, attributes);
            default:
                return null;
        }
    }
}
