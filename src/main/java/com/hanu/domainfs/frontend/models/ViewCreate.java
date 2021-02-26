package com.hanu.domainfs.frontend.models;

import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.components.ViewComponentGenerator;

public class ViewCreate extends AbstractViewClass {
    private List<SourceSegment> imports;

    public ViewCreate(String name, String superclass, 
                      Map<String, Object> initialStates,
                      List<SourceSegment> methods,
                      List<SourceSegment> imports) {
        super(name, superclass, new StateInitializer(initialStates), methods);
        this.imports = imports;
    }

    @Override
    public List<SourceSegment> getImportStatements() {
        return this.imports;
    }

    @Override
    public ViewLayout getLayout() {
        return ViewComponentGenerator.emptyRoot;
    }

    @Override
    public String getTarget() {
        // TODO Auto-generated method stub
        return null;
    }
}
