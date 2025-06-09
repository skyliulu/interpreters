package com.interpreters.lox;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScannerTest {

    @Test
    void scanTokens() {
        Scanner scanner = new Scanner("// comment\n1 + 2");
        List<Token> tokens = scanner.scanTokens();
        assertEquals(5, tokens.size());
        assertEquals(TokenType.COMMENT, tokens.get(0).getTokenType());
        assertEquals(TokenType.NUMBER, tokens.get(1).getTokenType());
        assertEquals(TokenType.PLUS, tokens.get(2).getTokenType());
        assertEquals(TokenType.NUMBER, tokens.get(3).getTokenType());
        assertEquals(TokenType.EOF, tokens.get(4).getTokenType());
    }

    @Test
    void scanBlockComment() {
        Scanner scanner = new Scanner("/* comment */\n1 + 2");
        List<Token> tokens = scanner.scanTokens();
        assertEquals(5, tokens.size());
        assertEquals(TokenType.COMMENT, tokens.get(0).getTokenType());
        assertEquals(TokenType.NUMBER, tokens.get(1).getTokenType());
        assertEquals(TokenType.PLUS, tokens.get(2).getTokenType());
        assertEquals(TokenType.NUMBER, tokens.get(3).getTokenType());
        assertEquals(TokenType.EOF, tokens.get(4).getTokenType());
        scanner = new Scanner("/* comment\nmulti line\n /* nest comment */ */1 + 2");
        tokens = scanner.scanTokens();
        assertEquals(5, tokens.size());
        assertEquals(TokenType.COMMENT, tokens.get(0).getTokenType());
        assertEquals(TokenType.NUMBER, tokens.get(1).getTokenType());
        assertEquals(TokenType.PLUS, tokens.get(2).getTokenType());
        assertEquals(TokenType.NUMBER, tokens.get(3).getTokenType());
        assertEquals(TokenType.EOF, tokens.get(4).getTokenType());
    }
}