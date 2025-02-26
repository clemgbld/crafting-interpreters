package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Stmt.Var;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParserTest {
    @Test
    public void shouldBeAbleToParseASimpleLiteral(){
        List<Token> tokens = List.of(
                new Token(TokenType.VAR, null, null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.STRING, "str", "str", 1),
                new Token(TokenType.SEMICOLON, null, null, 1),
                new Token(TokenType.EOF, null, null, 1)
        );

        Parser parser = new Parser(tokens);
        List<Stmt> result = parser.parse();


        assertEquals("str", ( (Literal) ( (Var) result.get(0)).initializer).value);
    }

    @Test
    public void shouldBeAbleToParseAnEmptyList(){
        List<Token> tokens = List.of(
                new Token(TokenType.VAR, null, null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Token(TokenType.EQUAL, "=", null, 1),
                new Token(TokenType.SEMICOLON, null, null, 1),
                new Token(TokenType.SEMICOLON, null, null, 1),
                new Token(TokenType.EOF, null, null, 1)
        );
    }
}
