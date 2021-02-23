package com.hanu.domainfs.frontend.models;

public final class IndentationUtils {
    private static final String INDENTATION = "\s\s";
    public static String indentStringBy(int moreIndent, String original) {
        StringBuilder sb = new StringBuilder();
        String[] lines = original.trim().split("\n");
        for (String line : lines) {
            for (int i = 0; i < moreIndent; i++) {
                sb.append(INDENTATION);
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
