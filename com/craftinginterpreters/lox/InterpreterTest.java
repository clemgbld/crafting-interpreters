package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Stmt.Print;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InterpreterTest {

    private final List<String> logs = new ArrayList<>();
    private final List<RuntimeError> errors = new ArrayList<>();
    @Test
    public void shouldBeAbleToSetAndGetStaticField(){
       Interpreter interpreter = new Interpreter(this::log,this::logError);
       interpreter.interpret(List.of(
               new Stmt.Class(new Token(TokenType.IDENTIFIER,"Math",null,1),List.of()),
               new Stmt.Expression(new Expr.Set(
                       new Expr.Variable(new Token(TokenType.IDENTIFIER,"Math",null,1)),
                       new Token(TokenType.IDENTIFIER,"pi",null,1),
                       new Expr.Literal(3.14))),
                    new Print(new Expr.Get(new Expr.Variable(new Token(TokenType.IDENTIFIER,"Math",null,1)),
                            new Token(TokenType.IDENTIFIER,"pi",null,1)
                    ))
       ));

       assertTrue(errors.isEmpty());
       assertEquals(Double.valueOf(3.14).toString(),logs.get(0));
    }

    private void log(String message){
        logs.add(message);
    }

    private void logError(RuntimeError runtimeError){
        errors.add(runtimeError);
    }
}
