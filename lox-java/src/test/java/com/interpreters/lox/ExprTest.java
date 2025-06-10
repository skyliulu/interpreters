package com.interpreters.lox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExprTest {

    @Test
    void testExpr() {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));
        AstPrinter printer = new AstPrinter();
        assertEquals("(* (- 123) (group 45.67))", printer.print(expression));
        AstPrinterRPN printerRPN = new AstPrinterRPN();
        assertEquals("123 - 45.67 group *", printerRPN.print(expression));
        // (1 + 2) * (4 - 3)
        Expr expression2 = new Expr.Binary(
                new Expr.Binary(
                        new Expr.Literal(1),
                        new Token(TokenType.PLUS, "+", null, 1),
                        new Expr.Literal(2)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Binary(
                        new Expr.Literal(4),
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(3)));
        assertEquals("(* (+ 1 2) (- 4 3))", printer.print(expression2));
        assertEquals("1 2 + 4 3 - *", printerRPN.print(expression2));
    }
}