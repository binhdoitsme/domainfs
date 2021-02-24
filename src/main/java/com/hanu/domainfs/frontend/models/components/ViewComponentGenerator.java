package com.hanu.domainfs.frontend.models.components;

import java.util.Map;

import com.hanu.domainfs.frontend.models.ViewComponent;

public final class ViewComponentGenerator {

    /**
     * @param extras contain extra information to generate an object of a view component type
     */
    public static ViewComponent generate(String type, 
            Map<String, Object> attributes, Object... extras) {
        switch (type) {
            case "button":
                return new Button((String) extras[0], attributes);
            case "input":
                return new FormControl((InputType)extras[0], attributes);
            case "label":
                return new FormLabel((String) extras[0], attributes);
            case "modal":
                return Modal.createDefault((String) extras[0], attributes);
            default:
                return null;
        }
    }
}
