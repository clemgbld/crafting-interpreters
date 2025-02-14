package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.*;
import com.craftinginterpreters.lox.Stmt.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiConsumer;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    private final Interpreter interpreter;

    private final BiConsumer<Token,String> logError;
    
    private final Stack<Scope> scopes = new Stack<>();

    private FunctionType currentFunction = FunctionType.NONE;

    public Resolver(Interpreter interpreter, BiConsumer<Token, String> logError) {
        this.interpreter = interpreter;
        this.logError = logError;
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Call expr) {
        resolve(expr.callee);
        expr.arguments.forEach(this::resolve);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Variable expr) {
        if(!scopes.isEmpty() && scopes.peek().containsKey(expr.name.lexeme) &&  scopes.peek().isDefined(expr.name.lexeme) == Boolean.FALSE){
            logError.accept(expr.name,"Can't read local variable in its own initializer.");
        }
        resolveLocal(expr,expr.name,true);
        return null;
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        resolve(expr.value);
        resolveLocal(expr,expr.name,false);
        return null;
    }

    private void resolveLocal(Expr expr, Token name, boolean isread) {
        for(int i = scopes.size() - 1; i >= 0; i--){
            if(scopes.get(i).containsKey(name.lexeme)){
                Scope scope = scopes.get(i);
                if(isread){
                    scope.useByName(name.lexeme);
                }else{
                    scope.unUseByName(name.lexeme);
                }
                interpreter.resolve(expr,scopes.size() - 1 - i, scope.getIndexByName(name.lexeme) );
                return;
            }
        }
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt,FunctionType.FUNCTION);
        return null;
    }

    private void resolveFunction(Function function,FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        function.params.forEach(param -> {
            declare(param);
            define(param);
        });
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if(stmt.elseBranch != null){
           resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        if(currentFunction == FunctionType.NONE){
            logError.accept(stmt.keyword,"Can't return from top-level code.");
        }

        if(stmt.value != null){
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        declare(stmt.name);
        if(stmt.initializer != null){
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    private void declare(Token name) {
       if(scopes.isEmpty()) return;
       Scope scope = scopes.peek();
       if(scope.containsKey(name.lexeme)){
           logError.accept(name,"Already a variable with this name in this scope.");
       }
       scope.add(name);
    }

    private void define(Token name) {
        if(scopes.isEmpty()) return;
        Scope scope = scopes.peek();
        scope.defineByName(name.lexeme);
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    private void endScope() {
       Scope scope =  scopes.pop();
       scope.verifyIfAVariableIsUnUsed(logError);
    }

    private void beginScope() {
        scopes.push(new Scope());
    }


    public void resolve(List<Stmt> statements) {
        statements.forEach(this::resolve);
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private class Scope{
        Map<String,Local> locales = new HashMap<>();
        private int index = 0;

        public void add(Token name){
            locales.put(name.lexeme,new Local(name,index));
            index++;
        }

        public int getIndexByName(String name){
           return locales.get(name).index;
        }

        public void defineByName(String name){
            locales.get(name).define();
        }

        public void useByName(String name){
            locales.get(name).use();
        }

        public void unUseByName(String name){
            locales.get(name).unUse();
        }

        public boolean containsKey(String name){
            return locales.containsKey(name);
        }

        public boolean isDefined(String name){
            return locales.get(name).isDefined();
        }

        public void verifyIfAVariableIsUnUsed(BiConsumer<Token, String> logError) {
            locales.forEach((key, local) -> {
                if(!local.isUsed()){
                    logError.accept(local.name,"Variable " + key + " is never read.");
                }
            });
        }
    };
}
