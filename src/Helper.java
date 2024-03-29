import gen.ToorlaParser;
import model.ParamFieldItem;
import model.ParamModel;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Helper {

    public static boolean isConstructorMethod(ToorlaParser.MethodDeclarationContext ctx) {
        var classContext = (ToorlaParser.ClassDeclarationContext) ctx.parent;
        var className = classContext.className.getText();
        var methodName = ctx.methodName.getText();
        return methodName.equals(className);
    }

    public static String getParameterList(ToorlaParser.MethodDeclarationContext ctx) {
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

    public static ParamModel[] getParametersIndexed(ToorlaParser.MethodDeclarationContext ctx) {
        var parameterNameList = ctx.ID().subList(1, ctx.ID().size());
        var parameterTypeList = ctx.toorlaType().subList(0, ctx.toorlaType().size() - 1);

        final int paramSize = parameterTypeList.size();

        List<ParamModel> params = new ArrayList<>();

        for(int i = 0; i < paramSize; i++) {
            var name = parameterNameList.get(i).getText();
            var type = parameterTypeList.get(i).getText();
            var param = new ParamModel(name, type, i + 1);
            params.add(param);
        }

        return params.toArray(new ParamModel[0]);
    }

    public static ParamFieldItem[] getParameters(ToorlaParser.MethodDeclarationContext ctx) {
        var parameterNameList = ctx.ID().subList(1, ctx.ID().size());
        var parameterTypeList = ctx.toorlaType().subList(0, ctx.toorlaType().size() - 1);

        final int paramSize = parameterTypeList.size();

        var list = new ArrayList<ParamFieldItem>();

        for(int i = 0; i < paramSize; i++) {
            var model = new ParamFieldItem(parameterNameList.get(i).getText(), parameterTypeList.get(i).getText());
            list.add(model);
        }

        return list.toArray(new ParamFieldItem[0]);
    }

    public static boolean isMainMethod(ToorlaParser.MethodDeclarationContext ctx) {
        var methodName = ctx.methodName.getText();
        var returnType = ctx.t.getText();
        var accessModifier = (ctx.access_modifier() == null) ? "public" : ctx.access_modifier().getText();

        return "main".equals(methodName) && "int".equals(returnType) && "public".equals(accessModifier) && Helper.hasNotAnyParameter(ctx);
    }
    private static boolean hasNotAnyParameter(ToorlaParser.MethodDeclarationContext ctx) {
        var parameterNameList = ctx.ID().subList(1, ctx.ID().size());
        return parameterNameList.isEmpty();
    }

    public static boolean hasChildBlock(ParserRuleContext ctx) {
        for(var child : ctx.children) {
            if(isBlock(child)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isBlock(ParseTree ctx) {
        final Class[] blockClasses = {
            ToorlaParser.ClosedConditionalContext.class,
            ToorlaParser.OpenConditionalContext.class,
            ToorlaParser.StatementClosedLoopContext.class,
            ToorlaParser.StatementOpenLoopContext.class
        };

        for(Class clazz : blockClasses) {
            if(clazz.isInstance(ctx)) {
                return true;
            }
        }

        return false;
    }

    public static String getParentClassName(ToorlaParser.ClassDeclarationContext ctx) {
        var parentClass = ctx.ID(1);

        if(parentClass == null)
            return "none";

        return parentClass.toString();
    }

    public static boolean isEntryClass(ToorlaParser.ClassDeclarationContext ctx) {
        return ctx.parent instanceof ToorlaParser.EntryClassDeclarationContext;
    }

    public static List<String> detectCircularInheritance(Map<String, String> classesToParents) {
        var classes = classesToParents.keySet();
        for(String className : classes) {
            var hierarchy = getHierarchy(className, classesToParents);
            var uniqueClassesOfHierarchy = new HashSet<>(hierarchy);
            if(hierarchy.size() != uniqueClassesOfHierarchy.size())
                return hierarchy;
        }
        return null;
    }

    private static List<String> getHierarchy(String className, Map<String, String> classesToParents) {
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

    public static String extractType(ToorlaParser.ExpressionOtherContext ctx) {
        if(ctx.n != null) {
            return "int";
        } else if(ctx.s != null) {
            return "string";
        } else if(ctx.st != null) {
            return ctx.st.getText() + "[]";
        } else if(ctx.i != null) {
            return ctx.i.getText();
        } else if(ctx.trueModifier != null || ctx.falseModifier != null) {
            return "boolean";
        }
        return null;
    }

}
