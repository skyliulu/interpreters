package com.interpreters.lox;

import java.util.List;
import java.util.stream.Collectors;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    public String print(List<Stmt> stmts) {
        return stmts.stream().map(stmt -> stmt.accept(this)).collect(Collectors.joining("\n"));
    }

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitAssignmentExpr(Expr.Assignment expr) {
        return expr.getName().getLexeme() + "=" + print(expr.getValue());
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return print(expr.getExpr()) + "? " + print(expr.getThenBranch()) + " : " + print(expr.getElseBranch());
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getLeft(), expr.getRight());
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.getExpression());
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.getValue() == null) {
            return "nil";
        } else {
            return expr.getValue().toString();
        }
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getRight());
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.getName().getLexeme();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return String.format("%s %s %s", print(expr.getLeft()), expr.getOperator().getLexeme(), print(expr.getRight()));
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return String.format("%s(%s)", print(expr.getCallee()),
                expr.getArguments().stream().map(this::print).collect(Collectors.joining(",")));
    }

    @Override
    public String visitFunctionExpr(Expr.Function expr) {
        return String.format("fun (%s) {\n%s\n}", expr.getParams().stream().map(Token::getLexeme).collect(Collectors.joining(","))
                , print(expr.getBody()));
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return print(expr.getObject()) + "." + expr.getName().getLexeme();
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return print(expr.getObject()) + "." + expr.getName().getLexeme() + " = " + print(expr.getValue());
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "this";
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return "super." + expr.getMethod().getLexeme();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return print(stmt.getExpression()) + ";";
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return "print " + print(stmt.getExpression()) + ";";
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        return String.format("var %s%s;", stmt.getName().getLexeme(), stmt.getInitializer() == null ? "" : " = " + print(stmt.getInitializer()));
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        return String.format("{\n%s\n}", print(stmt.getStatements()));
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        if (stmt.getElseStatement() != null) {
            return String.format("if (%s)\n\t%s\nelse\n\t%s", print(stmt.getCondition())
                    , print(List.of(stmt.getThenStatement())), print(List.of(stmt.getElseStatement())));
        } else {

            return String.format("if (%s)\n\t%s", print(stmt.getCondition()), print(List.of(stmt.getThenStatement())));
        }
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return String.format("while (%s)\n\t%s", print(stmt.getCondition()), print(List.of(stmt.getBody())));
    }

    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return "break;";
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        return String.format("fun %s(%s) {\n%s\n}", stmt.getName().getLexeme()
                , stmt.getFunction().getParams().stream().map(Token::getLexeme).collect(Collectors.joining(","))
                , print(stmt.getFunction().getBody()));
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        return "return " + (stmt.getValue() == null ? ";" : print(stmt.getValue()) + ";");
    }

    @Override
    public String visitClassStmt(Stmt.Class stmt) {
        return String.format("class %s{\n%s\n%s\n}"
                , stmt.getSuperclass() == null ? stmt.getName().getLexeme() : (stmt.getName().getLexeme() + " < " + stmt.getSuperclass().getName().getLexeme())
                , stmt.getClassMethods().isEmpty() ? "" : "class " + printMethod(stmt.getClassMethods())
                , printMethod(stmt.getMethods()));
    }

    private String printMethod(List<Stmt.Function> functions) {
        return functions.stream().map(f -> f.accept(this)).collect(Collectors.joining("\n"));
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }
}
