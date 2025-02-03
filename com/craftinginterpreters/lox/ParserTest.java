package com.craftinginterpreters.lox;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldBeAbleToProduceStatements(){
        Parser parser = new Parser(List.of(
                new Token(TokenType.PRINT, null,null,1),
                new Token(TokenType.NUMBER, null,2,1),
                new Token(TokenType.SEMICOLON, null,null,1),
                new Token(TokenType.PRINT, null,null,1),
                new Token(TokenType.NUMBER, null,3,1),
                new Token(TokenType.SEMICOLON, null,null,1),
                new Token(TokenType.EOF, null,null,1)
        ));
        assertEquals(2, ((List<Stmt>) parser.parse()).size());
    }

    @Test
    public void shouldBeAbleToProduceAnExpression(){
        Parser parser = new Parser(List.of(
                new Token(TokenType.NUMBER, null,2,1),
                new Token(TokenType.PLUS, "2",null,1),
                new Token(TokenType.NUMBER, null,3,1),
                new Token(TokenType.EOF, null,null,1)
        ));

        assertTrue( (parser.parse()) instanceof Expr.Binary);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldBeNotInitialized(){
        Parser parser = new Parser(List.of(
                new Token(TokenType.VAR, null,null,1),
                new Token(TokenType.IDENTIFIER, "a",null,1),
                new Token(TokenType.SEMICOLON, null,null,1),
                new Token(TokenType.EOF, null,null,1)
        ));

        List<Stmt> statements = (List<Stmt>) parser.parse();
        Stmt.Var value = (Stmt.Var) statements.get(0);
        assertEquals(new Token(TokenType.IDENTIFIER, "a",null,1),value.name);
        Expr.NotInitialized notInitialized = (Expr.NotInitialized) value.initializer;
        assertEquals(new Token(TokenType.IDENTIFIER, "a",null,1),notInitialized.name);
    }


}
