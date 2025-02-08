package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Stmt.Function;
import com.craftinginterpreters.lox.Stmt.Print;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InterpreterTest {

    private final List<String> logs = new ArrayList<>();
    private final List<RuntimeError> errors = new ArrayList<>();

    private final Interpreter interpreter = new Interpreter(this::log,this::logError);

    @Test
    public void shouldInterpretLambdaFunction(){

        interpreter.interpret(List.of(
                new Function(
                        new Token(TokenType.IDENTIFIER,"fn",null,1),
                        List.of(new Token(TokenType.IDENTIFIER,"inner",null,1)),
                        List.of(
                               new Stmt.Expression(new Expr.Call(
               new Expr.Variable(new Token(TokenType.IDENTIFIER,"inner",null,1)),
                new Token(TokenType.LEFT_PAREN,"inner",null,1),
                List.of(new Expr.Literal(2.0))
                    )))),
               new Stmt.Expression(
                       new Call(
                               new Expr.Variable(new Token(TokenType.IDENTIFIER,"fn",null,1)),
                               new Token(TokenType.LEFT_PAREN,null,null,1),
                               List.of(
                                       new Expr.Lambda(
                                               new Function(
                                                       null,
                                                       List.of(new Token(TokenType.IDENTIFIER,"a",null, 1)),
                                                       List.of(
                                                               new Print(
                                                                       new Expr.Variable(new Token(TokenType.IDENTIFIER,"a",null,1))
                                                               )
                                                       )
                                               )
                                       )
                               )
                       )
               )
        ));

        assertEquals("2",logs.get(0));
        assertTrue(errors.isEmpty());
    }

    private void log(String log){
       logs.add(log);
    }

    private void logError(RuntimeError log){
        errors.add(log);
    }
}
