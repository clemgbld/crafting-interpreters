package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Environment {
    private final Environment enclosing;
    private final Map<String,Supplier<Object>> values = new HashMap<>();

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Supplier<Object> getValue){
        values.put(name,getValue);
    }

    Supplier<Object> get(Token name){
        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }
        if(enclosing != null) return enclosing.get(name);
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "' .");
    }

    public void assign(Token name, Supplier<Object> getValue) {
        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme,getValue);
            return;
        }
        if(enclosing != null)  {
            enclosing.assign(name,getValue);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "' .");
    }
}
