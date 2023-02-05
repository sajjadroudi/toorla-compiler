public class ParameterModel {

    public final String name;
    public final String type;
    public final boolean isDefined;

    public ParameterModel(String name, String type, boolean isDefined) {
        this.name = name;
        this.type = type;
        this.isDefined = isDefined;
    }

    public ParameterModel(String name, String type) {
        this(name, type, true);
    }

}
