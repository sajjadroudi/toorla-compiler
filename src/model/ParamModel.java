package model;

public class ParamModel {

    private final String name;
    private final String type;
    private final int index;

    public ParamModel(String name, String type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    @Override
    public String toString() {
        return String.format("{name: %s, type: %s, index: %s}", name, type, index);
    }
}
