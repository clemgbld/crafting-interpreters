package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Stmt.*;
import com.craftinginterpreters.lox.Stmt.Class;
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

    @Test
    public void shouldDoNothingWhenThereIsNoSubClassesWithTheDesiredInnerMethod(){
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
                                                                ),
                                                                new Print(new Expr.Literal("log"))
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                new Var(
                        new Token(TokenType.IDENTIFIER,"a",null,1),
                        new Call(
                                new Variable(
                                        new Token(TokenType.IDENTIFIER,"A",null,1)
                                ),

                                new Token(TokenType.IDENTIFIER,null,null,1),
                                List.of()
                        )
                ),
                new Stmt.Expression(
                        new Call(
                                new Expr.Get(
                                        new Variable(new Token(TokenType.IDENTIFIER,"a",null,1)),
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
        assertEquals(List.of("log"),logs);
    }

    @Test
    public void shouldDoNothingWhenCallingInnerOnAClassThatHasNoSubClasses(){
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
                                                                ),
                                                                new Print(new Expr.Literal("log"))
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
                                        new Token(TokenType.IDENTIFIER,"v",null,1),
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
                                        new Token(TokenType.IDENTIFIER,"bark",null,1),
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
        assertEquals(List.of("log"),logs);
    }

    @Test
    public void shouldNotUseInnerOutsideAClass(){
        List<Stmt> ast = List.of(
               new Function(
                       new Token(TokenType.IDENTIFIER,"x",null,1),
                       List.of(),
                       List.of(
                               new Block(List.of(
                                       new Stmt.Expression(
                                               new Call(
                                                       new Expr.Inner(new Token(TokenType.INNER, "inner",null,1)),
                                                       new Token(TokenType.LEFT_PAREN,null,null,1),
                                                       List.of()
                                               )
                                       )
                               ))
                       )
               ),
                new Stmt.Expression(
                        new Call(
                                new Variable(
                                        new Token(TokenType.IDENTIFIER,"x",null,1)
                                ),

                                new Token(TokenType.IDENTIFIER,null,null,1),
                                List.of()
                        )
                )
        );

        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        assertEquals(TokenType.INNER,errors.get(0).name.type);
        assertEquals("Can't use 'inner' outside of a class.",errors.get(0).message);
    }

    @Test
    public void shouldBeAbleToPrintAnEmptyList(){
       List<Stmt> ast = List.of(
               new Print(
                       new Expr.LoxList(
                               List.of()
                       )
               )
       );

        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        interpreter.interpret(ast);
        assertEquals(List.of("[]"),logs);
    }

    @Test
    public void shouldBeAbleToPrintAList(){
        List<Stmt> ast = List.of(
                new Var(
                        new Token(TokenType.IDENTIFIER,"x",null,1),
                        new Expr.Literal("1")
                ),
                new Print(
                        new Expr.LoxList(
                                List.of(
                                        new Expr.Variable(new Token(TokenType.IDENTIFIER,"x",null,1)),
                                        new Expr.Literal("2"),
                                        new Expr.Literal("3")
                                )
                        )
                )
        );

        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        interpreter.interpret(ast);
        assertEquals(List.of("[1, 2, 3]"),logs);
    }

    @Test
    public void shouldBeAbleToCorrectlyPrintNilAndNumbers(){
        List<Stmt> ast = List.of(
                new Print(
                        new Expr.LoxList(
                                List.of(
                                        new Expr.Literal(1.0),
                                        new Expr.Literal(2.0),
                                        new Expr.Literal(3.3),
                                        new Expr.Literal(null)
                                )
                        )
                )
        );

        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        interpreter.interpret(ast);
        assertEquals(List.of("[1, 2, 3.3, nil]"),logs);
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
