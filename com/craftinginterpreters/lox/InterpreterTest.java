package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Stmt.Class;
import com.craftinginterpreters.lox.Stmt.Print;
import com.craftinginterpreters.lox.Stmt.Var;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InterpreterTest {
    private final List<RuntimeError> runtimeErrors = new ArrayList<>();
    private final List<Error> errors = new ArrayList<>();
    private final List<String> logs = new ArrayList<>();
    @Test
    public void shouldTryToReachTheSuperMethodBeforeTheSubMethod(){
        List<Stmt> ast = List.of(
                new Class(
                        new Token(TokenType.IDENTIFIER,"A",null,1),
                        null,
                        List.of(
                               new Stmt.Function(
                                      new Token(TokenType.IDENTIFIER,"cook",null,1),
                                       List.of(),
                                       List.of(
                                               new Stmt.Block(
                                                       List.of(
                                                               new Print(new Expr.Literal("cook super"))
                                                       )
                                               )
                                       )
                               )
                        )
                        ),
                new Class(
                        new Token(TokenType.IDENTIFIER,"B",null,1),
                        new Variable(new Token(TokenType.IDENTIFIER,"A",null,1)),
                        List.of(
                                new Stmt.Function(
                                        new Token(TokenType.IDENTIFIER,"cook",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Print(new Expr.Literal("cook sub"))
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                new Var(
                        new Token(TokenType.IDENTIFIER,"b",null,1),
                        new Call(
                                new Variable(
                                        new Token(TokenType.IDENTIFIER,"B",null,1)
                                ),

                                new Token(TokenType.IDENTIFIER,null,null,1),
                                List.of()
                        )
                ),
                new Stmt.Expression(
                        new Call(
                                new Expr.Get(
                                       new Variable(new Token(TokenType.IDENTIFIER,"b",null,1)),
                                       new Token(TokenType.IDENTIFIER,"cook",null,1)
                                ),

                                new Token(TokenType.IDENTIFIER,null,null,1),
                                List.of()
                        )
                )
        );

        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        interpreter.interpret(ast);
        assertEquals(List.of("cook super"),logs);
    }

    @Test
    public void shouldCallTheMethodWithTheSameNameInTheSubclassWhenUsingInner(){
        List<Stmt> ast = List.of(
                new Class(
                        new Token(TokenType.IDENTIFIER,"A",null,1),
                        null,
                        List.of(
                                new Stmt.Function(
                                        new Token(TokenType.IDENTIFIER,"cook",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                               new Stmt.Expression(
                                                                       new Call(
                                                                               new Expr.Inner(new Token(TokenType.INNER, "inner",null,1)),
                                                                               new Token(TokenType.LEFT_PAREN,null,null,1),
                                                                               List.of()
                                                                       )
                                                               )
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                new Class(
                        new Token(TokenType.IDENTIFIER,"B",null,1),
                        new Variable(new Token(TokenType.IDENTIFIER,"A",null,1)),
                        List.of(
                                new Stmt.Function(
                                        new Token(TokenType.IDENTIFIER,"cook",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Print(new Expr.Literal("cook sub"))
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                new Var(
                        new Token(TokenType.IDENTIFIER,"b",null,1),
                        new Call(
                                new Variable(
                                        new Token(TokenType.IDENTIFIER,"B",null,1)
                                ),

                                new Token(TokenType.IDENTIFIER,null,null,1),
                                List.of()
                        )
                ),
                new Stmt.Expression(
                        new Call(
                                new Expr.Get(
                                        new Variable(new Token(TokenType.IDENTIFIER,"b",null,1)),
                                        new Token(TokenType.IDENTIFIER,"cook",null,1)
                                ),

                                new Token(TokenType.IDENTIFIER,null,null,1),
                                List.of()
                        )
                )
        );

        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        interpreter.interpret(ast);
        assertTrue(runtimeErrors.isEmpty());
        assertTrue(errors.isEmpty());
        assertEquals(List.of("cook sub"),logs);
    }

    @Test
    public void shouldSelectTheCorrectSubClassWhenUsingInnerOnAMultipleLevelInheritance(){
        List<Stmt> ast = List.of(
                new Class(
                        new Token(TokenType.IDENTIFIER,"A",null,1),
                        null,
                        List.of(
                                new Stmt.Function(
                                        new Token(TokenType.IDENTIFIER,"cook",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Stmt.Expression(
                                                                        new Call(
                                                                                new Expr.Inner(new Token(TokenType.INNER, "inner",null,1)),
                                                                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                                                                List.of()
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                new Class(
                        new Token(TokenType.IDENTIFIER,"B",null,1),
                        new Variable(new Token(TokenType.IDENTIFIER,"A",null,1)),
                        List.of(
                                new Stmt.Function(
                                        new Token(TokenType.IDENTIFIER,"cook",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Print(new Expr.Literal("cook sub"))
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                new Class(
                        new Token(TokenType.IDENTIFIER,"C",null,1),
                        new Variable(new Token(TokenType.IDENTIFIER,"B",null,1)),
                        List.of(
                                new Stmt.Function(
                                        new Token(TokenType.IDENTIFIER,"cook",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Print(new Expr.Literal("cook sub sub"))
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                new Var(
                        new Token(TokenType.IDENTIFIER,"c",null,1),
                        new Call(
                                new Variable(
                                        new Token(TokenType.IDENTIFIER,"C",null,1)
                                ),

                                new Token(TokenType.IDENTIFIER,null,null,1),
                                List.of()
                        )
                ),
                new Stmt.Expression(
                        new Call(
                                new Expr.Get(
                                        new Variable(new Token(TokenType.IDENTIFIER,"c",null,1)),
                                        new Token(TokenType.IDENTIFIER,"cook",null,1)
                                ),

                                new Token(TokenType.IDENTIFIER,null,null,1),
                                List.of()
                        )
                )
        );

        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        interpreter.interpret(ast);
        assertTrue(runtimeErrors.isEmpty());
        assertTrue(errors.isEmpty());
        assertEquals(List.of("cook sub"),logs);
    }

    @Test
    public void shouldGoToTheNextSubClassIfTheMethodIsNotFoundInTheNearestSubClass(){
        List<Stmt> ast = List.of(
                new Class(
                        new Token(TokenType.IDENTIFIER,"A",null,1),
                        null,
                        List.of(
                                new Stmt.Function(
                                        new Token(TokenType.IDENTIFIER,"cook",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Stmt.Expression(
                                                                        new Call(
                                                                                new Expr.Inner(new Token(TokenType.INNER, "inner",null,1)),
                                                                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                                                                List.of()
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                new Class(
                        new Token(TokenType.IDENTIFIER,"B",null,1),
                        new Variable(new Token(TokenType.IDENTIFIER,"A",null,1)),
                        List.of(
                                new Stmt.Function(
                                        new Token(TokenType.IDENTIFIER,"bark",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Print(new Expr.Literal("bark sub"))
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                new Class(
                        new Token(TokenType.IDENTIFIER,"C",null,1),
                        new Variable(new Token(TokenType.IDENTIFIER,"B",null,1)),
                        List.of(
                                new Stmt.Function(
                                        new Token(TokenType.IDENTIFIER,"cook",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Print(new Expr.Literal("cook sub"))
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                new Var(
                        new Token(TokenType.IDENTIFIER,"c",null,1),
                        new Call(
                                new Variable(
                                        new Token(TokenType.IDENTIFIER,"C",null,1)
                                ),

                                new Token(TokenType.IDENTIFIER,null,null,1),
                                List.of()
                        )
                ),
                new Stmt.Expression(
                        new Call(
                                new Expr.Get(
                                        new Variable(new Token(TokenType.IDENTIFIER,"c",null,1)),
                                        new Token(TokenType.IDENTIFIER,"cook",null,1)
                                ),

                                new Token(TokenType.IDENTIFIER,null,null,1),
                                List.of()
                        )
                )
        );

        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        interpreter.interpret(ast);
        assertTrue(runtimeErrors.isEmpty());
        assertTrue(errors.isEmpty());
        assertEquals(List.of("cook sub"),logs);
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
