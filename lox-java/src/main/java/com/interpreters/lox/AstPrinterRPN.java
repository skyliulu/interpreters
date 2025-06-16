package com.interpreters.lox;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Challenges 5.3 :
 * In reverse Polish notation (RPN), the operands to an arithmetic operator are both placed before the operator, so 1 + 2 becomes 1 2 +.
 * Evaluation proceeds from left to right. Numbers are pushed onto an implicit stack. An arithmetic operator pops the top two numbers,
 * performs the operation, and pushes the result. Thus, this:
 * <p>
 * (1 + 2) * (4 - 3)
 * in RPN becomes:
 * <p>
 * 1 2 + 4 3 - *
 * Define a visitor class for our syntax tree classes that takes an expression, converts it to RPN,
 * and returns the resulting string.
 */

public class AstPrinterRPN implements Expr.Visitor<String>, Stmt.Visitor<String> {

    public String print(List<Stmt> stmts) {
        return stmts.stream().map(stmt -> stmt.accept(this)).collect(Collectors.joining("\n"));
    }

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitAssignmentExpr(Expr.Assignment expr) {
        return "";
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
        return "";
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return "";
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
        return "";
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        return "";
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        return "";
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        for (Expr expr : exprs) {
            builder.append(expr.accept(this));
            builder.append(" ");
        }
        builder.append(name);
        return builder.toString();
    }
}
