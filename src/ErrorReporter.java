import java.util.List;

public class ErrorReporter {

    public void reportClassRedefinitionError(String className, int line, int column) {
        System.err.printf("Error 101: in line [%d:%d], class [%s] has been defined already\n", line, column, className);
    }

    public void reportMethodRedefinitionError(String methodName, int line, int column) {
        System.err.printf("Error 102: in line [%d:%d], method [%s] has been defined already\n", line, column, methodName);
    }

    public void reportFieldRedefinitionError(String fieldName, int line, int column) {
        System.err.printf("Error 103: in line [%d:%d], field [%s] has been defined already\n", line, column, fieldName);
    }

    public void reportLocalVariableRedefinitionError(String variableName, int line, int column) {
        System.err.printf("Error 104: in line [%d:%d], var [%s] has been defined already\n", line, column, variableName);
    }

    public void reportCircularInheritanceError(List<String> classes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            builder
                    .append("[")
                    .append(classes.get(i))
                    .append("]");

            if(i != classes.size() - 1) {
                builder.append(" -> ");
            }
        }

        System.err.printf("Error 410: Invalid inheritance %s\n", builder);
    }

    public void reportIncompatibleReturnTypeError() {

    }

    public void reportAccessToPrivateMethodError() {

    }

}
