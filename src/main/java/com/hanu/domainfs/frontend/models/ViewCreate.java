package com.hanu.domainfs.frontend.models;

import java.util.List;
import java.util.Map;

public abstract class ViewCreate extends AbstractViewClass {
    private String viewLib;

    public ViewCreate(String name, String viewLib, 
                      Map<String, Object> initialStates,
                      List<SourceSegment> methods) {
        super(name, new StateInitializer(initialStates), methods);
        this.viewLib = viewLib;
    }

    @Override
    public List<SourceSegment> getImportStatements() {
        return List.of(
            new ViewLibImportStatement(this.viewLib)
        );
    }

    @Override
    public ViewLayout getLayout() {
        // TODO Auto-generated method stub
        return null;
    }
}
