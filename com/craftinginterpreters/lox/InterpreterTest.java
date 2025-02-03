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
    private final List<RuntimeError> errorsLog = new ArrayList<>();

    private final Interpreter interpreter = new Interpreter(this::log, this::logError);

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
        interpreter.interpret(List.of(
                new Stmt.Var(new Token(TokenType.IDENTIFIER,"a",null,1),new Expr.NotInitialized(new Token(TokenType.IDENTIFIER,"a",null,1))),
                new Print(new Variable(new Token(TokenType.IDENTIFIER,"a",null,1)))
        ));
        assertEquals("Variable a not initialized.",errorsLog.get(0).getMessage());
        assertEquals(TokenType.IDENTIFIER,errorsLog.get(0).token.type);
    }

    @Test
    public void shouldNotThrowWhenDefiningANotInitializedVar(){
        interpreter.interpret(List.of(
                new Stmt.Var(new Token(TokenType.IDENTIFIER,"a",null,1),new Expr.NotInitialized(new Token(TokenType.IDENTIFIER,"a",null,1)))
        ));
        assertTrue(errorsLog.isEmpty());
    }

    private void log(String log){
        logs.add(log);
    }

    private void logError(RuntimeError runtimeError){
        errorsLog.add(runtimeError);
    }
}
