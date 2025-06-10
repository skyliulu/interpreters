package com.interpreters.tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        try (PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
            writer.println("package com.interpreters.lox;");
            writer.println();
            writer.println("import java.util.List;");
            writer.println();
            writer.println("public abstract class " + baseName + " {");
            // visitor pattern
            defineVisitor(writer, baseName, types);
            writer.println();
            writer.println("\tabstract <R> R accept(Visitor<R> visitor);");
            writer.println();
            // ast classes
            for (String type : types) {
                String[] parts = type.split(":");
                String className = parts[0].trim();
                String fieldList = parts[1].trim();
                defineType(writer, baseName, className, fieldList);
            }
            writer.println("}");
        }
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("\tpublic interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("\t\tR visit" + capitalize(typeName) + capitalize(baseName) + "(" + typeName + " " + baseName.toLowerCase() + ");");
            writer.println();
        }
        writer.println("\t}");
        writer.println();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("\tpublic static class " + className + " extends " + baseName + " {");
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            writer.println("\t\tprivate final " + field + ";");
        }
        writer.println();
        // constructor
        writer.println("\t\tpublic " + className + "(" + fieldList + ") {");
        for (String field : fields) {
            String fieldName = field.split(" ")[1];
            writer.println("\t\t\tthis." + fieldName + " = " + fieldName + ";");
        }
        writer.println("\t\t}");
        writer.println();
        // accept
        writer.println("\t\t@Override");
        writer.println("\t\tpublic <R> R accept(Visitor<R> visitor) {");
        writer.println("\t\t\treturn visitor.visit" + capitalize(className) + baseName + "(this);");
        writer.println("\t\t}");
        writer.println();
        // getter
        for (String field : fields) {
            String fieldName = field.split(" ")[1];
            String fieldType = field.split(" ")[0];
            writer.println("\t\tpublic " + fieldType + " get" + capitalize(fieldName) + "() {");
            writer.println("\t\t\treturn " + fieldName + ";");
            writer.println("\t\t}");
            writer.println();
        }
        writer.println("\t}");
        writer.println();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
