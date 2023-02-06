package model;

public class ClassFieldItem extends SymbolItem {

    private final String type;
    private final boolean isDefined;

    public ClassFieldItem(String name, String type, boolean isDefined) {
        super(name);
        this.type = type;
        this.isDefined = isDefined;
    }

    @Override
    public String toString() {
        return String.format("ClassField (name: %s) (type: %s) (isDefined: %s)", getName(), type, isDefined);
    }
}
