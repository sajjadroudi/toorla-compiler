package gen;

public class ErrorReporter {

    public void reportClassRedefinitionError(String className, int line, int column) {
        System.err.printf("Error 101: in line [%d:%d], class [%s] has been defined already\n", line, column, className);
    }

    public void reportMethodRedefinitionError() {

    }

    public void reportFieldRedefinitionError() {

    }

    public void reportLocalVariableRedefinitionError() {

    }

    public void reportCircularInheritanceError() {

    }

    public void reportIncompatibleReturnTypeError() {

    }

    public void reportAccessToPrivateMethodError() {

    }

}
