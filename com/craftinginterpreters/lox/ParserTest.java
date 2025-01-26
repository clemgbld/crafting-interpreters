package com.craftinginterpreters.lox;


import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParserTest {

    @Test
    public void shouldBeAbleToParseSimpleBlocks(){
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.NUMBER,null,1,1));
        tokens.add(new Token(TokenType.EQUAL_EQUAL,"==",null,1));
        tokens.add(new Token(TokenType.NUMBER,null,2,1));
        tokens.add(new Token(TokenType.COMMA,",",null,1));
        tokens.add(new Token(TokenType.NUMBER,null,3,1));
        tokens.add(new Token(TokenType.EOF,"\0",null,1));
        Parser parser = new Parser(tokens);

        assertEquals(new AstPrinter().print(parser.parse()), "(, (== 1 2) 3)");
    }

    @Test
    public void shouldBeAbleToParseMultipleBlocks(){
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.NUMBER,null,1,1));
        tokens.add(new Token(TokenType.EQUAL_EQUAL,"==",null,1));
        tokens.add(new Token(TokenType.NUMBER,null,2,1));
        tokens.add(new Token(TokenType.COMMA,",",null,1));
        tokens.add(new Token(TokenType.NUMBER,null,3,1));
        tokens.add(new Token(TokenType.EQUAL_EQUAL,"==",null,1));
        tokens.add(new Token(TokenType.NUMBER,null,2,1));
        tokens.add(new Token(TokenType.COMMA,",",null,1));
        tokens.add(new Token(TokenType.NUMBER,null,3,1));
        tokens.add(new Token(TokenType.EOF,"\0",null,1));
        Parser parser = new Parser(tokens);

        assertEquals(new AstPrinter().print(parser.parse()), "(, (, (== 1 2) (== 3 2)) 3)");
    }

}
