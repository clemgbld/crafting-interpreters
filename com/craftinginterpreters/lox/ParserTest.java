package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Lambda;
import com.craftinginterpreters.lox.Stmt.Function;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParserTest {
    private final List<Error> errors = new ArrayList<>();

    @Test
    public void shouldSuccessfullyParseAFunctionStatement(){
        Parser parser = new Parser(List.of(
                new Token(TokenType.FUN,null,null,1),
               new Token(TokenType.IDENTIFIER,"count",null,1),
                new Token(TokenType.LEFT_PAREN,null,null,1),
                new Token(TokenType.RIGHT_PAREN,null,null,1),
                new Token(TokenType.LEFT_BRACE,null,null,1),
                new Token(TokenType.RIGHT_BRACE,null,null,1),
                new Token(TokenType.EOF,null,null,1)
                ),this::error);

        var result = parser.parse();
        assertTrue(errors.isEmpty());
        assertTrue(result.get(0) instanceof Function);
    }

    @Test
    public void shouldParseLambda(){
        Parser parser = new Parser(List.of(
                new Token(TokenType.FUN,null,null,1),
                new Token(TokenType.LEFT_PAREN,null,null,1),
                new Token(TokenType.RIGHT_PAREN,null,null,1),
                new Token(TokenType.LEFT_BRACE,null,null,1),
                new Token(TokenType.RIGHT_BRACE,null,null,1),
                new Token(TokenType.EOF,null,null,1)
        ),this::error);

        parser.parse();
        assertFalse(errors.isEmpty());
    }

    @Test
    public void shouldAllowLambdaAsExpression(){
        Parser parser = new Parser(List.of(
                new Token(TokenType.VAR,null,null,1),
                new Token(TokenType.IDENTIFIER,"a",null,1),
                new Token(TokenType.EQUAL,null,null,1),
                new Token(TokenType.FUN,null,null,1),
                new Token(TokenType.LEFT_PAREN,null,null,1),
                new Token(TokenType.RIGHT_PAREN,null,null,1),
                new Token(TokenType.LEFT_BRACE,null,null,1),
                new Token(TokenType.RIGHT_BRACE,null,null,1),
                new Token(TokenType.SEMICOLON,null,null,1),
                new Token(TokenType.EOF,null,null,1)
        ),this::error);
        var result = parser.parse();
        assertTrue(errors.isEmpty());
        Stmt.Var variable = (Stmt.Var) result.get(0);
        assertTrue(variable.initializer instanceof Lambda);
    }

    @Test
    public void shouldNotAllowInAStatement(){
        Parser parser = new Parser(List.of(
                new Token(TokenType.FUN,null,null,1),
                new Token(TokenType.LEFT_PAREN,null,null,1),
                new Token(TokenType.RIGHT_PAREN,null,null,1),
                new Token(TokenType.LEFT_BRACE,null,null,1),
                new Token(TokenType.RIGHT_BRACE,null,null,1),
                new Token(TokenType.SEMICOLON,null,null,1),
                new Token(TokenType.EOF,null,null,1)
        ),this::error);

         parser.parse();
        assertFalse(errors.isEmpty());
    }


    private void error(Token token,String message){
       errors.add(new Error(token,message));
    }
    record Error(Token token, String message){}

}
