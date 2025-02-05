package com.craftinginterpreters.lox;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
                new Token(TokenType.EOF, null,null, 1)
        ), this::logParseError);
        parser.parse();
        assertTrue(errors.isEmpty());
    }

}
