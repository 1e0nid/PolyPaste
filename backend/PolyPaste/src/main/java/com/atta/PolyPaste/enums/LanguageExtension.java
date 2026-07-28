package com.atta.PolyPaste.enums;

import lombok.Getter;

@Getter
public enum LanguageExtension {
    HTML(".html"),
    CSS(".css"),
    JAVASCRIPT(".js"),
    VUE(".vue"),
    JAVA(".java"),
    NONE(".txt");

    private final String extension;

    LanguageExtension(String extension) {
        this.extension = extension;
    }

    public static String getExtensionByValue(String value) {
        if (value == null) return ".txt";
        try {
            return LanguageExtension.valueOf(value.toUpperCase()).getExtension();
        } catch (IllegalArgumentException e) {
            return ".txt";
        }
    }
}
