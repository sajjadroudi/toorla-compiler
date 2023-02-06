package model;

public class ParamFieldItem extends SymbolItem {

    private final String type;
    private final boolean isDefined;

    public ParamFieldItem(String name, String type, boolean isDefined) {
        super(name);
        this.type = type;
        this.isDefined = isDefined;
    }

    public ParamFieldItem(String name, String type) {
        this(name, type, true);
    }

    @Override
    public String toString() {
        return String.format("ParamField (name: %s) (type: %s) (isDefined: %s)", getName(), type, isDefined);
    }
}
