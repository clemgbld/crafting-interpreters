package com.craftinginterpreters.lox;

import java.util.Objects;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Token token)) return false;
        return line == token.line && type == token.type && Objects.equals(lexeme, token.lexeme) && Objects.equals(literal, token.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, lexeme, literal, line);
    }
}
