package com.interpreters.lox;

import java.util.ArrayList;
import java.util.List;

/**
 * program        → declaration* EOF ;
 * declaration    → funDecl | varDecl | statement;
 * funDecl       → "fun" function
 * *  function    → IDENTIFIER "(" parameters? ")" block;
 * * * parameters → IDENTIFIER ("," IDENTIFIER)*；
 * valDecl        → "var" IDENTIFIER (“=” expression)?";" ;
 * statement      → exprStmt | printStmt | ifStmt | whileStmt | forStmt | breakStmt | block ;
 * exprStmt       → expression ";" ;
 * printStmt      → "print" expression ";" ;
 * ifStmt         → "if (" expression ") statement ("else" statement)?" ;
 * whileStmt      → "while ("  expression ")" statement;
 * forStmt        → "for ("  (valDecl | exprStmt | ";") expression? ";" expression? ")" statement;
 * breakStmt      → "break;" ;
 * block          → "{" declaration* "}" ;
 * expression     → assignment ;
 * assignment     → identifier "=" assignment | comma ;
 * comma          → ternary ( "," ternary )* ;
 * ternary        → logic_or ("?" expression ":" ternary)?;
 * logic_or       → logic_and ("or" logic_and)*;
 * logic_and      → equality ("and" equality)*;
 * equality       → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term           → factor ( ( "-" | "+" ) factor )* ;
 * factor         → unary ( ( "/" | "*" ) unary )* ;
 * unary          → ( "!" | "-" ) unary | call ;
 * call           → primary ("(" arguments? ")")* ;
 * * arguments      → expression ("," expression )* ;
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
    private int loopDepth = 0;

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

    // declaration    → funDecl | varDecl | statement;
    private Stmt declaration() {
        try {
            if (match(TokenType.FUN)) {
                return funDecl("function");
            } else if (match(TokenType.VAR)) {
                return varDecl();
            } else {
                return statement();
            }
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    /**
     * funDecl       → "fun" function
     * *  function    → IDENTIFIER "(" parameters? ")" block;
     * * * parameters → IDENTIFIER ("," IDENTIFIER)*；
     */
    private Stmt.Function funDecl(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name,");
        consume(TokenType.LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parmas = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parmas.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                parmas.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");
        consume(TokenType.LEFT_BRACE, "Expect '{' before " + kind + " body.");
        Stmt.Block body = block();
        return new Stmt.Function(name, parmas, body.getStatements());
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

    // statement      → exprStmt | printStmt | ifStmt | whileStmt | forStmt | block ;
    private Stmt statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
        } else if (match(TokenType.LEFT_BRACE)) {
            return block();
        } else if (match(TokenType.IF)) {
            return ifStmt();
        } else if (match(TokenType.WHILE)) {
            return whileStmt();
        } else if (match(TokenType.FOR)) {
            return forStmt();
        } else if (match(TokenType.BREAK)) {
            return breakStmt();
        }
        return expressionStatement();
    }

    // breakStmt      → "break;" ;
    private Stmt breakStmt() {
        if (loopDepth == 0) {
            error(previous(), "Must be inside a loop to use 'break'.");
        }
        consume(TokenType.SEMICOLON, "Expect ';' after break.");
        return new Stmt.Break();
    }

    // forStmt        → "for ("  (valDecl | exprStmt | ";") expression? ";" expression? ")" statement;
    private Stmt forStmt() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after for.");
        Stmt init;
        if (match(TokenType.SEMICOLON)) {
            init = null;
        } else if (match(TokenType.VAR)) {
            init = varDecl();
        } else {
            init = expressionStatement();
        }
        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition");
        Expr increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");
        try {
            loopDepth++;
            Stmt body = statement();
            if (increment != null) {
                body = new Stmt.Block(List.of(body, new Stmt.Expression(increment)));
            }
            if (condition == null) {
                condition = new Expr.Literal(true);
            }
            Stmt whileStmt = new Stmt.While(condition, body);
            if (init == null) {
                return whileStmt;
            } else {
                return new Stmt.Block(List.of(init, whileStmt));
            }
        } finally {
            loopDepth--;
        }
    }

    // whileStmt      → "while ("  expression ")" statement;
    private Stmt whileStmt() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after while.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after while condition");
        try {
            loopDepth++;
            Stmt body = statement();
            return new Stmt.While(condition, body);
        } finally {
            loopDepth--;
        }
    }

    // ifStmt         → "if (" expression ") statement ("else" statement)?" ;
    private Stmt ifStmt() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");
        Stmt thenBranch = statement(), elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // block          → "{" declaration* "}" ;
    private Stmt.Block block() {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            stmts.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return new Stmt.Block(stmts);
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
    // comma          → ternary ( "," ternary )* ;
    private Expr comma() {
        Expr expr = ternary();
        while (match(TokenType.COMMA)) {
            Token operator = previous();
            Expr right = ternary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // challenges 6.2
    // ternary        → logic_or ("?" expression ":" ternary)?;
    private Expr ternary() {
        Expr expr = logicOr();
        if (match(TokenType.QUESTION)) {
            Expr thenBranch = expression();
            consume(TokenType.COLON, "Expect ':' after then branch of ternary expression.");
            Expr elseBranch = ternary();
            expr = new Expr.Ternary(expr, thenBranch, elseBranch);
        }
        return expr;
    }

    // logic_or       → logic_and ("or" logic_and)*;
    private Expr logicOr() {
        Expr expr = logicAnd();
        while (match(TokenType.OR)) {
            Expr right = logicAnd();
            expr = new Expr.Logical(expr, previous(), right);
        }
        return expr;
    }

    // logic_and      → equality ("and" equality)*;
    private Expr logicAnd() {
        Expr left = equality();
        while (match(TokenType.AND)) {
            Expr right = equality();
            left = new Expr.Logical(left, previous(), right);
        }
        return left;
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

    // unary          → ( "!" | "-" ) unary | call ;
    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    // call           → primary ("(" arguments? ")")* ;
    // arguments      → expression ("," expression )* ;
    private Expr call() {
        Expr expr = primary();
        while (match(TokenType.LEFT_PAREN)) {
            expr = finishCall(expr);
        }
        return expr;
    }

    private Expr finishCall(Expr expr) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 augments.");
                }
                // should use higher precedence than comma
                arguments.add(ternary());
            } while (match(TokenType.COMMA));
        }
        Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after augments.");
        return new Expr.Call(expr, paren, arguments);
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
