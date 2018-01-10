package model;

public class SuggestValue {

    public String value;

    public SuggestValue(String value) {
        this.value = value;
    }

    public SuggestValue() {
        this.value = "";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
