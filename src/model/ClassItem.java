package model;

public class ClassItem extends SymbolItem {

    private final String parent;
    private final boolean isEntry;

    public ClassItem(String name, String parent, boolean isEntry) {
        super(name);
        this.parent = parent;
        this.isEntry = isEntry;
    }

    @Override
    public String toString() {
        return String.format("Class (name: %s) (parent: %s) (isEntry: %s)", getName(), parent, isEntry);
    }
}
