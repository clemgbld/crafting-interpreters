package com.craftinginterpreters.lox;

public class ReturnException extends RuntimeException{
     final Object value;
    public ReturnException(Object value) {
        super(null,null,false,false);
        this.value = value;
    }
}
