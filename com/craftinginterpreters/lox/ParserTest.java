package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.GetList;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.LoxList;
import com.craftinginterpreters.lox.Expr.SetList;
import com.craftinginterpreters.lox.Stmt.Var;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserTest {
    private final List<Error> errors = new ArrayList<>();
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

        Parser parser = new Parser(tokens, this::logError);
        List<Stmt> result = parser.parse();


        assertEquals("str", ( (Literal) ( (Var) result.get(0)).initializer).value);
    }

    @Test
    public void shouldBeAbleToParseAnEmptyList(){
        List<Token> tokens = List.of(
                new Token(TokenType.VAR, null, null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 2),
                new Token(TokenType.EQUAL, "=", null, 3),
                new Token(TokenType.LEFT_BRACKET, null, null, 4),
                new Token(TokenType.RIGHT_BRACKET, null, null, 5),
                new Token(TokenType.SEMICOLON, null, null, 5),
                new Token(TokenType.EOF, null, null, 6)
        );

        Parser parser = new Parser(tokens, this::logError);
        List<Stmt> result = parser.parse();


        assertEquals(List.of(), ( (LoxList) ( (Var) result.get(0)).initializer).exprs);
    }

    @Test
    public void shouldBeAbleToParseAList(){
        List<Token> tokens = List.of(
                new Token(TokenType.VAR, null, null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 2),
                new Token(TokenType.EQUAL, "=", null, 3),
                new Token(TokenType.LEFT_BRACKET, null, null, 4),
                new Token(TokenType.NUMBER, null, 1.0, 4),
                new Token(TokenType.COMMA, null, null, 5),
                new Token(TokenType.NUMBER, null, 2.0, 4),
                new Token(TokenType.COMMA, null, null, 5),
                new Token(TokenType.NUMBER, null, 3.0, 4),
                new Token(TokenType.RIGHT_BRACKET, null, null, 5),
                new Token(TokenType.SEMICOLON, null, null, 5),
                new Token(TokenType.EOF, null, null, 6)
        );

        Parser parser = new Parser(tokens, this::logError);
        List<Stmt> result = parser.parse();

        assertEquals(1.0, ((Literal) ( (LoxList) ( (Var) result.get(0)).initializer).exprs.get(0)).value);
        assertEquals(2.0, ((Literal) ( (LoxList) ( (Var) result.get(0)).initializer).exprs.get(1)).value);
        assertEquals(3.0, ((Literal) ( (LoxList) ( (Var) result.get(0)).initializer).exprs.get(2)).value);
    }

    @Test
    public void shouldEnforceRightBracketAtTheEndOfAList(){
            List<Token> tokens = List.of(
                    new Token(TokenType.VAR, null, null, 1),
                    new Token(TokenType.IDENTIFIER, "x", null, 2),
                    new Token(TokenType.EQUAL, "=", null, 3),
                    new Token(TokenType.LEFT_BRACKET, null, null, 4),
                    new Token(TokenType.NUMBER, null, 1.0, 4),
                    new Token(TokenType.COMMA, null, null, 5),
                    new Token(TokenType.NUMBER, null, 2.0, 4),
                    new Token(TokenType.COMMA, null, null, 5),
                    new Token(TokenType.NUMBER, null, 3.0, 4),
                    new Token(TokenType.SEMICOLON, null, null, 5),
                    new Token(TokenType.EOF, null, null, 6)
            );

        Parser parser = new Parser(tokens, this::logError);
        parser.parse();
        assertEquals(TokenType.SEMICOLON,errors.get(0).name.type);
        assertEquals("Expect ']' after list items.",errors.get(0).message);
    }

    @Test
    public void shouldBeAbleToParseTheGetOfAnItemInAList(){
        List<Token> tokens = List.of(
                new Token(TokenType.VAR, null, null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 2),
                new Token(TokenType.EQUAL, "=", null, 3),
                new Token(TokenType.LEFT_BRACKET, null, null, 4),
                new Token(TokenType.NUMBER, null, 1.0, 4),
                new Token(TokenType.RIGHT_BRACKET, null, null, 4),
                new Token(TokenType.LEFT_BRACKET, null, null, 10),
                new Token(TokenType.NUMBER, null, 0.0, 4),
                new Token(TokenType.RIGHT_BRACKET, null, null, 4),
                new Token(TokenType.SEMICOLON, null, null, 5),
                new Token(TokenType.EOF, null, null, 6)
        );
        Parser parser = new Parser(tokens, this::logError);
        var result = parser.parse();
        assertEquals(1.0,((Literal) ((LoxList) ((GetList) ((Var) result.get(0)).initializer).object).exprs.get(0)).value);
        assertEquals(0.0, ((Literal) ( (GetList) ( (Var) result.get(0)).initializer).index).value);
        assertEquals(10, ((Token) ( (GetList) ( (Var) result.get(0)).initializer).name).line);
    }

    @Test
    public void shouldForAGetListToFinishByAnEmptyBracket(){
        List<Token> tokens = List.of(
                new Token(TokenType.VAR, null, null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 2),
                new Token(TokenType.EQUAL, "=", null, 3),
                new Token(TokenType.LEFT_BRACKET, null, null, 4),
                new Token(TokenType.NUMBER, null, 1.0, 4),
                new Token(TokenType.RIGHT_BRACKET, null, null, 4),
                new Token(TokenType.LEFT_BRACKET, null, null, 4),
                new Token(TokenType.NUMBER, null, 0.0, 4),
                new Token(TokenType.SEMICOLON, null, null, 5),
                new Token(TokenType.EOF, null, null, 6)
        );
        Parser parser = new Parser(tokens, this::logError);
        parser.parse();
        assertEquals(TokenType.SEMICOLON,errors.get(0).name.type);
        assertEquals("Expect ']' after list indexing.",errors.get(0).message);

    }

    @Test
    public void shouldBeAbleToParseSetList(){
        List<Token> tokens = List.of(
                new Token(TokenType.VAR, null, null, 1),
                new Token(TokenType.IDENTIFIER, "x", null, 2),
                new Token(TokenType.EQUAL, "=", null, 3),
                new Token(TokenType.LEFT_BRACKET, null, null, 4),
                new Token(TokenType.NUMBER, null, 1.0, 4),
                new Token(TokenType.RIGHT_BRACKET, null, null, 4),
                new Token(TokenType.LEFT_BRACKET, null, null, 10),
                new Token(TokenType.NUMBER, null, 0.0, 4),
                new Token(TokenType.RIGHT_BRACKET, null, null, 4),
                new Token(TokenType.EQUAL, "=", null, 5),
                new Token(TokenType.NUMBER, null, 1.5, 5),
                new Token(TokenType.SEMICOLON, null, null, 5),
                new Token(TokenType.EOF, null, null, 6)
        );
        Parser parser = new Parser(tokens, this::logError);
        var result = parser.parse();
        assertEquals(1.0,((Literal) ((LoxList) ((SetList) ((Var) result.get(0)).initializer).object).exprs.get(0)).value);
        assertEquals(0.0, ((Literal) ( (SetList) ( (Var) result.get(0)).initializer).index).value);
        assertEquals(10, ((Token) ( (SetList) ( (Var) result.get(0)).initializer).name).line);
        assertEquals(1.5, ((Literal) ( (SetList) ( (Var) result.get(0)).initializer).value).value);
    }

    private void logError(Token name, String message){
        errors.add(new Error(name,message));
    }

    private record Error(Token name,String message){}

}
