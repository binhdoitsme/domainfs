package com.hanu.domainfs.frontend.models;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface ViewComponent extends SourceSegment {
    String tagName();
    Map<String, Object> attributes();
    String content();
    List<ViewComponent> innerComponents();

    public static void traverseWithSideEffects(
            ViewComponent vc, Consumer<ViewComponent> sideEffect) {
        sideEffect.accept(vc);
        if (vc.innerComponents() == null || vc.innerComponents().isEmpty()) return;
        for (ViewComponent inner : vc.innerComponents()) {
            traverseWithSideEffects(inner, sideEffect);
        }
    }

    @Override
    default String toSourceCode() {
        return getImplementationStrategy().implement(this);
    }
}
