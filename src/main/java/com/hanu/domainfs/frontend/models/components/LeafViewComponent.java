package com.hanu.domainfs.frontend.models.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.ImplementationStrategy;
import com.hanu.domainfs.frontend.models.SourceSegment;
import com.hanu.domainfs.frontend.models.ViewComponent;

abstract class LeafViewComponent implements ViewComponent {
    private final String tagName;
    private final boolean isSingleTag;
    private String content;
    private Map<String, Object> attributes;

    public LeafViewComponent(String tagName, String content) {
        this(tagName, content, null, false);
    }

    public LeafViewComponent(String tagName, String content, 
                             Map<String, Object> attributes,
                             boolean isSingleTag) {
        this.tagName = tagName;
        this.isSingleTag = isSingleTag;
        if (content == null) this.content = "";
        else this.content = content;
        if (attributes == null) this.attributes = new HashMap<>();
        else this.attributes = attributes;
    }

    protected void addAttribute(String key, Object value) {
        attributes.put(key, value);
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
    public ImplementationStrategy getImplementationStrategy() {
        return new Implementation();
    }

    @Override
    public List<ViewComponent> innerComponents() { return null; }

    private class Implementation implements ImplementationStrategy {
        @Override
        public String implement(SourceSegment src) {
            if (!(src instanceof ViewComponent))
                throw new UnsupportedOperationException();
            ViewComponent component = (ViewComponent) src;
            if (isSingleTag) {
                return String.format("<%s %s />", component.tagName(),
                    AttributesUtils.attributesToString(component.attributes()));
            }
            return String.format("<%s %s>%s</%s>", component.tagName(),
                    AttributesUtils.attributesToString(component.attributes()), component.content(),
                    component.tagName());
        }
    };
}
