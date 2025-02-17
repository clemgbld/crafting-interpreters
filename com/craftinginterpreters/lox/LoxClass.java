package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

public class LoxClass extends LoxInstance implements LoxCallable {
    final Map<String, LoxFunction> methods;
    final String name;

        public LoxClass(String name, Map<String, LoxFunction> methods, LoxClass klass) {
            super(klass);
        this.name = name;
        this.methods = methods;
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
        return this.methods.get(name);
    }
}
