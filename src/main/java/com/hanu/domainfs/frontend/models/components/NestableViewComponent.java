package com.hanu.domainfs.frontend.models.components;

import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.ImplementationStrategy;
import com.hanu.domainfs.frontend.models.SourceSegment;
import com.hanu.domainfs.frontend.models.ViewComponent;

abstract class NestableViewComponent implements ViewComponent {
    private final String tagName;
    private String content;
    private Map<String, Object> attributes;
    private List<ViewComponent> innerComponents;
    private final boolean contentFirst;

    public NestableViewComponent(String tagName, String content, 
            Map<String, Object> attributes, List<ViewComponent> innerComponents, boolean contentFirst) {
        this.tagName = tagName;
        this.content = content;
        this.attributes = attributes;
        this.innerComponents = innerComponents;
        this.contentFirst = contentFirst;
    }

    @Override
    public String tagName() {
        return tagName;
    }

    @Override
    public Map<String, Object> attributes() {
        return attributes;
    }

    @Override
    public String content() {
        return content;
    }

    @Override
    public List<ViewComponent> innerComponents() {
        return innerComponents;
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return new Implementation();
    }

    private class Implementation implements ImplementationStrategy {
        @Override
        public String implement(SourceSegment src) {
            if (!(src instanceof ViewComponent))
                throw new UnsupportedOperationException();
            ViewComponent component = (ViewComponent) src;
            final String openTag = String.format("<%s %s>", component.tagName(),
                    AttributesUtils.attributesToString(component.attributes()));
            final String closingTag = String.format("</%s>", component.tagName());
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(openTag).append("\n");
            if (contentFirst) stringBuilder.append("  ").append(content);
            for (ViewComponent inner : innerComponents) {
                String[] lines = inner.toSourceCode().trim().split("\n");
                for (String line : lines) {
                    stringBuilder.append("  ").append(line);
                }
            }
            if (!contentFirst) stringBuilder.append("  ").append(content);
            stringBuilder.append(closingTag);
            return stringBuilder.toString();
        }
    };

}
