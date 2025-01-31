package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Literal;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InterpreterTest {

    private final List<Object> output = new ArrayList<>();

    private final List<RuntimeError> errors =  new ArrayList<>();

    private void log(Object obj){
        output.add(obj);
    }
    private void logRuntimeError(RuntimeError error){
        errors.add(error);
    }
    private final Interpreter interpreter = new Interpreter(this::log, this::logRuntimeError);

    @Test
    public void shouldConcatenateTwoOperandsWhenTheFirstOneIsAStringAndTheSecondANumber(){
        interpreter.interpret(new Binary(new Literal("scott"),new Token(TokenType.PLUS,"+",null,1),new Literal(2.0)));
        assertEquals("scott2",output.get(0));
    }

    @Test
    public void shouldConcatenateTwoOperandsWhenTheFirstOneIsDoubleAndTheSecondAString(){
        interpreter.interpret(new Binary(new Literal(1.0),new Token(TokenType.PLUS,"+",null,1),new Literal("bla")));
        assertEquals("1bla",output.get(0));
    }

    @Test
    public void shouldThrowAnErrorWhenWeTryToConcatenateOtherTypesThanStringAndNumber(){
        interpreter.interpret(new Binary(new Literal(false),new Token(TokenType.PLUS,"+",null,1),new Literal("bla")));
        assertEquals("Operands must be either number or strings.",errors.get(0).getMessage());
    }

    @Test
    public void shouldBeInfinityWhenDividingByAFloatingPointNumber(){
        interpreter.interpret(new Binary(new Literal(1.5),new Token(TokenType.SLASH,"+",null,1),new Literal(0.0)));
        assertEquals("Infinity",output.get(0));
    }

    @Test
    public void shouldThrowAnErrorWhenWeTryingToDivideAnIntegerByZero(){
        interpreter.interpret(new Binary(new Literal(1.0),new Token(TokenType.SLASH,"+",null,1),new Literal(0.0)));
        assertEquals("Cannot divide integer by zero.",errors.get(0).getMessage());
    }

}
