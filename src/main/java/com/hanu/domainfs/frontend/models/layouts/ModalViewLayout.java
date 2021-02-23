package com.hanu.domainfs.frontend.models.layouts;

import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.ImplementationStrategy;
import com.hanu.domainfs.frontend.models.ViewComponent;
import com.hanu.domainfs.frontend.models.ViewLayout;

class ModalViewLayout implements ViewLayout {

    @Override
    public String tagName() { return "ViewLib.Modal"; }

    @Override
    public Map<String, Object> attributes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String content() { return null; }

    @Override
    public List<ViewComponent> innerComponents() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void add(ViewComponent component) {
        // TODO Auto-generated method stub
        
    }
    
}
