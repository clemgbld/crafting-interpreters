package com.craftinginterpreters.lox;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParserTest {

    @Test
    public void shouldBeAbleToParseOnInheritance(){
       Parser parser = new Parser(
               List.of(
                       new Token(TokenType.CLASS,null,null,1),
                       new Token(TokenType.IDENTIFIER,"Math",null,1),
                       new Token(TokenType.LESS,null,null,1),
                       new Token(TokenType.IDENTIFIER,"SetTheory",null,1),
                       new Token(TokenType.LEFT_BRACE,null,null,1),
                       new Token(TokenType.RIGHT_BRACE,null,null,1),
                       new Token(TokenType.EOF,null,null,1)
               )
       );

       List<Stmt> result = parser.parse();
       Stmt.Class first = (Stmt.Class) result.get(0);
       assertEquals("SetTheory", first.superClass.get(0).name.lexeme);
    }

    @Test
    public void shouldBeAbleToParseMultipleLevelOfInheritance(){
        Parser parser = new Parser(
                List.of(
                        new Token(TokenType.CLASS,null,null,1),
                        new Token(TokenType.IDENTIFIER,"Math",null,1),
                        new Token(TokenType.LESS,null,null,1),
                        new Token(TokenType.IDENTIFIER,"SetTheory",null,1),
                        new Token(TokenType.COMMA,null,null,1),
                        new Token(TokenType.IDENTIFIER,"Physics",null,1),
                        new Token(TokenType.COMMA,null,null,1),
                        new Token(TokenType.IDENTIFIER,"Learning",null,1),
                        new Token(TokenType.LEFT_BRACE,null,null,1),
                        new Token(TokenType.RIGHT_BRACE,null,null,1),
                        new Token(TokenType.EOF,null,null,1)
                )
        );

        List<Stmt> result = parser.parse();
        Stmt.Class first = (Stmt.Class) result.get(0);
        assertEquals("SetTheory", first.superClass.get(0).name.lexeme);
        assertEquals("Physics", first.superClass.get(1).name.lexeme);
        assertEquals("Learning", first.superClass.get(2).name.lexeme);
    }
}
