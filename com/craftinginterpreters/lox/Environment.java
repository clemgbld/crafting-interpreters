package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class Environment {
    public Environment enclosing;
    final Map<String,Object> values = new HashMap<>();

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value){
        values.put(name,value);
    }

    Object get(Token name){
        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }
        if(enclosing != null) return enclosing.get(name);
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "' .");
    }

    public void assign(Token name, Object value) {
        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme,value);
            return;
        }
        if(enclosing != null)  {
            enclosing.assign(name,value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "' .");
    }

    public Object getAt(int depth, String name) {
        return ancestors(depth).values.get(name);
    }

    public Object assignAt(int depth, String name, Object value) {
        return ancestors(depth).values.put(name,value);
    }

    private Environment ancestors(int depth) {
        Environment env = this;
        return IntStream.range(0,depth)
                .boxed()
                .reduce(env,(environment,i) -> environment.enclosing, (a,b) -> a);
    }
}
