package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Stmt.Function;
import com.craftinginterpreters.lox.Stmt.Print;
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

    @Test
    public void shouldThrowARuntimeErrorWhenTryingToInstanceAnObjectThatIsNotAClassWhenUsingInheritance(){
        List<Stmt> ast =  List.of(
                new Stmt.Class(
                        new Token(TokenType.IDENTIFIER,"A",null,1),
                        List.of(),
                        List.of()
                ),
               new Stmt.Var(
                       new Token(TokenType.IDENTIFIER,"B",null,1),
                       new Expr.Literal(1.0)
               ),
                new Stmt.Class(
                        new Token(TokenType.IDENTIFIER,"C",null,1),
                        List.of(
                                new Variable(new Token(TokenType.IDENTIFIER ,"A",null,1)),
                                new Variable(new Token(TokenType.IDENTIFIER ,"B",null,2))
                        ),
                        List.of()
                )
        );
        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        interpreter.interpret(ast);

        assertEquals("Superclass must be a class.",runtimeErrors.get(0).getMessage());
    }

    @Test
    public void shouldEnableASubclassToUseMethodsOfMultipleSuperclasses(){
        List<Stmt> ast =  List.of(
                new Stmt.Class(
                        new Token(TokenType.IDENTIFIER,"A",null,1),
                        List.of(),
                        List.of(
                                new Function(new Token(TokenType.IDENTIFIER,"a",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Print(new Expr.Literal("1"))
                                                        )
                                                )
                                        ))
                        )
                ),
                new Stmt.Class(
                        new Token(TokenType.IDENTIFIER,"B",null,1),
                        List.of(),
                        List.of(
                                new Function(new Token(TokenType.IDENTIFIER,"b",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Print(new Expr.Literal("2"))
                                                        )
                                                )
                                        ))
                        )
                ),
                new Stmt.Class(
                        new Token(TokenType.IDENTIFIER,"C",null,1),
                        List.of(
                                new Variable(new Token(TokenType.IDENTIFIER ,"A",null,1)),
                                new Variable(new Token(TokenType.IDENTIFIER ,"B",null,2))
                        ),
                        List.of()
                ),
                new Stmt.Var(
                        new Token(TokenType.IDENTIFIER,"c",null,1),
                        new Call(new Expr.Variable(new Token(TokenType.IDENTIFIER,"C",null,1)),
                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                List.of()
                        )
                ),
                new Stmt.Expression(
                        new Call(new Expr.Get(
                                new Expr.Variable(new Token(TokenType.IDENTIFIER,"c",null,1)),
                                new Token(TokenType.IDENTIFIER,"a",null,1)
                                ),
                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                List.of()
                        )
                ),
                new Stmt.Expression(
                        new Call(new Expr.Get(
                                new Expr.Variable(new Token(TokenType.IDENTIFIER,"c",null,1)),
                                new Token(TokenType.IDENTIFIER,"b",null,1)
                        ),
                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                List.of()
                        )
                )
        );

        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        interpreter.interpret(ast);

        assertTrue(errors.isEmpty());
        assertTrue(runtimeErrors.isEmpty());
        assertEquals(List.of("1","2"),logs);
    }


    @Test
    public void shouldBeAbleToUseSuperForMultipleSuperclasses(){
        List<Stmt> ast =  List.of(
                new Stmt.Class(
                        new Token(TokenType.IDENTIFIER,"A",null,1),
                        List.of(),
                        List.of(
                                new Function(new Token(TokenType.IDENTIFIER,"a",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Print(new Expr.Literal("1"))
                                                        )
                                                )
                                        ))
                        )
                ),
                new Stmt.Class(
                        new Token(TokenType.IDENTIFIER,"B",null,1),
                        List.of(),
                        List.of(
                                new Function(new Token(TokenType.IDENTIFIER,"b",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Print(new Expr.Literal("2"))
                                                        )
                                                )
                                        ))
                        )
                ),
                new Stmt.Class(
                        new Token(TokenType.IDENTIFIER,"C",null,1),
                        List.of(
                                new Variable(new Token(TokenType.IDENTIFIER ,"A",null,1)),
                                new Variable(new Token(TokenType.IDENTIFIER ,"B",null,2))
                        ),
                        List.of(
                                new Function(new Token(TokenType.IDENTIFIER,"call",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Stmt.Expression(
                                                                        new Call(
                                                                                new Expr.Super(
                                                                                        new Token(TokenType.SUPER,"super",null,1),
                                                                                        new Token(TokenType.IDENTIFIER,"a",null,1)
                                                                                ),
                                                                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                                                               List.of()
                                                                        )
                                                                ),
                                                                new Stmt.Expression(
                                                                        new Call(new Expr.Super(
                                                                                        new Token(TokenType.SUPER,"super",null,1),
                                                                                        new Token(TokenType.IDENTIFIER,"b",null,1)
                                                                                ),
                                                                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                                                                List.of()
                                                                        )
                                                                )
                                                        )
                                                )
                                        ))
                        )
                ),
                new Stmt.Var(
                        new Token(TokenType.IDENTIFIER,"c",null,1),
                        new Call(new Expr.Variable(new Token(TokenType.IDENTIFIER,"C",null,1)),
                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                List.of()
                        )
                ),
                new Stmt.Expression(
                        new Call(new Expr.Get(
                                new Expr.Variable(new Token(TokenType.IDENTIFIER,"c",null,1)),
                                new Token(TokenType.IDENTIFIER,"call",null,1)
                        ),
                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                List.of()
                        )
                )
        );

        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        interpreter.interpret(ast);

        assertTrue(errors.isEmpty());
        assertTrue(runtimeErrors.isEmpty());
        assertEquals(List.of("1","2"),logs);
    }

    @Test
    public void shouldBeAbleToUseSuperOnMultipleInheritanceLevel(){
        List<Stmt> ast =  List.of(
                new Stmt.Class(
                        new Token(TokenType.IDENTIFIER,"A",null,1),
                        List.of(),
                        List.of(
                                new Function(new Token(TokenType.IDENTIFIER,"a",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Print(new Expr.Literal("1"))
                                                        )
                                                )
                                        ))
                        )
                ),
                new Stmt.Class(
                        new Token(TokenType.IDENTIFIER,"B",null,1),
                        List.of(
                                new Variable(new Token(TokenType.IDENTIFIER ,"A",null,1))
                        ),
                        List.of(
                        )
                ),
                new Stmt.Class(
                        new Token(TokenType.IDENTIFIER,"C",null,1),
                        List.of(
                                new Variable(new Token(TokenType.IDENTIFIER ,"B",null,2))
                        ),
                        List.of(
                                new Function(new Token(TokenType.IDENTIFIER,"call",null,1),
                                        List.of(),
                                        List.of(
                                                new Stmt.Block(
                                                        List.of(
                                                                new Stmt.Expression(
                                                                        new Call(
                                                                                new Expr.Super(
                                                                                        new Token(TokenType.SUPER,"super",null,1),
                                                                                        new Token(TokenType.IDENTIFIER,"a",null,1)
                                                                                ),
                                                                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                                                                List.of()
                                                                        )
                                                                )
                                                        )
                                                )
                                        ))
                        )
                ),
                new Stmt.Var(
                        new Token(TokenType.IDENTIFIER,"c",null,1),
                        new Call(new Expr.Variable(new Token(TokenType.IDENTIFIER,"C",null,1)),
                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                List.of()
                        )
                ),
                new Stmt.Expression(
                        new Call(new Expr.Get(
                                new Expr.Variable(new Token(TokenType.IDENTIFIER,"c",null,1)),
                                new Token(TokenType.IDENTIFIER,"call",null,1)
                        ),
                                new Token(TokenType.LEFT_PAREN,null,null,1),
                                List.of()
                        )
                )
        );

        Interpreter interpreter = new Interpreter(this::log,this::logRuntimeError);
        Resolver resolver = new Resolver(interpreter,this::logError);
        resolver.resolve(ast);
        interpreter.interpret(ast);

        assertTrue(errors.isEmpty());
        System.out.println(runtimeErrors);
        assertEquals(List.of("1"),logs);
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
