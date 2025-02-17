package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Stmt.Class;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ParserTest {
    private final List<Error> errors = new ArrayList<>();
    @Test
    public void shouldBeAbleToParseStaticMethod(){
        Parser parser = new Parser(List.of(
                new Token(TokenType.CLASS,null,null,1),
                new Token(TokenType.IDENTIFIER,"Math",null,1),
                new Token(TokenType.LEFT_BRACE,null,null,1),
                new Token(TokenType.CLASS,null,null,1),
                new Token(TokenType.IDENTIFIER,"square",null,1),
                new Token(TokenType.LEFT_PAREN,null,null,1),
                new Token(TokenType.RIGHT_PAREN,null,null,1),
                new Token(TokenType.LEFT_BRACE,null,null,1),
                new Token(TokenType.RIGHT_BRACE,null,null,1),
                new Token(TokenType.IDENTIFIER,"cube",null,1),
                new Token(TokenType.LEFT_PAREN,null,null,1),
                new Token(TokenType.RIGHT_PAREN,null,null,1),
                new Token(TokenType.LEFT_BRACE,null,null,1),
                new Token(TokenType.RIGHT_BRACE,null,null,1),
                new Token(TokenType.RIGHT_BRACE,null,null,1),
                new Token(TokenType.EOF,null,null,1)
        ),this::logError);

        var result = parser.parse();

        assertTrue(errors.isEmpty());
        assertTrue(((Class) result.get(0)).methods.get(0).isStatic);
        assertFalse(((Class) result.get(0)).methods.get(1).isStatic);
    }

    @Test
    public void shouldNotAllowTheWordClassInFrontOfAFunction(){
        Parser parser = new Parser(List.of(
                new Token(TokenType.FUN,null,null,1),
                new Token(TokenType.CLASS,null,null,1),
                new Token(TokenType.IDENTIFIER,"square",null,1),
                new Token(TokenType.LEFT_PAREN,null,null,1),
                new Token(TokenType.RIGHT_PAREN,null,null,1),
                new Token(TokenType.LEFT_BRACE,null,null,1),
                new Token(TokenType.RIGHT_BRACE,null,null,1),
                new Token(TokenType.EOF,null,null,1)
        ),this::logError);

        parser.parse();
        assertEquals(TokenType.CLASS,errors.get(0).name.type);
        assertEquals("Expect function name.",errors.get(0).message);
    }


    private void logError(Token name,String message){
        errors.add(new Error(name,message));
    }

    private record Error(Token name,String message){}
}
