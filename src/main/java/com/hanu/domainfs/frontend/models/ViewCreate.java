package com.hanu.domainfs.frontend.models;

import java.util.List;
import java.util.Map;

public class ViewCreate extends AbstractViewClass {
    private String viewLib;
    private final ViewLayout defaultLayout;

    public ViewCreate(String name, String viewLib, 
                      ViewLayout defaultLayout,
                      Map<String, Object> initialStates,
                      List<SourceSegment> methods) {
        super(name, new StateInitializer(initialStates), methods);
        this.viewLib = viewLib;
        this.defaultLayout = defaultLayout;
    }

    @Override
    public List<SourceSegment> getImportStatements() {
        return List.of(
            new ViewLibImportStatement(this.viewLib)
        );
    }

    @Override
    public ViewLayout getLayout() {
        return defaultLayout;
    }

    @Override
    public String getTarget() {
        // TODO Auto-generated method stub
        return null;
    }
}
