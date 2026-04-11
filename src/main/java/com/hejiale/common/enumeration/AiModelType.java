package com.hejiale.common.enumeration;

public enum AiModelType {
    CHAT("chat"),
    SERVICE("service"),
    PDF("pdf");

    private String value;

    AiModelType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
