package com.hanu.domainfs.frontend.models.components;

import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.ViewComponent;
import com.hanu.domainfs.frontend.models.ViewLayout;

@SuppressWarnings({ "rawtypes", "unchecked" })
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
            case "form":
                if (extras.length != 0) 
                    return new Form(attributes, (List) extras[1]);
                else if (extras.length == 0) return new Form();
            case "formGroup":
                if (extras.length != 0) 
                    return new FormGroup((List) extras[1]);
                else if (extras.length == 0) return new FormGroup();
            default:
                return null;
        }
    }

    public static final ViewLayout emptyRoot =
            new NestableViewComponent("", null) {};
}
