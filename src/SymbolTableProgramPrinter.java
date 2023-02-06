import gen.ErrorReporter;
import gen.ToorlaListener;
import gen.ToorlaParser;
import model.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class SymbolTableProgramPrinter implements ToorlaListener  {

    private final Stack<SymbolTable> scopes = new Stack<>();
    private boolean isAnalyzingMethodVar = false;
    private String currentMethodVarType = null;

    private boolean isInsideALocalBlock = false;

    private final ErrorReporter errorReporter = new ErrorReporter();

    private final Map<String, String> classesToParents = new HashMap<>();

    @Override
    public void enterProgram(ToorlaParser.ProgramContext ctx) {
        var root = new SymbolTable("program", ctx.start.getLine(), null);
        scopes.push(root);
    }

    @Override
    public void exitProgram(ToorlaParser.ProgramContext ctx) {
        var circularInheritanceHierarchy = detectCircularInheritance();
        if(circularInheritanceHierarchy != null) {
            errorReporter.reportCircularInheritanceError(circularInheritanceHierarchy);
        }

        scopes.pop();
    }

    private List<String> detectCircularInheritance() {
        var classes = classesToParents.keySet();
        for(String className : classes) {
            var hierarchy = getHierarchy(className);
            var uniqueClassesOfHierarchy = new HashSet<>(hierarchy);
            if(hierarchy.size() != uniqueClassesOfHierarchy.size())
                return hierarchy;
        }
        return null;
    }

    private List<String> getHierarchy(String className) {
        List<String> hierarchy = new ArrayList<>();

        var parent = className;

        hierarchy.add(parent);

        while(true) {
            parent = classesToParents.get(parent);

            if(hierarchy.contains(parent)) {
                hierarchy.add(parent); // To show that the hierarchy has a loop
                break;
            }

            if(parent == null || parent.equals("none"))
                break;

            hierarchy.add(parent);
        }
        return hierarchy;
    }

    @Override
    public void enterClassDeclaration(ToorlaParser.ClassDeclarationContext ctx) {
        var className = ctx.ID(0).toString();
        var parentClassName = Helper.getParentClassName(ctx);

        classesToParents.put(className, parentClassName);

        var isEntry = Helper.isEntryClass(ctx);

        var key = "class_" + className;
        var value = new ClassItem(className, parentClassName, isEntry);

        if(scopes.peek().contains(key)) {
            int line = ctx.start.getLine();
            int column = ctx.ID(0).getSymbol().getCharPositionInLine();
            errorReporter.reportClassRedefinitionError(className, line, column);
            key = String.format("%s_%s_%s", className, line, column);
        }

        scopes.peek().insert(key, value);

        var newScope = new SymbolTable(className, ctx.start.getLine(), scopes.peek());
        scopes.push(newScope);
    }

    @Override
    public void exitClassDeclaration(ToorlaParser.ClassDeclarationContext ctx) {
        scopes.pop();
    }

    @Override
    public void enterEntryClassDeclaration(ToorlaParser.EntryClassDeclarationContext ctx) {

    }

    @Override
    public void exitEntryClassDeclaration(ToorlaParser.EntryClassDeclarationContext ctx) {

    }

    @Override
    public void enterFieldDeclaration(ToorlaParser.FieldDeclarationContext ctx) {
        var fieldName = ctx.ID(0).toString();
        var type = ctx.fieldType.getText();

        var key = "field_" + fieldName;

        if(scopes.peek().contains(key)) {
            int line = ctx.start.getLine();
            int column = ctx.ID(0).getSymbol().getCharPositionInLine();
            errorReporter.reportFieldRedefinitionError(fieldName, line, column);
            key = String.format("%s_%s_%s", fieldName, line, column);
        }

        var value = new ClassFieldItem(fieldName, type, true);
        scopes.peek().insert(key, value);
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
        var methodType = Helper.isConstructorMethod(ctx) ? "constructor" : "method";
        var accessModifier = (ctx.access_modifier() == null) ? "public" : ctx.access_modifier().getText();
        var methodName = ctx.methodName.getText();
        var returnType = ctx.t.getText();

        var key = methodType + "_" + methodName;

        if(scopes.peek().contains(key)) {
            int line = ctx.start.getLine();
            int column = ctx.ID(0).getSymbol().getCharPositionInLine();
            errorReporter.reportMethodRedefinitionError(methodName, line, column);
            key = String.format("%s_%s_%s", methodName, line, column);
        }

        SymbolItem value;
        if("constructor".equals(methodType)) {
            value = new ConstructorItem(methodName, accessModifier, Helper.getParametersIndexed(ctx));
        } else {
            value = new MethodItem(methodName, returnType, accessModifier, Helper.getParametersIndexed(ctx));
        }
        scopes.peek().insert(key, value);

        var newScope = new SymbolTable(methodName, ctx.start.getLine(), scopes.peek());
        scopes.push(newScope);

        var parameters = Helper.getParameters(ctx);
        for(ParamFieldItem param : parameters) {
            var fieldKey = "field_" + param.getName();
            scopes.peek().insert(fieldKey, param);
        }
    }

    @Override
    public void exitMethodDeclaration(ToorlaParser.MethodDeclarationContext ctx) {
        scopes.pop();
    }

    @Override
    public void enterClosedStatement(ToorlaParser.ClosedStatementContext ctx) {

    }

    @Override
    public void exitClosedStatement(ToorlaParser.ClosedStatementContext ctx) {

    }

    @Override
    public void enterClosedConditional(ToorlaParser.ClosedConditionalContext ctx) {
        String name = "if";
        if(isInsideALocalBlock)
            name = "nested";

        var newScope = new SymbolTable(name, ctx.start.getLine(), scopes.peek());
        scopes.push(newScope);
    }

    @Override
    public void exitClosedConditional(ToorlaParser.ClosedConditionalContext ctx) {
        scopes.pop();
    }

    @Override
    public void enterOpenConditional(ToorlaParser.OpenConditionalContext ctx) {
        String name = "if";
        if(isInsideALocalBlock)
            name = "nested";

        var newScope = new SymbolTable(name, ctx.start.getLine(), scopes.peek());
        scopes.push(newScope);
    }

    @Override
    public void exitOpenConditional(ToorlaParser.OpenConditionalContext ctx) {
        scopes.pop();
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
        isAnalyzingMethodVar = true;
    }

    @Override
    public void exitStatementVarDef(ToorlaParser.StatementVarDefContext ctx) {
        var variableNames = ctx.ID().stream().map(ParseTree::getText).toList();
        for(var variableName : variableNames) {
            var key = "field_" + variableName;

            if(scopes.peek().contains(key)) {
                int line = ctx.start.getLine();
                int column = ctx.ID(0).getSymbol().getCharPositionInLine();
                errorReporter.reportLocalVariableRedefinitionError(variableName, line, column);
                key = String.format("%s_%s_%s", variableName, line, column);
            }

            var value = new MethodVarItem(variableName, currentMethodVarType, true);
            scopes.peek().insert(key, value);
        }

        isAnalyzingMethodVar = false;
        currentMethodVarType = null;
    }

    @Override
    public void enterStatementBlock(ToorlaParser.StatementBlockContext ctx) {
        isInsideALocalBlock = true;
    }

    @Override
    public void exitStatementBlock(ToorlaParser.StatementBlockContext ctx) {
        isInsideALocalBlock = false;
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
        String name = "while";
        if(isInsideALocalBlock)
            name = "nested";

        var newScope = new SymbolTable(name, ctx.start.getLine(), scopes.peek());
        scopes.push(newScope);
    }

    @Override
    public void exitStatementClosedLoop(ToorlaParser.StatementClosedLoopContext ctx) {
        scopes.pop();
    }

    @Override
    public void enterStatementOpenLoop(ToorlaParser.StatementOpenLoopContext ctx) {
        var newScope = new SymbolTable("while", ctx.start.getLine(), scopes.peek());
        scopes.push(newScope);
    }

    @Override
    public void exitStatementOpenLoop(ToorlaParser.StatementOpenLoopContext ctx) {
        scopes.pop();
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
        if(!isAnalyzingMethodVar || currentMethodVarType != null)
            return;

        if(ctx.n != null) {
            currentMethodVarType = "int";
        } else if(ctx.s != null) {
            currentMethodVarType = "string";
        } else if(ctx.st != null) {
            currentMethodVarType = ctx.st.getText() + "[]";
        } else if(ctx.i != null) {
            currentMethodVarType = ctx.i.getText();
        } else if(ctx.trueModifier != null || ctx.falseModifier != null) {
            currentMethodVarType = "boolean";
        } else {
            currentMethodVarType = "local var";
        }
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
