package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    private final Map<String, LoxFunction> methods;
    final String name;

    public final LoxClass superClass;

    public LoxClass subClass;

    public LoxClass(String name, LoxClass superClass ,Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
        this.superClass = superClass;
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
        if(superClass != null){
            return superClass.findMethod(name);
        }

        if(methods.containsKey(name)){
            return methods.get(name);
        }

        return null;
    }

    public LoxFunction findInnerMethod(String name){
        if(this.subClass == null) return null;
        if(subClass.methods.containsKey(name)){
            return subClass.methods.get(name);
        }
        return subClass.findInnerMethod(name);
    }


    public LoxClass findSuperClassByName(String className) {
        if (this.superClass == null) return null;
        if(this.superClass.name.equals(className) ) return this.superClass;
        return this.superClass.findSuperClassByName(className);
    }

    public void setSubClass(LoxClass subClass) {
        this.subClass = subClass;
    }
}
