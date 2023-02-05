import gen.ToorlaLexer;
import gen.ToorlaListener;
import gen.ToorlaParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

public class Compiler {

    private static final String SAMPLE_TEST_FILE_PATH = "sample/input.trl";

    public static void main(String[] args) throws IOException {
        ParseTree tree = buildTree();
        ToorlaListener listener = new SymbolTableProgramPrinter();

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, tree);

        SymbolTable.getAllInstances().forEach(System.out::println);
    }

    private static ParseTree buildTree() throws IOException {
        CharStream stream = CharStreams.fromFileName(SAMPLE_TEST_FILE_PATH);
        ToorlaLexer lexer = new ToorlaLexer(stream);
        TokenStream tokens = new CommonTokenStream(lexer);
        ToorlaParser parser = new ToorlaParser(tokens);
        parser.setBuildParseTree(true);
        return parser.program();
    }

}