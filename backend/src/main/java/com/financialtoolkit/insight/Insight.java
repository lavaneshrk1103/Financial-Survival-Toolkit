package com.financialtoolkit.insight;

public class Insight {
    private final String type;
    private final String text;

    public Insight(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}
