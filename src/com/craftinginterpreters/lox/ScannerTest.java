package com.craftinginterpreters.lox;



import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class ScannerTest {

    List<Error> errors = new ArrayList<>();

    private void error(int line,String message ){
        errors.add(new Error(line,message));
    }

    @Test
    public void shouldBeaAbleToHandleMultiLinesComments(){
        Scanner scanner = new Scanner("/* something has been said */", this::error);
        assertEquals(scanner.scanTokens(),List.of(new Token(TokenType.EOF,"",null,1)));
    }

    @Test
    public void shouldStopTheCommentWhenThereIsAStartPlusASlash(){
        Scanner scanner = new Scanner("/* something has been said */ +", this::error);
        assertEquals(scanner.scanTokens(),List.of( new Token(TokenType.PLUS,"+",null,1),new Token(TokenType.EOF,"",null,1)));
    }

    @Test
    public void shouldNotStopTheCommentAtJustOneSlash(){
        Scanner scanner = new Scanner("/* something / */", this::error);
        assertEquals(scanner.scanTokens(),List.of(new Token(TokenType.EOF,"",null,1)));
    }

    @Test
    public void shouldDisplayAnErrorIfTheCommentNeverClose(){
        Scanner scanner = new Scanner("/* something", this::error);
        scanner.scanTokens();
        assertEquals(errors.get(0),new Error(1,"Unexpected multi line comment never closed"));
    }

    @Test
    public void shouldIncreaseTheLineNumberWhenEncounteringANewLineInAMultiLineComment(){
        Scanner scanner = new Scanner("/* something has \n been said */", this::error);
        assertEquals(scanner.scanTokens(),List.of(new Token(TokenType.EOF,"",null,2)));
    }

    @Test
    public void shouldBeAbleToHandleNestedComments(){
        Scanner scanner = new Scanner("/* nested  /* inside */  comments */", this::error);
        assertEquals(scanner.scanTokens(),List.of(new Token(TokenType.EOF,"",null,1)));
    }

    @Test
    public void shouldReportBecauseTheNestedCommentsIsNotClosed(){
        Scanner scanner = new Scanner("/* nested \n  /*  comments */", this::error);
        scanner.scanTokens();
        assertEquals(errors.get(0),new Error(2,"Unexpected multi line comment never closed"));
    }

    @Test
    public void shouldReportMultipleErrorsInADeeplyNestedComments(){
        Scanner scanner = new Scanner("/* nested \n  /* /*  comments */", this::error);
        scanner.scanTokens();
        assertEquals(errors.get(0),new Error(2,"Unexpected multi line comment never closed"));
        assertEquals(errors.get(1),new Error(2,"Unexpected multi line comment never closed"));
    }

    @Test
    public void shouldBeAbleToHandleDeeplyNestedComments(){
        Scanner scanner = new Scanner("/* nested  /* yo /* deep nesting */ */  comments */", this::error);
        assertEquals(scanner.scanTokens(),List.of(new Token(TokenType.EOF,"",null,1)));
    }

    record Error(int line, String message) {

    }



}