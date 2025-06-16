package com.interpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Challenges:
 * 1. The lexical grammars of Python and Haskell are not regular.
 * What does that mean, and why aren’t they?
 * <p>
 * 对于 Python 和 Haskell 来说，使其词法文法非正则的特性是对上下文敏感的缩进。需要记住一个先前缩进级别的栈，
 * 意味着一个简单的、基于正则表达式的工具（如 lex 或 flex）本身是不足以胜任的。
 * 在实践中，这些语言的词法分析器通常是以下两者的结合体：
 * 一个传统的有限自动机，用于识别基本的词元（标识符、数字、运算符、关键字）。
 * 一个手动编码的逻辑层，它管理缩进栈，并将 INDENT/DEDENT 或虚拟花括号/分号等词元注入到传递给语法分析器的词元流中。
 * <p>
 * 3. Our scanner here, like most, discards comments and whitespace since those aren’t needed by the parser.
 * Why might you want to write a scanner that does not discard those? What would it be useful for?
 * <p>
 * 不丢弃注释和空白的扫描器，其价值在于它能够为对源代码的非逻辑结构和人类可读信息敏感的工具提供所需的数据。
 * 它使得对代码进行更深层次的分析、转换和展示成为可能，从而提高了开发效率和代码质量。
 * 保留注释和空白的扫描器以下几个方面会非常有用：
 * 1. 代码美化器/格式化器 (Code Beautifiers/Formatters)：
 * 2. 文档生成器 (Documentation Generators)：
 * 3. 代码分析工具 (Code Analysis Tools)
 * 4. 源代码转换/重构工具 (Source Code Transformation/Refactoring Tools)
 * 5. 语法高亮显示器 (Syntax Highlighters)
 * 6. 版本控制系统 (Version Control Systems) 的高级差异比较
 */
public class Scanner {
    private static final Map<String, TokenType> keyWords = new HashMap<>();

    static {
        keyWords.put("and", TokenType.AND);
        keyWords.put("class", TokenType.CLASS);
        keyWords.put("else", TokenType.ELSE);
        keyWords.put("false", TokenType.FALSE);
        keyWords.put("for", TokenType.FOR);
        keyWords.put("fun", TokenType.FUN);
        keyWords.put("if", TokenType.IF);
        keyWords.put("nil", TokenType.NIL);
        keyWords.put("or", TokenType.OR);
        keyWords.put("print", TokenType.PRINT);
        keyWords.put("return", TokenType.RETURN);
        keyWords.put("super", TokenType.SUPER);
        keyWords.put("this", TokenType.THIS);
        keyWords.put("true", TokenType.TRUE);
        keyWords.put("var", TokenType.VAR);
        keyWords.put("while", TokenType.WHILE);
        keyWords.put("break", TokenType.BREAK);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char ch = advance();
        switch (ch) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '?' -> addToken(TokenType.QUESTION);
            case ':' -> addToken(TokenType.COLON);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '*' -> addToken(TokenType.STAR);
            case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
            case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '/' -> {
                if (match('/')) {
                    lineComment();
                } else if (match('*')) {
                    blockComment();
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            case ' ', '\r', '\t' -> {
            }
            case '\n' -> line++;
            case '"' -> string();
            default -> {
                if (isDigit(ch)) {
                    number();
                } else if (isAlpha(ch)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
            }
        }
    }

    // one line comment: // comment
    private void lineComment() {
        while (peek() != '\n' && !isAtEnd()) {
            advance();
        }
        addToken(TokenType.COMMENT, source.substring(start + 2, current).trim());
    }

    /**
     * Challenges:
     * 4. Add support to Lox’s scanner for C-style \/* ... *\/
     * block comments. Make sure to handle newlines in them.
     */
    private void blockComment() {
        int stack = 1;
        while (!isAtEnd()) {
            if (peek() == '\n') line++;
            if (peek() == '/' && peekNext() == '*') {
                stack++;
            }
            if (peek() == '*' && peekNext() == '/') {
                stack--;
            }
            if (stack == 0) {
                break;
            }
            advance();
        }
        advance();
        advance();
        addToken(TokenType.COMMENT, source.substring(start + 2, current - 2).trim());
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }
        advance();
        String str = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, str);
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }
        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        String text = source.substring(start, current);
        TokenType type = keyWords.get(text);
        if (type == null) {
            type = TokenType.IDENTIFIER;
        }
        addToken(type);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }
        current++;
        return true;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(new Token(type, source.substring(start, current), literal, line));
    }
}
