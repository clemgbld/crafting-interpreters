package com.craftinginterpreters.lox;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ParserTest.ParsingTest.class,
        ParserTest.OperatorErrorTest.class
})
public class ParserTest {

    @RunWith(JUnit4.class)
    static public  class ParsingTest  {
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

        @Test
        public void shouldHandleMisplacedOperatorTokenPlaceAtTheBeginningWhenThereIsNoMoreTokensToParseAfter(){
            List<Token> tokens = List.of(
                    new Token(TokenType.PLUS,"+",null,1),
                    new Token(TokenType.NUMBER, null, 2, 1),
                    new Token(TokenType.EOF, "\0", null, 1)
            );
            Parser parser = new Parser(tokens,this::error);
            var result = parser.parse();
            assertNull(result);
            assertEquals(errors.get(0), new Error(new Token(TokenType.NUMBER, null, 2, 1), "+ operator detected at the beginning of an expression."));
        }


    }

    @RunWith(Parameterized.class)
    static public  class OperatorErrorTest  {
        List<Error> errors = new ArrayList<>();

        private void error(Token token,String message ){
            errors.add(new Error(token,message));
        }

        private final TokenType type;

        private final String lexeme;

        public OperatorErrorTest(TokenType type, String lexeme) {
            this.type = type;
            this.lexeme = lexeme;
        }

        @Test
        public void shouldReportAnErrorDiscardTheRightEndOperandForABinaryOperator(){
            List<Token> tokens = List.of(
                    new Token(type,lexeme,null,1),
                    new Token(TokenType.NUMBER, null, 2, 1),
                    new Token(TokenType.NUMBER, null, 3, 1),
                    new Token(TokenType.PLUS,"+",null,1),
                    new Token(TokenType.NUMBER, null, 1, 1),
                    new Token(TokenType.EOF, "\0", null, 1)
            );

            Parser parser = new Parser(tokens,this::error);
            var result = parser.parse();
            assertEquals("(+ 3 1)",new AstPrinter().print(result));
            assertEquals(errors.get(0), new Error(new Token(TokenType.NUMBER, null, 2, 1),lexeme + " operator detected at the beginning of an expression."));
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {TokenType.PLUS, "+"},
                    {TokenType.SLASH, "/"},
                    {TokenType.QUESTION_MARK, "?"},
                    {TokenType.COLON, ":"},
                    {TokenType.COMMA, ","},
                    {TokenType.BANG_EQUAL, "=!"},
                    {TokenType.EQUAL_EQUAL, "=="},
                    {TokenType.LESS, "<"},
                    {TokenType.GREATER, ">"},
                    {TokenType.GREATER_EQUAL, ">="},
                    {TokenType.LESS_EQUAL, "<="},
            });
        }

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
