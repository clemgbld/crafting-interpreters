package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Stmt.Break;
import com.craftinginterpreters.lox.Stmt.Print;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InterpreterTest {

    private final List<String> logs = new ArrayList<>();
    private final List<RuntimeError> errorLogs = new ArrayList<>();

    @Test
    public void shouldBeAbleToBreakOutOfTheLoop(){
        Interpreter interpreter = new Interpreter(this::log,this::logError);
        interpreter.interpret(List.of(
            new Stmt.While(
                    new Expr.Literal(true),
                    new Stmt.Block(
                           List.of(
                                   new Print(new Expr.Literal(2)),
                                   new Break()
                           )
                    )
            )
        ));
        assertEquals("2",logs.get(0));
        assertTrue(errorLogs.isEmpty());
    }

    private void log(String log){
        logs.add(log);
    }

    private void logError(RuntimeError log){
        errorLogs.add(log);
    }
}
