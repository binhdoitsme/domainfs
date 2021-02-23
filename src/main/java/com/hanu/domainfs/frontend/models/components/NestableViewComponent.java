package com.hanu.domainfs.frontend.models.components;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.ImplementationStrategy;
import com.hanu.domainfs.frontend.models.IndentationUtils;
import com.hanu.domainfs.frontend.models.SourceSegment;
import com.hanu.domainfs.frontend.models.ViewComponent;
import com.hanu.domainfs.frontend.models.ViewLayout;

abstract class NestableViewComponent implements ViewLayout, ImplementationStrategy {
    private final String tagName;
    private String content;
    private Map<String, Object> attributes;
    private List<ViewComponent> innerComponents;
    private final boolean contentFirst;

    public NestableViewComponent(String tagName, String content, 
            Map<String, Object> attributes, List<ViewComponent> innerComponents, boolean contentFirst) {
        this.tagName = tagName;        
        this.content = content != null ? content : "";
        this.attributes = attributes == null ? new LinkedHashMap<>() : attributes;
        this.innerComponents = innerComponents == null ? 
            new LinkedList<>() : new LinkedList<>(innerComponents);
        this.contentFirst = contentFirst;
    }

    public NestableViewComponent(String tagName, String content) {
        this(tagName, content, new LinkedHashMap<>(), new LinkedList<>(), false);
    }

    protected void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public void add(ViewComponent component) {
        innerComponents.add(component);
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
        return this;
    }
    
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
        if (contentFirst && !content.isEmpty())
            stringBuilder.append(IndentationUtils.indentStringBy(1, content));
        for (ViewComponent inner : innerComponents) {
            stringBuilder.append(IndentationUtils.indentStringBy(1, inner.toSourceCode()));
        }
        if (!contentFirst && !content.isEmpty())
            stringBuilder.append(IndentationUtils.indentStringBy(1, content));
        stringBuilder.append(closingTag);
        return stringBuilder.toString();
    }
}
