package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Stmt.Print;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class InterpreterTest {
    private final List<String> logs = new ArrayList<>();

    private final Interpreter interpreter = new Interpreter(this::log);

    @Test
    public void shouldBeAbleToExecuteStatements(){

        interpreter.interpret(List.of(
                new Print(new Expr.Literal(3.0)),
                new Print(new Expr.Literal(2.0))
        ));

        assertEquals("3",logs.get(0));
        assertEquals("2",logs.get(1));
    }

    @Test
    public void shouldBeAbleToEvaluateAndPrintTheResultOfAnExpression(){
        interpreter.interpret(new Expr.Binary(
                new Literal(3.0),
                new Token(TokenType.PLUS,"+",null,1),
                new Literal(2.0)
        ));

        assertEquals("5",logs.get(0));
    }

    @Test
    public void shouldBeAbleToPrintAVariableInitializedAtNil(){
        interpreter.interpret(List.of(
                new Stmt.Var(new Token(TokenType.IDENTIFIER,"a",null,1),new Expr.Literal(null)),
                new Print(new Variable(new Token(TokenType.IDENTIFIER,"a",null,1)))
        ));

        assertEquals("nil",logs.get(0));
    }

    @Test
    public void shouldThrowARuntimeErrorWhenTryingToAccessAVariableNotExplicitlyInitialized() throws RuntimeError{
        try{
            interpreter.interpret(List.of(
                    new Stmt.Var(new Token(TokenType.IDENTIFIER,"a",null,1),new Expr.NotInitialized(new Token(TokenType.IDENTIFIER,"a",null,1))),
                    new Print(new Variable(new Token(TokenType.IDENTIFIER,"a",null,1)))
            ));
        }catch (RuntimeError runtimeError){
            assertEquals(TokenType.IDENTIFIER,runtimeError.token.type);
            assertEquals("Variable a not initialized.",runtimeError.getMessage());
        }
    }

    private void log(String log){
        logs.add(log);
    }
}
