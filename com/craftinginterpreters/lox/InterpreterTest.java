package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Stmt.Block;
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

    @Test
    public void shouldNotBeAbleToCallAStaticMethod(){
        var ast  = List.of(
                new Stmt.Class(new Token(TokenType.IDENTIFIER,"Math",null,1),
                        List.of(new Function(
                                new Token(TokenType.IDENTIFIER,"square",null,1),
                                List.of(new Token(TokenType.IDENTIFIER,"n",null,1)),
                                List.of(
                                        new Block(
                                                List.of(
                                                        new Stmt.Return(
                                                                new Token(TokenType.RETURN,null,null,1),
                                                                new Expr.Binary(
                                                                        new Expr.Variable(new Token(TokenType.IDENTIFIER,"n",null,1)),
                                                                        new Token(TokenType.STAR,"*",null,1),
                                                                        new Expr.Variable(new Token(TokenType.IDENTIFIER,"n",null,1))
                                                                )
                                                        )
                                                )
                                        )
                                ),
                                true
                        ))),
                new Print(
                        new Expr.Call(
                                new Expr.Get(new Expr.Variable(new Token(TokenType.IDENTIFIER,"Math",null,1)),
                                        new Token(TokenType.IDENTIFIER,"square",null,1)
                                ),
                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                List.of(new Expr.Literal(3.0))
                        )
                )

        );
       Interpreter interpreter = new Interpreter(this::log,this::logError);
       Resolver resolver = new Resolver(interpreter);
       resolver.resolve(ast);
       interpreter.interpret(ast);

       assertTrue(errors.isEmpty());
       assertEquals("9",logs.get(0));
    }


    private void log(String message){
        logs.add(message);
    }

    private void logError(RuntimeError runtimeError){
        errors.add(runtimeError);
    }
}
