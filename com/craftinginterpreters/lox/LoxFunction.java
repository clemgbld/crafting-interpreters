package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Stmt.Function;

import java.util.ArrayList;
import java.util.List;

public class LoxFunction implements LoxCallable{
    private final Stmt.Function declaration;

    private final List<Object> closure;

    public LoxFunction(Function declaration, List<Object>  closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
                if(closure != null){
                    interpreter.localEnvironment.add(0,closure);
                }

                List<Object> localEnv = new ArrayList<>();
                for (var i = 0; i < declaration.params.size();i++){
                    localEnv.add(arguments.get(i));
                }
                try {
                    interpreter.executeBlock(declaration.body,localEnv);
                }catch (ReturnException ex){
                    return ex.value;
                }finally {
                    if(closure != null){
                        interpreter.localEnvironment.remove(0);
                    }
                }
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
}
