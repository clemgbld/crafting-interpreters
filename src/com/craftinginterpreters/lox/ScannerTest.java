package com.craftinginterpreters.lox;



import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;


public class ScannerTest {

    List<Error> errors = new ArrayList<>();

    private void error(int line,String message ){
        errors.add(new Error(line,message));
    }

    @Test
    public void shouldBeAbleToHandleNestedComments(){
        Scanner scanner = new Scanner("/* something has been said */", this::error);
        assertTrue(scanner.scanTokens().isEmpty());
    }

    record Error(int line, String message) {

    }



}