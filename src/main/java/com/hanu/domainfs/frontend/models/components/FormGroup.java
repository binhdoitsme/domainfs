package com.hanu.domainfs.frontend.models.components;

import java.util.List;

import com.hanu.domainfs.frontend.models.ViewComponent;

class FormGroup extends NestableViewComponent {

    public FormGroup(List<ViewComponent> innerComponents) {
        super("FormGroup", null, null, innerComponents, false);
    }

    public FormGroup() {
        this(null);
    }
    
}
