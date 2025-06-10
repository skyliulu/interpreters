package com.interpreters.lox;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @Test
    void testParse() {
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.NUMBER, "1", 1, 1));
        tokens.add(new Token(TokenType.PLUS, "+", null, 1));
        tokens.add(new Token(TokenType.NUMBER, "2", 2, 1));
        tokens.add(new Token(TokenType.EOF, "", null, 1));
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();
        AstPrinter printer = new AstPrinter();
        assertEquals("(+ 1 2)", printer.print(expression));
    }
}