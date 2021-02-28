package com.hanu.domainfs.frontend.models.components;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.ViewComponent;

class FormSelect extends NestableViewComponent {

    public FormSelect(Map<String, Object> attributes, Object... options) {
        super("Form.Control", attributes, toOptionViewComponents(options));
        addAttribute("as", "select");
    }

    private static List<ViewComponent> toOptionViewComponents(Object[] options) {
        final List<ViewComponent> optionViewList = new LinkedList<>();
        for (Object option : options) {
            ViewComponent optionView = new LeafViewComponent(
                "option", option.toString(), Map.of("value", option.toString()), false) {};
            optionViewList.add(optionView);
        }
        return optionViewList;
    }

    @Override
    public void add(ViewComponent component) {
        throw new UnsupportedOperationException();
    }
}
