public class Printer {

    private static final int INDENTATION_UNIT = 4;

    private int indentation = 0;

    public void increaseIndentation() {
        indentation += INDENTATION_UNIT;
    }

    public void decreaseIndentation() {
        indentation -= INDENTATION_UNIT;
    }

    public void print(String format, Object... args) {
        System.out.printf(format.indent(indentation), args);
    }

    public void println(String format, Object... args) {
        print(format + "\n", args);
    }

}
