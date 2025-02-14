package com.craftinginterpreters.lox;

public class Local {

    private boolean isDefined = false;

    private boolean isUsed = false;

    public Token name;

    public int index;

    public Local(Token name,int index) {
        this.name = name;
        this.index = index;
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
