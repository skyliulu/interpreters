package com.interpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private final Environment global = new Environment();
    private Environment environment = global;
    private final Map<Expr, Integer> locals = new HashMap<>();

    public Interpreter() {
        global.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn clock>";
            }
        });
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name);
        } else {
            return global.get(name);
        }
    }

    private void assignVariable(Token name, Expr expr, Object value) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, name, value);
        } else {
            global.assign(name, value);
        }
    }

    public Environment getGlobalEnv() {
        return global;
    }

    public void interpret(Expr expr) {
        try {
            Object value = evaluate(expr);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    public void interpret(List<Stmt> stmts) {
        try {
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitAssignmentExpr(Expr.Assignment expr) {
        Object value = evaluate(expr.getValue());
        assignVariable(expr.getName(), expr, value);
        return null;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object condition = evaluate(expr.getExpr());
        if (isTruthy(condition)) {
            return evaluate(expr.getThenBranch());
        } else {
            return evaluate(expr.getElseBranch());
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.getLeft());
        Object right = evaluate(expr.getRight());
        switch (expr.getOperator().getTokenType()) {
            case PLUS -> {
                if (left instanceof Double leftDouble && right instanceof Double rightDouble) {
                    return leftDouble + rightDouble;
                }
                // challenge 7.2
                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }
                throw new RuntimeError(expr.getOperator(), "Operands must be two numbers or two strings or at least one strings.");
            }
            case MINUS -> {
                checkNumberOperand(expr.getOperator(), left, right);
                return (double) left - (double) right;
            }
            case STAR -> {
                checkNumberOperand(expr.getOperator(), left, right);
                return (double) left * (double) right;
            }
            case SLASH -> {
                checkNumberOperand(expr.getOperator(), left, right);
                // challenge 7.3
                checkDivideZero(expr.getOperator(), right);
                return (double) left / (double) right;
            }
            case GREATER -> {
                checkNumberOperand(expr.getOperator(), left, right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperand(expr.getOperator(), left, right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperand(expr.getOperator(), left, right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperand(expr.getOperator(), left, right);
                return (double) left <= (double) right;
            }
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
        }
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.getValue();
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.getRight());
        switch (expr.getOperator().getTokenType()) {
            case MINUS -> {
                checkNumberOperand(expr.getOperator(), right);
                return -((double) right);
            }
            case BANG -> {
                return !isTruthy(right);
            }
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.getName(), expr);
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.getLeft());
        // short circuit
        if (expr.getOperator().getTokenType() == TokenType.AND) {
            if (!isTruthy(left)) return left;
        } else if (expr.getOperator().getTokenType() == TokenType.OR) {
            if (isTruthy(left)) return left;
        }
        return evaluate(expr.getRight());
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.getCallee());
        List<Object> arguments = expr.getArguments().stream().map(this::evaluate).toList();
        if (!(callee instanceof LoxCallable loxCallable)) {
            throw new RuntimeError(expr.getParen(), "Can only call functions and classes.");
        }
        if (loxCallable.arity() != arguments.size()) {
            throw new RuntimeError(expr.getParen(), String.format("Expect %d arguments but got %d.", loxCallable.arity(), arguments.size()));
        }
        return loxCallable.call(this, arguments);
    }

    @Override
    public Object visitFunctionExpr(Expr.Function expr) {
        return new LoxFunction(expr, environment);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.getObject());
        if (object instanceof LoxInstance loxInstance) {
            return loxInstance.get(expr.getName());
        }
        throw new RuntimeError(expr.getName(), "Only instances have properties.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.getObject());
        if (object instanceof LoxInstance loxInstance) {
            Object value = evaluate(expr.getValue());
            loxInstance.set(expr.getName(), value);
            return value;
        }
        throw new RuntimeError(expr.getName(), "Only instances have properties.");
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.getKeyword(), expr);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object object = evaluate(stmt.getExpression());
        System.out.println(stringify(object));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (null != stmt.getInitializer()) {
            value = evaluate(stmt.getInitializer());
        }
        environment.define(stmt.getName().getLexeme(), value);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        if (null == stmt.getStatements() || stmt.getStatements().isEmpty()) {
            return null;
        }
        executeBlock(stmt.getStatements(), new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.getCondition()))) {
            execute(stmt.getThenStatement());
        } else if (null != stmt.getElseStatement()) {
            execute(stmt.getElseStatement());
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        try {
            while (isTruthy(evaluate(stmt.getCondition()))) {
                execute(stmt.getBody());
            }
        } catch (BreakException breakException) {
            // do nothing
        }
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException();
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt.getName().getLexeme(), stmt.getFunction(), environment);
        environment.define(stmt.getName().getLexeme(), function);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.getValue() != null) {
            value = evaluate(stmt.getValue());
        }
        throw new Return(value);
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        // two-stage variable binding process allows references to the class inside its own methods.
        environment.define(stmt.getName().getLexeme(), null);
        Map<String, LoxFunction> methods = new HashMap<>();
        stmt.getMethods().forEach(method -> {
            LoxFunction function = new LoxFunction(method.getName().getLexeme(), method.getFunction(), environment);
            methods.put(method.getName().getLexeme(), function);
        });
        LoxClass loxClass = new LoxClass(stmt.getName().getLexeme(), methods);
        environment.assign(stmt.getName(), loxClass);
        return null;
    }

    public void executeBlock(List<Stmt> body, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            body.forEach(this::execute);
        } finally {
            this.environment = previous;
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (null == object) {
            return false;
        }
        if (object instanceof Boolean bool) {
            return bool;
        }
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }
        return a.equals(b);
    }

    private void checkDivideZero(Token operator, Object right) {
        if (right instanceof Double doubleRight && doubleRight.compareTo(0.0) == 0) {
            throw new RuntimeError(operator, "Cannot divide by zero");
        }
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperand(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private static class BreakException extends RuntimeException {

    }
}
