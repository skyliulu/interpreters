package com.interpreters.lox;

import java.util.List;

public abstract class Expr {
    public static class Binary extends Expr {
        private final Expr left;
        private final Token operator;
        private final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expr getLeft() {
            return left;
        }

        public Token getOperator() {
            return operator;
        }

        public Expr getRight() {
            return right;
        }

    }

    public static class Grouping extends Expr {
        private final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        public Expr getExpression() {
            return expression;
        }

    }

    public static class Literal extends Expr {
        private final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

    }

    public static class Unary extends Expr {
        private final Token operator;
        private final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        public Token getOperator() {
            return operator;
        }

        public Expr getRight() {
            return right;
        }

    }

}
