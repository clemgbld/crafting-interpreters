package com.craftinginterpreters.lox;

public class Local {

    private boolean isDefined = false;

    private boolean isUsed = false;

    public Token name;

    public Local(Token name) {
        this.name = name;
    }

    public void define(){
        this.isDefined = true;
    }

    public void use(){
        this.isUsed = true;
    }

    public void unUse() { this.isUsed = false;}

    public boolean isDefined() {
        return isDefined;
    }

    public boolean isUsed() {
        return isUsed;
    }
}
