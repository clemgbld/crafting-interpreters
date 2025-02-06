package com.craftinginterpreters.lox;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ParserTest {

    record Error(Token token,String message){}

    private final List<Error> errors = new ArrayList<>();


    @Test
    public void shouldThrowAnErrorWhenTryingToBreakOutsideOfALoop(){
        Parser parser  = new Parser(List.of(
                new Token(TokenType.BREAK, null, null,1),
                new Token(TokenType.EOF, null,null, 1)
        ), this::logParseError);
        parser.parse();
        assertEquals(TokenType.BREAK,errors.get(0).token.type);
        assertEquals("Break not in a loop.",errors.get(0).message);
    }

    private void logParseError(Token token, String message){
       errors.add(new Error(token,message));
    }

    @Test
    public void shouldBeAbleToParseTheSimplestBreakStatementInAWhileLoop(){
        Parser parser  = new Parser(List.of(
                new Token(TokenType.WHILE, null, null,1),
                new Token(TokenType.LEFT_PAREN, null, null,1),
                new Token(TokenType.TRUE,null, null,1),
                new Token(TokenType.RIGHT_PAREN, null, null,1),
                new Token(TokenType.BREAK, null, null,1),
                new Token(TokenType.SEMICOLON, null, null,1),
                new Token(TokenType.EOF, null,null, 1)
        ), this::logParseError);
        parser.parse();
        assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldThrowAnErrorWhenThereIsNoSemiColonAfterBreak(){
        Parser parser  = new Parser(List.of(
                new Token(TokenType.WHILE, null, null,1),
                new Token(TokenType.LEFT_PAREN, null, null,1),
                new Token(TokenType.TRUE,null, null,1),
                new Token(TokenType.RIGHT_PAREN, null, null,1),
                new Token(TokenType.BREAK, null, null,1),
                new Token(TokenType.EOF, null,null, 1)
        ), this::logParseError);
        parser.parse();
        assertEquals(TokenType.EOF,errors.get(0).token.type);
        assertEquals("Expect ';' after loop break.",errors.get(0).message);
    }

    @Test
    public void shouldBeAbleToParseWhileLoopWithoutBreakAndBlock(){
        Parser parser  = new Parser(List.of(
                new Token(TokenType.WHILE, null, null,1),
                new Token(TokenType.LEFT_PAREN, null, null,1),
                new Token(TokenType.TRUE,null, null,1),
                new Token(TokenType.RIGHT_PAREN, null, null,1),
                new Token(TokenType.LEFT_BRACE, null, null,1),
                new Token(TokenType.BREAK, null, null,1),
                new Token(TokenType.SEMICOLON, null, null,1),
                new Token(TokenType.RIGHT_BRACE, null, null,1),
                new Token(TokenType.EOF, null,null, 1)
        ), this::logParseError);
        parser.parse();
        assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldBeAbleToParseWhileLoopWithoutBreakAndIfElse(){
        Parser parser  = new Parser(List.of(
                new Token(TokenType.WHILE, null, null,1),
                new Token(TokenType.LEFT_PAREN, null, null,1),
                new Token(TokenType.TRUE,null, null,1),
                new Token(TokenType.RIGHT_PAREN, null, null,1),
                new Token(TokenType.IF, null, null,1),
                new Token(TokenType.LEFT_PAREN, null, null,1),
                new Token(TokenType.TRUE, null, null,1),
                new Token(TokenType.RIGHT_PAREN, null, null,1),
                new Token(TokenType.BREAK, null, null,1),
                new Token(TokenType.SEMICOLON, null, null,1),
                new Token(TokenType.ELSE, null, null,1),
                new Token(TokenType.LEFT_BRACE, null, null,1),
                new Token(TokenType.BREAK, null, null,1),
                new Token(TokenType.SEMICOLON, null, null,1),
                new Token(TokenType.RIGHT_BRACE, null, null,1),
                new Token(TokenType.EOF, null,null, 1)
        ), this::logParseError);
        parser.parse();
        assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldBeAbleToBreakInAForLoop(){
        Parser parser  = new Parser(List.of(
                new Token(TokenType.FOR, null, null,1),
                new Token(TokenType.LEFT_PAREN, null, null,1),
                new Token(TokenType.SEMICOLON, null, null,1),
                new Token(TokenType.SEMICOLON, null, null,1),
                new Token(TokenType.RIGHT_PAREN, null, null,1),
                new Token(TokenType.BREAK, null, null,1),
                new Token(TokenType.SEMICOLON, null, null,1),
                new Token(TokenType.EOF, null,null, 1)
        ), this::logParseError);
        parser.parse();
        System.out.println(errors);
        assertTrue(errors.isEmpty());
    }

}
