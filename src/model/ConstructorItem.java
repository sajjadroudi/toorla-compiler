package model;

public class ConstructorItem extends SymbolItem {

    private final String accessModifier;

    private final ParamModel[] params;

    public ConstructorItem(String name, String accessModifier, ParamModel[] params) {
        super(name);
        this.accessModifier = accessModifier;
        this.params = params;
    }

    @Override
    public String toString() {
        return String.format(
                "Constructor (name: %s) (parameter list: %s) (access modifier: %s)",
                getName(),
                stringifyParameters(),
                accessModifier
        );
    }

    private String stringifyParameters() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < params.length; i++) {
            builder.append(params[i]);
            if(i != params.length - 1)
                builder.append(", ");
        }
        builder.append("]");
        return builder.toString();
    }

}
