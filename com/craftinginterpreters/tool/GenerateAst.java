package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
       if(args.length != 1){
           System.err.println("Usage: generate_ast <outpout directory>");
           System.exit(64);
       }
       String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Get      : Expr object, Token name",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logical  : Expr left, Token operator, Expr right",
                "Set      : Expr object, Token name, Expr value",
                "Super    : Token keyword, Token method",
                "This     : Token keyword",
                "Unary    : Token operator , Expr right",
                "Variable : Token name",
                "Assign   : Token name, Expr value"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body",
                "Block      : List<Stmt> statements",
                "Class      : Token name, Expr.Variable superClass, List<Stmt.Function> methods"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
       String path = outputDir + '/' + baseName + ".java";
       try( PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8)){
           writer.println("package com.craftinginterpreters.lox;");
           writer.println();
           writer.println("import java.util.List;");
           writer.println();
           writer.println("abstract class " + baseName + " {");
           defineVisitor(writer,baseName,types);
          types.forEach(type -> {
              String[] splitByColon = type.split(":");
              String className = splitByColon[0].trim();
              String fields = splitByColon[1].trim();
              defineType(writer,baseName,className,fields);
          });

          writer.println();
          writer.println("   abstract <R> R accept(Visitor<R> visitor);");

           writer.println("}");
       }

    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println(" interface Visitor<R> {");
        types.forEach(type -> {
           String typeName = type.split(":")[0].trim();
           writer.println(" R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        });
        writer.println();
        writer.println("  }");

    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldsList) {
        writer.println("public static class " + className + " extends " + baseName + " {");

        writer.println("     "  + className + "(" + fieldsList + ") {");

        String[] fields = fieldsList.split((", "));
        Arrays.stream(fields).forEach(field -> {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        });

        writer.println("    }");

        writer.println();
        writer.println("  @Override");
        writer.println("  <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        writer.println();
        Arrays.stream(fields).forEach(field -> {
           writer.println("   final " + field + ";");
        });

        writer.println("   }");

    }
}
