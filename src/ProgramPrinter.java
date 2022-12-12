import gen.ToorlaListener;
import gen.ToorlaParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ProgramPrinter implements ToorlaListener {

    private static final int INDENTATION_UNIT = 4;
    private static final Class[] blockClasses = {
            ToorlaParser.ClosedConditionalContext.class,
            ToorlaParser.OpenConditionalContext.class,
            ToorlaParser.StatementClosedLoopContext.class,
            ToorlaParser.StatementOpenLoopContext.class
    };

    private int indentation = 0;

    private void increaseIndentation() {
        indentation += INDENTATION_UNIT;
    }

    private void decreaseIndentation() {
        indentation -= INDENTATION_UNIT;
    }

    @Override
    public void enterProgram(ToorlaParser.ProgramContext ctx) {
        System.out.println("program start {");
        increaseIndentation();
    }

    @Override
    public void exitProgram(ToorlaParser.ProgramContext ctx) {
        decreaseIndentation();
        System.out.println("}");
    }

    @Override
    public void enterClassDeclaration(ToorlaParser.ClassDeclarationContext ctx) {
        var className = ctx.ID(0).toString();
        var isEntry = (ctx.parent instanceof ToorlaParser.EntryClassDeclarationContext);

        System.out.printf("class: %s / class parent: %s / isEntry: %s {".indent(indentation), className, getParentClassName(ctx), isEntry);
        increaseIndentation();
    }

    private String getParentClassName(ToorlaParser.ClassDeclarationContext ctx) {
        var parentClass = ctx.ID(1);

        if(parentClass == null)
            return "none";
        else
            return parentClass.toString();
    }

    @Override
    public void exitClassDeclaration(ToorlaParser.ClassDeclarationContext ctx) {
        decreaseIndentation();
        System.out.print("}".indent(indentation));
    }

    @Override
    public void enterEntryClassDeclaration(ToorlaParser.EntryClassDeclarationContext ctx) {

    }

    @Override
    public void exitEntryClassDeclaration(ToorlaParser.EntryClassDeclarationContext ctx) {

    }

    @Override
    public void enterFieldDeclaration(ToorlaParser.FieldDeclarationContext ctx) {
        System.out.printf("field: %s / type: %s\n".indent(indentation), ctx.ID(0), ctx.fieldType.getText());
    }

    @Override
    public void exitFieldDeclaration(ToorlaParser.FieldDeclarationContext ctx) {

    }

    @Override
    public void enterAccess_modifier(ToorlaParser.Access_modifierContext ctx) {

    }

    @Override
    public void exitAccess_modifier(ToorlaParser.Access_modifierContext ctx) {

    }

    @Override
    public void enterMethodDeclaration(ToorlaParser.MethodDeclarationContext ctx) {
        var methodType = isConstructorMethod(ctx) ? "constructor" : "method";
        var accessModifier = (ctx.access_modifier() == null) ? "public" : ctx.access_modifier().getText();
        System.out.printf("class %s: %s / return type: %s/ type: %s{\n".indent(indentation), methodType, ctx.methodName.getText(), ctx.t.getText(), accessModifier);

        increaseIndentation();
        System.out.print(getParameterList(ctx).indent(indentation));
    }

    private boolean isConstructorMethod(ToorlaParser.MethodDeclarationContext ctx) {
        var classContext = (ToorlaParser.ClassDeclarationContext) ctx.parent;
        var className = classContext.className.getText();
        var methodName = ctx.methodName.getText();
        return methodName.equals(className);
    }

    private String getParameterList(ToorlaParser.MethodDeclarationContext ctx) {
        var parameterNameList = ctx.ID().subList(1, ctx.ID().size());
        var parameterTypeList = ctx.toorlaType().subList(0, ctx.toorlaType().size() - 1);

        final int paramSize = parameterTypeList.size();

        StringBuilder output = new StringBuilder();
        for(int i = 0; i < paramSize; i++) {
            output
                    .append("type: ")
                    .append(parameterTypeList.get(i).getText())
                    .append(" / ")
                    .append("name: ")
                    .append(parameterNameList.get(i).getText());

            if(i != (paramSize - 1))
                output.append(", ");
        }

        return "parameter list: [" + output + "]";
    }

    @Override
    public void exitMethodDeclaration(ToorlaParser.MethodDeclarationContext ctx) {
        decreaseIndentation();
        System.out.print("}".indent(indentation));
    }

    @Override
    public void enterClosedStatement(ToorlaParser.ClosedStatementContext ctx) {

    }

    @Override
    public void exitClosedStatement(ToorlaParser.ClosedStatementContext ctx) {

    }

    private boolean hasChildBlock(ParserRuleContext ctx) {
        for(var child : ctx.children) {
            if(isBlock(child)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlock(ParseTree ctx) {
        for(Class clazz : blockClasses) {
            if(clazz.isInstance(ctx)) {
                return true;
            }
        }
        return false;
    }

    private void startProducingNestedBlock(ParserRuleContext ctx) {
        if(hasChildBlock(ctx)) {
            System.out.print("nested {".indent(indentation));
            increaseIndentation();
        }
    }

    private void endProducingNestedBlock(ParserRuleContext ctx) {
        if(hasChildBlock(ctx)) {
            decreaseIndentation();
            System.out.print("}".indent(indentation));
        }
    }

    @Override
    public void enterClosedConditional(ToorlaParser.ClosedConditionalContext ctx) {
        startProducingNestedBlock(ctx);
    }

    @Override
    public void exitClosedConditional(ToorlaParser.ClosedConditionalContext ctx) {
        endProducingNestedBlock(ctx);
    }

    @Override
    public void enterOpenConditional(ToorlaParser.OpenConditionalContext ctx) {
        startProducingNestedBlock(ctx);
    }

    @Override
    public void exitOpenConditional(ToorlaParser.OpenConditionalContext ctx) {
        endProducingNestedBlock(ctx);
    }

    @Override
    public void enterOpenStatement(ToorlaParser.OpenStatementContext ctx) {

    }

    @Override
    public void exitOpenStatement(ToorlaParser.OpenStatementContext ctx) {

    }

    @Override
    public void enterStatement(ToorlaParser.StatementContext ctx) {

    }

    @Override
    public void exitStatement(ToorlaParser.StatementContext ctx) {

    }

    @Override
    public void enterStatementVarDef(ToorlaParser.StatementVarDefContext ctx) {
        var variableNames = ctx.ID();
        for(var variableName : variableNames) {
            System.out.printf("field: %s / type: local var".indent(indentation), variableName);
        }
    }

    @Override
    public void exitStatementVarDef(ToorlaParser.StatementVarDefContext ctx) {

    }

    @Override
    public void enterStatementBlock(ToorlaParser.StatementBlockContext ctx) {

    }

    @Override
    public void exitStatementBlock(ToorlaParser.StatementBlockContext ctx) {

    }

    @Override
    public void enterStatementContinue(ToorlaParser.StatementContinueContext ctx) {

    }

    @Override
    public void exitStatementContinue(ToorlaParser.StatementContinueContext ctx) {

    }

    @Override
    public void enterStatementBreak(ToorlaParser.StatementBreakContext ctx) {

    }

    @Override
    public void exitStatementBreak(ToorlaParser.StatementBreakContext ctx) {

    }

    @Override
    public void enterStatementReturn(ToorlaParser.StatementReturnContext ctx) {

    }

    @Override
    public void exitStatementReturn(ToorlaParser.StatementReturnContext ctx) {

    }

    @Override
    public void enterStatementClosedLoop(ToorlaParser.StatementClosedLoopContext ctx) {
        startProducingNestedBlock(ctx);
    }

    @Override
    public void exitStatementClosedLoop(ToorlaParser.StatementClosedLoopContext ctx) {
        endProducingNestedBlock(ctx);
    }

    @Override
    public void enterStatementOpenLoop(ToorlaParser.StatementOpenLoopContext ctx) {
        startProducingNestedBlock(ctx);
    }

    @Override
    public void exitStatementOpenLoop(ToorlaParser.StatementOpenLoopContext ctx) {
        endProducingNestedBlock(ctx);
    }

    @Override
    public void enterStatementWrite(ToorlaParser.StatementWriteContext ctx) {

    }

    @Override
    public void exitStatementWrite(ToorlaParser.StatementWriteContext ctx) {

    }

    @Override
    public void enterStatementAssignment(ToorlaParser.StatementAssignmentContext ctx) {

    }

    @Override
    public void exitStatementAssignment(ToorlaParser.StatementAssignmentContext ctx) {

    }

    @Override
    public void enterStatementInc(ToorlaParser.StatementIncContext ctx) {

    }

    @Override
    public void exitStatementInc(ToorlaParser.StatementIncContext ctx) {

    }

    @Override
    public void enterStatementDec(ToorlaParser.StatementDecContext ctx) {

    }

    @Override
    public void exitStatementDec(ToorlaParser.StatementDecContext ctx) {

    }

    @Override
    public void enterExpression(ToorlaParser.ExpressionContext ctx) {

    }

    @Override
    public void exitExpression(ToorlaParser.ExpressionContext ctx) {

    }

    @Override
    public void enterExpressionOr(ToorlaParser.ExpressionOrContext ctx) {

    }

    @Override
    public void exitExpressionOr(ToorlaParser.ExpressionOrContext ctx) {

    }

    @Override
    public void enterExpressionOrTemp(ToorlaParser.ExpressionOrTempContext ctx) {

    }

    @Override
    public void exitExpressionOrTemp(ToorlaParser.ExpressionOrTempContext ctx) {

    }

    @Override
    public void enterExpressionAnd(ToorlaParser.ExpressionAndContext ctx) {

    }

    @Override
    public void exitExpressionAnd(ToorlaParser.ExpressionAndContext ctx) {

    }

    @Override
    public void enterExpressionAndTemp(ToorlaParser.ExpressionAndTempContext ctx) {

    }

    @Override
    public void exitExpressionAndTemp(ToorlaParser.ExpressionAndTempContext ctx) {

    }

    @Override
    public void enterExpressionEq(ToorlaParser.ExpressionEqContext ctx) {

    }

    @Override
    public void exitExpressionEq(ToorlaParser.ExpressionEqContext ctx) {

    }

    @Override
    public void enterExpressionEqTemp(ToorlaParser.ExpressionEqTempContext ctx) {

    }

    @Override
    public void exitExpressionEqTemp(ToorlaParser.ExpressionEqTempContext ctx) {

    }

    @Override
    public void enterExpressionCmp(ToorlaParser.ExpressionCmpContext ctx) {

    }

    @Override
    public void exitExpressionCmp(ToorlaParser.ExpressionCmpContext ctx) {

    }

    @Override
    public void enterExpressionCmpTemp(ToorlaParser.ExpressionCmpTempContext ctx) {

    }

    @Override
    public void exitExpressionCmpTemp(ToorlaParser.ExpressionCmpTempContext ctx) {

    }

    @Override
    public void enterExpressionAdd(ToorlaParser.ExpressionAddContext ctx) {

    }

    @Override
    public void exitExpressionAdd(ToorlaParser.ExpressionAddContext ctx) {

    }

    @Override
    public void enterExpressionAddTemp(ToorlaParser.ExpressionAddTempContext ctx) {

    }

    @Override
    public void exitExpressionAddTemp(ToorlaParser.ExpressionAddTempContext ctx) {

    }

    @Override
    public void enterExpressionMultMod(ToorlaParser.ExpressionMultModContext ctx) {

    }

    @Override
    public void exitExpressionMultMod(ToorlaParser.ExpressionMultModContext ctx) {

    }

    @Override
    public void enterExpressionMultModTemp(ToorlaParser.ExpressionMultModTempContext ctx) {

    }

    @Override
    public void exitExpressionMultModTemp(ToorlaParser.ExpressionMultModTempContext ctx) {

    }

    @Override
    public void enterExpressionUnary(ToorlaParser.ExpressionUnaryContext ctx) {

    }

    @Override
    public void exitExpressionUnary(ToorlaParser.ExpressionUnaryContext ctx) {

    }

    @Override
    public void enterExpressionMethods(ToorlaParser.ExpressionMethodsContext ctx) {

    }

    @Override
    public void exitExpressionMethods(ToorlaParser.ExpressionMethodsContext ctx) {

    }

    @Override
    public void enterExpressionMethodsTemp(ToorlaParser.ExpressionMethodsTempContext ctx) {

    }

    @Override
    public void exitExpressionMethodsTemp(ToorlaParser.ExpressionMethodsTempContext ctx) {

    }

    @Override
    public void enterExpressionOther(ToorlaParser.ExpressionOtherContext ctx) {

    }

    @Override
    public void exitExpressionOther(ToorlaParser.ExpressionOtherContext ctx) {

    }

    @Override
    public void enterToorlaType(ToorlaParser.ToorlaTypeContext ctx) {

    }

    @Override
    public void exitToorlaType(ToorlaParser.ToorlaTypeContext ctx) {

    }

    @Override
    public void enterSingleType(ToorlaParser.SingleTypeContext ctx) {

    }

    @Override
    public void exitSingleType(ToorlaParser.SingleTypeContext ctx) {

    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {

    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {

    }
}
