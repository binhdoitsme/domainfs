package com.hanu.domainfs.frontend.models.components;

public interface InputType {
    String toTypeString();
    /**
     * Define standard input types.
     */
    public enum Type implements InputType {
        TEXT {
            @Override
            public String toTypeString() { return "text"; }
        },
        MULTILINE {
            @Override
            public String toTypeString() { return "textarea"; }
        },
        NUMBER {
            @Override
            public String toTypeString() { return "number"; }
        },
        DATE {
            @Override
            public String toTypeString() { return "date"; }
        },
        TIME {
            @Override
            public String toTypeString() { return "time"; }
        },
        PHONE {
            @Override
            public String toTypeString() { return "tel"; }
        },
        URL {
            @Override
            public String toTypeString() { return "url"; }
        },
        EMAIL {
            @Override
            public String toTypeString() { return "email"; }
        },
        PASSWORD {
            @Override
            public String toTypeString() { return "password"; }
        },
        COLOR {
            @Override
            public String toTypeString() { return "color"; }
        }
    }
}
