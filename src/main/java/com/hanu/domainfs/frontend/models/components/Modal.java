package com.hanu.domainfs.frontend.models.components;

import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.ViewComponent;

public class Modal extends NestableViewComponent {
    private final Modal.Body body;

    private Modal(String title, List<ViewComponent> footerComponents, Map<String, Object> attrs) {
        super("Modal", null, attrs, List.of(), false);
        body = new Modal.Body();
        super.add(new Modal.Header(title));
        super.add(body);
        super.add(new Modal.Footer(footerComponents));
    }

    private Modal(String title, Map<String, Object> attributes, 
                    List<ViewComponent> innerComponents) {
        super("Modal", null, attributes, innerComponents, false);
        body = new Modal.Body();
        super.add(new Modal.Header(title));
        super.add(body);
        super.add(Modal.Footer.defaultFooter());
    }

    private Modal(String title, Map<String, Object> attributes) {
        this(title, attributes, List.of());
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
            super("Modal.Header", null, attributes, 
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
            super("Modal.Title", title);
        }
    }

    private static class Body extends NestableViewComponent {
        public Body(Map<String, Object> attributes, 
                List<ViewComponent> innerComponents) {
            super("Modal.Body", null, attributes, innerComponents, false);
        }

        public Body() {
            super("Modal.Body", null);
        }
    }

    private static class Footer extends NestableViewComponent {
        public Footer(Map<String, Object> attributes, 
                List<ViewComponent> innerComponents) {
            super("Modal.Footer", null, attributes, innerComponents, false);
        }

        public Footer( List<ViewComponent> innerComponents) {
            super("Modal.Footer", null, null, innerComponents, false);
        }

        public Footer() {
            super("Modal.Footer", null);
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

    public static class Builder {
        private Map<String, Object> attrs;
        private String title;
        private List<ViewComponent> items;

        public Builder withAttributes(Map<String, Object> attrs) {
            this.attrs = attrs;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withFooterItems(ViewComponent... items) {
            this.items = List.of(items);
            return this;
        }

        public Modal build() {
            return new Modal(title, items, attrs);
        }
    }
}
