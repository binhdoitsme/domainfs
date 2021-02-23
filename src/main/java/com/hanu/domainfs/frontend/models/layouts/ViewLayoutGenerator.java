package com.hanu.domainfs.frontend.models.layouts;

import java.util.Map;

import com.hanu.domainfs.frontend.models.ViewLayout;

public final class ViewLayoutGenerator {
    public static ViewLayout generate(String type, 
        Map<String, Object> attributes, String content, Object... extras) {
        
        switch (type) {
            case "modal":
            default: return null;
        }
    }
}
