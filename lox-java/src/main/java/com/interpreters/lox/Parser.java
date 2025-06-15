package com.interpreters.lox;

import java.util.ArrayList;
import java.util.List;

/**
 * program        → declaration* EOF ;
 * declaration    → varDecl | statement;
 * valDecl        → "var" IDENTIFIER (“=” expression)?";" ;
 * statement      → exprStmt | printStmt ;
 * exprStmt       → expression ";" ;
 * printStmt      → "print" expression ";" ;
 * expression     → assignment ;
 * assignment     → identifier "=" assignment | comma ;
 * comma          → conditional ( "," conditional )* ;
 * conditional    → equality ("?" expression ":" conditional)?;
 * equality       → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term           → factor ( ( "-" | "+" ) factor )* ;
 * factor         → unary ( ( "/" | "*" ) unary )* ;
 * unary          → ( "!" | "-" ) unary | primary ;
 * primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER
 * // error productions
 * | ( "!=" | "==" ) equality
 * | ( ">" | ">=" | "<" | "<=" ) comparison
 * | "+" term
 * | ( "/" | "*" ) factor;
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            stmts.add(declaration());
        }
        return stmts;
    }

    // declaration    → varDecl | statement;
    private Stmt declaration() {
        try {
            if (match(TokenType.VAR)) {
                return varDecl();
            } else {
                return statement();
            }
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    // valDecl        → "var" IDENTIFIER (“=” expression)?";" ;
    private Stmt varDecl() {
        Token token = consume(TokenType.IDENTIFIER, "Expect variable name");
        Expr initialize = null;
        if (match(TokenType.EQUAL)) {
            initialize = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(token, initialize);
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
        }
        return expressionStatement();
    }

    // printStmt      → "print" expression ";" ;
    private Stmt printStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(expr);
    }

    // exprStmt       → expression ";" ;
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    // expression     → equality ;
    private Expr expression() {
        return assignment();
    }

    // assignment     → identifier "=" assignment | comma ;
    private Expr assignment() {
        Expr expr = comma();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            // valid left expr
            if (expr instanceof Expr.Variable variable) {
                return new Expr.Assignment(variable.getName(), value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    // challenges 6.1
    // comma          → conditional ( "," conditional )* ;
    private Expr comma() {
        Expr expr = conditional();
        while (match(TokenType.COMMA)) {
            Token operator = previous();
            Expr right = conditional();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // challenges 6.2
    // conditional    → equality "?" expression ":" conditional ;
    private Expr conditional() {
        Expr expr = equality();
        if (match(TokenType.QUESTION)) {
            Expr thenBranch = expression();
            consume(TokenType.COLON, "Expect ':' after then branch of conditional expression.");
            Expr elseBranch = conditional();
            expr = new Expr.Conditional(expr, thenBranch, elseBranch);
        }
        return expr;
    }

    // equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // term           → factor ( ( "-" | "+" ) factor )* ;
    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // factor         → unary ( ( "/" | "*" ) unary )* ;
    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // unary          → ( "!" | "-" ) unary | primary ;
    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    /**
     * primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | identifier
     * | ( "!=" | "==" ) equality
     * | ( ">" | ">=" | "<" | "<=" ) comparison
     * | "+" term
     * | ( "/" | "*" ) factor;
     * challenge 6.3
     * With the normal infix productions, the operand non-terminals are one precedence level higher than the operator's own precedence.
     * In order to handle a series of operators of the same precedence, the rules explicitly allow repetition.
     * With the error productions, though, the right-hand operand rule is the same precedence level.
     * That will effectively strip off the erroneous leading operator and then consume a series of infix uses of operators at the same level by reusing the existing correct rule.
     */
    private Expr primary() {
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().getLiteral());
        }
        if (match(TokenType.TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(TokenType.FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TokenType.NIL)) {
            return new Expr.Literal(null);
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        // error production
        if (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            error(previous(), "Missing left-hand operand");
            equality();
            return null;
        }
        if (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            error(previous(), "Missing left-hand operand");
            comparison();
            return null;
        }
        if (match(TokenType.PLUS)) {
            error(previous(), "Missing left-hand operand");
            term();
            return null;
        }
        if (match(TokenType.SLASH, TokenType.STAR)) {
            error(previous(), "Missing left-hand operand");
            factor();
            return null;
        }
        throw error(peek(), "Expect expression.");
    }

    // error recovery synchronization
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            // statement finnish
            if (previous().getTokenType().equals(TokenType.SEMICOLON)) return;
            // or start new statement
            switch (peek().getTokenType()) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

    private static class ParseError extends RuntimeException {
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getTokenType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getTokenType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
