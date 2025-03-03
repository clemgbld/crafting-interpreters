package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Stmt.Function;

import java.util.List;

public class LoxFunction implements LoxCallable{
    private final Stmt.Function declaration;
    private final Environment closure;

    private final boolean isInitializer;

    public LoxFunction(Function declaration, Environment closure,boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
                final Environment environment = new Environment(closure);
                for (var i = 0; i < declaration.params.size();i++){
                    environment.define(declaration.params.get(i).lexeme,arguments.get(i));
                }
                try {
                    interpreter.executeBlock(declaration.body,environment);
                }catch (ReturnException ex){
                    if(isInitializer) return closure.getAt(0,"this");
                    return ex.value;
                }
                if(isInitializer) return closure.getAt(0,"this");
                return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();

    }

    @Override
    public String toString(){
        return "<fn " + declaration.name.lexeme + ">";
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this",instance);
        return new LoxFunction(declaration,environment,isInitializer);
    }
}
