package model;

public abstract class SymbolItem {

    private final String name;

    public SymbolItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
