package com.interpreters.lox;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Variable>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;
    private boolean inLoop = false;

    private enum ClassType {
        NONE, CLASS
    }

    private enum FunctionType {
        NONE, FUNCTION, METHOD
    }

    private enum VariableState {
        DECLARED, DEFINED, READ
    }

    private static class Variable {
        private final Token name;
        private VariableState state;

        public Variable(Token name, VariableState state) {
            this.name = name;
            this.state = state;
        }

        public Token getName() {
            return name;
        }

        public VariableState getState() {
            return state;
        }

        public void setState(VariableState state) {
            this.state = state;
        }
    }

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visitAssignmentExpr(Expr.Assignment expr) {
        resolve(expr.getValue());
        resolveLocal(expr, expr.getName(), false);
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr) {
        resolve(expr.getExpr());
        resolve(expr.getThenBranch());
        resolve(expr.getElseBranch());
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.getExpression());
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().containsKey(expr.getName().getLexeme())
                && scopes.peek().get(expr.getName().getLexeme()).getState() == VariableState.DECLARED) {
            Lox.error(expr.getName(), "Can't read local variable in its own initializer.");
        }
        resolveLocal(expr, expr.getName(), true);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.getCallee());
        expr.getArguments().forEach(this::resolve);
        return null;
    }

    @Override
    public Void visitFunctionExpr(Expr.Function expr) {
        resolveFunction(expr, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.getObject());
        // properties are looked up dynamically, they don’t get resolved.
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.getObject());
        resolve(expr.getValue());
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.getKeyword(), "Can't use 'this' outside of class.");
            return null;
        }
        resolveLocal(expr, expr.getKeyword(), true);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.getName());
        if (null != stmt.getInitializer()) {
            resolve(stmt.getInitializer());
        }
        define(stmt.getName());
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.getStatements());
        endScope();
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.getCondition());
        resolve(stmt.getThenStatement());
        if (null != stmt.getElseStatement()) {
            resolve(stmt.getElseStatement());
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        boolean enclosing = inLoop;
        inLoop = true;
        resolve(stmt.getCondition());
        resolve(stmt.getBody());
        inLoop = enclosing;
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (!inLoop) {
            Lox.error(stmt.getKeyword(), "Can't break from none loop code.");
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.getName());
        define(stmt.getName());
        resolveFunction(stmt.getFunction(), FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.getKeyword(), "Can't return from top-level code.");
        }
        if (null != stmt.getValue()) {
            resolve(stmt.getValue());
        }
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        declare(stmt.getName());
        define(stmt.getName());
        ClassType enclosing = currentClass;
        currentClass = ClassType.CLASS;
        beginScope();
        scopes.peek().put("this", new Variable(stmt.getName(), VariableState.READ));
        stmt.getMethods().forEach(method -> {
            resolveFunction(method.getFunction(), FunctionType.METHOD);
        });
        endScope();
        currentClass = enclosing;
        return null;
    }

    private void resolveFunction(Expr.Function function, FunctionType type) {
        FunctionType enclosing = currentFunction;
        currentFunction = type;
        beginScope();
        function.getParams().forEach(token -> {
            declare(token);
            define(token);
        });
        resolve(function.getBody());
        endScope();
        currentFunction = enclosing;
    }

    private void resolveLocal(Expr expr, Token name, boolean isRead) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.getLexeme())) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                if (isRead) {
                    scopes.get(i).get(name.getLexeme()).setState(VariableState.READ);
                }
                return;
            }
        }
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        Map<String, Variable> scope = scopes.peek();
        if (scope.containsKey(name.getLexeme())) {
            Lox.error(name, "Already a variable with this name in this scope.");
        }
        // variable is not ready
        scope.put(name.getLexeme(), new Variable(name, VariableState.DECLARED));
    }

    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        // variable is ready
        scopes.peek().get(name.getLexeme()).setState(VariableState.DEFINED);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        Map<String, Variable> scope = scopes.pop();
        scope.forEach((k, v) -> {
            if (v.getState() != VariableState.READ) {
                Lox.error(v.getName(), "Local variable is not used.");
            }
        });
    }

    public void resolve(List<Stmt> statements) {
        statements.forEach(this::resolve);
    }

    public void resolve(Stmt statement) {
        statement.accept(this);
    }

    public void resolve(Expr expr) {
        expr.accept(this);
    }
}
