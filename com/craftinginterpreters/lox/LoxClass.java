package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    private final Map<String, LoxFunction> methods;
    final String name;

    private final List<LoxClass> superClasses;


    public LoxClass(String name, List<LoxClass> superClasses ,Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
        this.superClasses = superClasses;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null){
            initializer.bind(instance).call(interpreter,arguments);
        }
        return instance;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if(initializer == null) return 0;
        return initializer.arity();
    }

    public LoxFunction findMethod(String name) {
        if(methods.containsKey(name)){
            return methods.get(name);
        }
       return superClasses.stream()
                .filter(c -> c.methods.containsKey(name))
                .findFirst()
                .map(c -> c.findMethod(name))
                .orElse(null);

    }
}
