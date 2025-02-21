package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Variable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InterpreterTest {
    private final List<RuntimeError> runtimeErrors = new ArrayList<>();
    private final List<Error> errors = new ArrayList<>();
    private final List<String> logs = new ArrayList<>();

    @Test
    public void shouldNotAllowAClassToExtendsItselfWhenUsingMultipleInheritance(){
        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(
                List.of(
                        new Stmt.Class(
                                new Token(TokenType.IDENTIFIER,"A",null,1),
                                List.of(),
                                List.of()
                        ),
                        new Stmt.Class(
                                new Token(TokenType.IDENTIFIER,"B",null,1),
                                List.of(),
                                List.of()
                        ),
                        new Stmt.Class(
                                new Token(TokenType.IDENTIFIER,"C",null,1),
                                List.of(
                                        new Variable(new Token(TokenType.IDENTIFIER ,"A",null,1)),
                                        new Variable(new Token(TokenType.IDENTIFIER ,"C",null,2))
                                ),
                                List.of()
                        )
                )
        );

        assertEquals(
               "A class can't inherit from itself.",
                errors.get(0).message
        );

        assertEquals(
                "C",
                errors.get(0).name.lexeme
        );

        assertEquals(
                2,
                errors.get(0).name.line
        );

    }

    private void log(String message){
       logs.add(message);
    }

    private void logRuntimeError(RuntimeError runtimeError){
       runtimeErrors.add(runtimeError);
    }

    private void logError(Token name, String message){
       errors.add(new Error(name,message));
    }

    private record Error(Token name,String message){}
}
