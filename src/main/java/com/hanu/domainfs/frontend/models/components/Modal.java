package com.hanu.domainfs.frontend.models.components;

import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.ViewComponent;

class Modal extends NestableViewComponent {
    private final Modal.Body body;

    private Modal(String title, Map<String, Object> attributes, 
                    List<ViewComponent> innerComponents) {
        super("ViewLib.Modal", null, attributes, innerComponents, false);
        body = new Modal.Body();
        super.add(new Modal.Header(title));
        super.add(body);
        super.add(Modal.Footer.defaultFooter());
    }

    private Modal(String title, Map<String, Object> attributes) {
        this(title, attributes, List.of());
    }

    private Modal(String title, List<ViewComponent> innerComponents) {
        this(title, Map.of(), innerComponents);
    }

    @Override
    public void add(ViewComponent component) {
        body.add(component);
    }

    public static Modal createDefault(String title, Map<String, Object> attrs) {
        return new Modal(title, attrs);
    }

    private static class Header extends NestableViewComponent {
        public Header(String title, Map<String, Object> attributes) {
            super("ViewLib.Modal.Header", null, attributes, 
                List.of(
                    new Title(title)
                ), false);
            addAttribute("closeButton", true);
        }

        public Header(String title) {
            this(title, null);
        }
    }

    private static class Title extends LeafViewComponent {
        public Title(String title) {
            super("ViewLib.Modal.Title", title);
        }
    }

    private static class Body extends NestableViewComponent {
        public Body(Map<String, Object> attributes, 
                List<ViewComponent> innerComponents) {
            super("ViewLib.Modal.Body", null, attributes, innerComponents, false);
        }

        public Body() {
            super("ViewLib.Modal.Body", null);
        }
    }

    private static class Footer extends NestableViewComponent {
        public Footer(Map<String, Object> attributes, 
                List<ViewComponent> innerComponents) {
            super("ViewLib.Modal.Footer", null, attributes, innerComponents, false);
        }

        public Footer() {
            super("ViewLib.Modal.Footer", null);
        }

        static Footer defaultFooter() {
            Footer footer = new Footer();
            footer.add(new Button("Cancel", Map.of(
                "variant", "secondary",
                "size", "sm",
                "onClick", "{handleClose}"
            )));

            return footer;
        }
    }
}
