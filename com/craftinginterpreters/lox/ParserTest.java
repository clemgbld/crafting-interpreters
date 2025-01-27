package com.craftinginterpreters.lox;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ParserTest {

    List<Error> errors = new ArrayList<>();

    private void error(Token token,String message ){
        errors.add(new Error(token,message));
    }

    @Test
    public void shouldBeAbleToParseSimpleBlocks() {
        List<Token> tokens = List.of(
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.COMMA, ",", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.EOF, "\0", null, 1)
        );
        Parser parser = new Parser(tokens,this::error);

        assertEquals(new AstPrinter().print(parser.parse()), "(, (== 1 2) 3)");
    }

    @Test
    public void shouldBeAbleToParseMultipleBlocks() {
        List<Token> tokens = List.of(
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.COMMA, ",", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.COMMA, ",", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.EOF, "\0", null, 1)
        );
        Parser parser = new Parser(tokens,this::error);

        assertEquals(new AstPrinter().print(parser.parse()), "(, (, (== 1 2) (== 3 2)) 3)");
    }

    @Test
    public void shouldParseSimpleTernaryOperator() {
        List<Token> tokens = List.of(
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.COMMA, ",", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.COMMA, ",", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.QUESTION_MARK, "?", null, 1),
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.COMMA, ",", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.COMMA, ",", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.COLON, ":", null, 1),
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.COMMA, ",", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.COMMA, ",", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.EOF, "\0", null, 1)
        );
        Parser parser = new Parser(tokens,this::error);

        var result = parser.parse();

        assertEquals(new AstPrinter().print(result), "(? (, (, (== 1 2) (== 3 2)) 3) (: (, (, (== 1 2) (== 3 2)) 3) (, (, (== 1 2) (== 3 2)) 3)))");
    }

    @Test
    public void shouldParseNestedTernaryForTheFirstCase() {
        List<Token> tokens = List.of(
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.QUESTION_MARK, "?", null, 1),
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.QUESTION_MARK, "?", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.COLON, ":", null, 1),
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.COLON, ":", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.EOF, "\0", null, 1)
        );
        Parser parser = new Parser(tokens,this::error);

        assertEquals(new AstPrinter().print(parser.parse()), "(? (== 1 2) (: (? (== 1 2) (: 2 1)) 3))");
    }

    @Test
    public void shouldParseNestedTernaryForTheSecondCase() {
        List<Token> tokens = List.of(
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.QUESTION_MARK, "?", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.COLON, ":", null, 1),
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.QUESTION_MARK, "?", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.COLON, ":", null, 1),
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EOF, "\0", null, 1)
        );
        Parser parser = new Parser(tokens,this::error);

        assertEquals(new AstPrinter().print(parser.parse()), "(? (== 1 2) (: 3 (? (== 1 2) (: 2 1))))");
    }

    @Test
    public void shouldHaveAnErrorWhenTheColumnIsMissing() {
        List<Token> tokens = List.of(
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.QUESTION_MARK, "?", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EOF, "\0", null, 1)
        );
        Parser parser = new Parser(tokens,this::error);
        assertNull(parser.parse());
        assertEquals(errors.get(0), new Error(new Token(TokenType.NUMBER, null, 1, 1),"Expect ':' after expression."));
    }

    @Test
    public void shouldHaveAnErrorWhenTheLastTokenIsAColon() {
        List<Token> tokens = List.of(
                new Token(TokenType.NUMBER, null, 1, 1),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Token(TokenType.NUMBER, null, 2, 1),
                new Token(TokenType.QUESTION_MARK, "?", null, 1),
                new Token(TokenType.NUMBER, null, 3, 1),
                new Token(TokenType.COLON, ":", null, 1),
                new Token(TokenType.EOF, "\0", null, 1)
        );
        Parser parser = new Parser(tokens,this::error);
        assertNull(parser.parse());
        assertEquals(errors.get(0), new Error(new Token(TokenType.EOF, "\0", null, 1),"Expect expression."));
    }



    record Error(Token token, String message) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Error error)) return false;
            return Objects.equals(token, error.token) && Objects.equals(message, error.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(token, message);
        }
    }

}
