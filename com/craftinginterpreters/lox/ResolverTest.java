package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Stmt.Print;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResolverTest {
    private List<Error> errors = new ArrayList<>();

    @Test
    public void shouldReportAnErrorWhenAVariableIsNeverUsed(){
        Resolver resolver = new Resolver(new Interpreter(),this::logError);
        resolver.resolve(List.of(
                new Stmt.Block(List.of(
                        new Stmt.Var(new Token(TokenType.IDENTIFIER,"a",null, 1), new Expr.Literal(1)),
                        new Stmt.Var(new Token(TokenType.IDENTIFIER,"c",null, 2), new Expr.Literal(1)),
                        new Stmt.Var(new Token(TokenType.IDENTIFIER,"b",null, 1), new Expr.Literal(1)),
                        new Print(new Variable(new Token(TokenType.IDENTIFIER,"b",null, 1)))
                ))
        ));

        assertEquals("Variable a is never read.",errors.get(0).message);
        assertEquals(TokenType.IDENTIFIER, errors.get(0).token.type);
        assertEquals("a", errors.get(0).token.lexeme);
        assertEquals(1, errors.get(0).token.line);
        assertEquals("Variable c is never read.",errors.get(1).message);
        assertEquals(TokenType.IDENTIFIER, errors.get(1).token.type);
        assertEquals("c", errors.get(1).token.lexeme);
        assertEquals(2, errors.get(1).token.line);
    }

    @Test
    public void shouldReportNotReportAnErrorWhenVariableAreUsedInAnotherScoped(){
        Resolver resolver = new Resolver(new Interpreter(),this::logError);
        resolver.resolve(List.of(
                new Stmt.Block(List.of(
                        new Stmt.Var(new Token(TokenType.IDENTIFIER,"a",null, 1), new Expr.Literal(1)),
                        new Stmt.Block(List.of(
                                new Print(new Variable(new Token(TokenType.IDENTIFIER,"a",null, 1)))
                        ))
                ))
        ));
        assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldReportAnErrorWhenAVariableIsReassignedButNeverUsed(){
        Resolver resolver = new Resolver(new Interpreter(),this::logError);
        resolver.resolve(List.of(
                new Stmt.Block(List.of(
                        new Stmt.Var(new Token(TokenType.IDENTIFIER,"a",null, 1), new Expr.Literal(1)),
                        new Stmt.Expression(new Expr.Assign(new Token(TokenType.IDENTIFIER,"a",null, 1),new Expr.Literal(2))),
                        new Stmt.Var(new Token(TokenType.IDENTIFIER,"b",null, 1), new Expr.Literal(1)),
                        new Print(new Variable(new Token(TokenType.IDENTIFIER,"b",null, 1)))
                ))
        ));
        assertEquals("Variable a is never read.",errors.get(0).message);
        assertEquals(TokenType.IDENTIFIER, errors.get(0).token.type);
        assertEquals("a", errors.get(0).token.lexeme);
        assertEquals(1, errors.get(0).token.line);
    }

    @Test
    public void shouldReportAnErrorWhenAVariableIsReassignedWithItselfButNeverUsed(){
        Resolver resolver = new Resolver(new Interpreter(),this::logError);
        resolver.resolve(List.of(
                new Stmt.Block(List.of(
                        new Stmt.Var(new Token(TokenType.IDENTIFIER,"a",null, 1), new Expr.Literal(1)),
                        new Stmt.Expression(new Expr.Assign(new Token(TokenType.IDENTIFIER,"a",null, 1),new Expr.Variable(new Token(TokenType.IDENTIFIER,"a",null, 1)))),
                        new Stmt.Var(new Token(TokenType.IDENTIFIER,"b",null, 1), new Expr.Literal(1)),
                        new Print(new Variable(new Token(TokenType.IDENTIFIER,"b",null, 1)))
                ))
        ));
        assertEquals("Variable a is never read.",errors.get(0).message);
        assertEquals(TokenType.IDENTIFIER, errors.get(0).token.type);
        assertEquals("a", errors.get(0).token.lexeme);
        assertEquals(1, errors.get(0).token.line);
    }


    @Test
    public void shouldNotReportAnError(){
        Resolver resolver = new Resolver(new Interpreter(),this::logError);
        resolver.resolve(List.of(
                new Stmt.Block(List.of(
                        new Stmt.Var(new Token(TokenType.IDENTIFIER,"b",null, 1), new Expr.Literal(1)),
                        new Print(new Variable(new Token(TokenType.IDENTIFIER,"b",null, 1)))
                ))
        ));

        assertTrue(errors.isEmpty());
    }


    private void logError(Token token, String message){
       errors.add(new Error(token,message));
    }

    record Error(Token token,String message){}
}
