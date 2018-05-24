package org.otavio.memory;

public class Document {

    private final String json;

    public Document(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Document{");
        sb.append("json='").append(json).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
