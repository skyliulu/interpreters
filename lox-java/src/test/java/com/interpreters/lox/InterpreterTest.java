package com.interpreters.lox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    @Test
    void testInterpreter() {
        // (1 + 2) * (4 - 3) = 3
        Expr expression = new Expr.Binary(
                new Expr.Binary(
                        new Expr.Literal(1.0),
                        new Token(TokenType.PLUS, "+", null, 1),
                        new Expr.Literal(2.0)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Binary(
                        new Expr.Literal(4.0),
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(3.0)));
        Interpreter interpreter = new Interpreter();
        interpreter.interpret(expression);
    }
}