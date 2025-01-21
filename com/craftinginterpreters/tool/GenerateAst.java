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
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator , Expr right"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
       String path = outputDir + '/' + baseName + ".java";
       try( PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8)){
           writer.println("import com.craftinginterpreters.lox.Token;");
           writer.println();
           writer.println("import java.util.List;");
           writer.println();
           writer.println("abstract class " + baseName + " {");
          types.forEach(type -> {
              String[] splitByColon = type.split(":");
              String className = splitByColon[0].trim();
              String fields = splitByColon[1].trim();
              defineType(writer,baseName,className,fields);
          });

           writer.println("}");
       }

    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldsList) {
        writer.println(" static class " + className + " extends " + baseName + " {");

        writer.println("     "  + className + "(" + fieldsList + ") {");

        String[] fields = fieldsList.split((", "));
        Arrays.stream(fields).forEach(field -> {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        });

        writer.println("    }");

        writer.println();
        Arrays.stream(fields).forEach(field -> {
           writer.println("   final " + field + ";");
        });

        writer.println("   }");

    }
}
