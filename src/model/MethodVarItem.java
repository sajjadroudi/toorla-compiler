package model;

public class MethodVarItem extends SymbolItem {

    private final String type;
    private final boolean isDefined;

    public MethodVarItem(String name, String type, boolean isDefined) {
        super(name);
        this.type = type;
        this.isDefined = isDefined;
    }

    @Override
    public String toString() {
        return String.format("MethodVar (name: %s) (type: %s) (isDefined: %s)", getName(), type, isDefined);
    }
}
