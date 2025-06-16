package com.interpreters.lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();

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
        environment.assign(expr.getName(), value);
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
        return environment.get(expr.getName());
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
        Environment previous = this.environment;
        try {
            this.environment = new Environment(previous);
            stmt.getStatements().forEach(this::execute);
        } finally {
            this.environment = previous;
        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.getCondition()))) {
            execute(stmt.getThenStatement());
        } else {
            execute(stmt.getElseStatement());
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.getCondition()))) {
            execute(stmt.getBody());
        }
        return null;
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
}
