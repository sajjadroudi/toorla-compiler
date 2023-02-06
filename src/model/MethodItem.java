package model;

import java.util.Objects;

public class MethodItem extends SymbolItem {

    private final String returnType;
    private final String accessModifier;

    private final ParamModel[] params;

    public MethodItem(String name, String returnType, String accessModifier, ParamModel[] params) {
        super(name);
        this.returnType = returnType;
        this.accessModifier = accessModifier;
        this.params = params;
    }

    @Override
    public String toString() {
        return String.format(
                "Method (name: %s) (parameter list: %s) (return type: %s) (access modifier: %s)",
                getName(),
                stringifyParameters(),
                returnType,
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodItem that = (MethodItem) o;
        return Objects.equals(getName(), that.getName()) && isParamsEqual(that.params);
    }

    private boolean isParamsEqual(ParamModel[] other) {
        if(params.length != other.length)
            return false;

        for(int i = 0; i < params.length; i++) {
            if(!params[i].getType().equals(other[i].getType()))
                return false;
        }

        return true;
    }

}
