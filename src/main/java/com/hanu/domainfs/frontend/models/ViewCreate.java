package com.hanu.domainfs.frontend.models;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
        final Set<String> definedTagNames = new TreeSet<>();
        ViewComponent.traverseWithSideEffects(getLayout(), 
            vc -> {
                String tagName = vc.tagName();
                if (tagName.contains(".")) 
                    definedTagNames.add(tagName.split("\\.")[0]);
                else definedTagNames.add(tagName);
            });

        return List.of(
            new ImportStatement(this.viewLib, 
                definedTagNames.toArray(new String[definedTagNames.size()]))
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
