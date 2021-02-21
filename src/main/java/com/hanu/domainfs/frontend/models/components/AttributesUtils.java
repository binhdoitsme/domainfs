package com.hanu.domainfs.frontend.models.components;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

public final class AttributesUtils {
    static String attributesToString(Map<String, Object> attrs) {
        StringBuilder output = new StringBuilder();
        for (Entry<String, Object> entry : attrs.entrySet()) {
            output.append(entry.getKey()).append("=");
            Object value = entry.getValue();
            if (value instanceof String) {
                String stringValue = (String) value;
                if (stringValue.contains("{")) {
                    output.append(stringValue); // {handleDoSth} -> reference
                } else {
                    output.append('"').append(entry.getValue()).append('"');
                }
            } else if (value.getClass().isArray()) {
                output.append("{[")
                    .append(Stream.of((Object[])value)
                        .map(v -> v.toString())
                        .reduce((s1, s2) -> s1 + "," + s2))
                    .append("]}");
            } else {
                output.append("{").append(value.toString()).append("}");
            }
            output.append(" ");
        }
        return output.toString();
    }
}
