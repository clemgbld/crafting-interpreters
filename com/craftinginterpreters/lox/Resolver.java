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
    
    private final Stack<Map<String,Local>> scopes = new Stack<>();

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
        if(!scopes.isEmpty() && scopes.peek().containsKey(expr.name.lexeme) &&  scopes.peek().get(expr.name.lexeme).isDefined() == Boolean.FALSE){
            logError.accept(expr.name,"Can't read local variable in its own initializer.");
        }
        Local local =  scopes.peek().get(expr.name.lexeme);
        local.use();
        resolveLocal(expr,expr.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        resolve(expr.value);
        resolveLocal(expr,expr.name);
        return null;
    }

    private void resolveLocal(Expr expr, Token name) {
        for(int i = scopes.size() - 1; i >= 0; i--){
            if(scopes.get(i).containsKey(name.lexeme)){
                interpreter.resolve(expr,scopes.size() - 1 - i);
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
       Map<String,Local> scope = scopes.peek();
       if(scope.containsKey(name.lexeme)){
           logError.accept(name,"Already a variable with this name in this scope.");
       }
       scope.put(name.lexeme, new Local(name));
    }

    private void define(Token name) {
        if(scopes.isEmpty()) return;
        Map<String,Local> scope = scopes.peek();
        Local local = scope.get(name.lexeme);
        local.define();
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
       Map<String,Local> scope =  scopes.pop();
       scope.forEach((key, local) -> {
          if(!local.isUsed()){
              logError.accept(local.name,"Variable " + key + " is never read.");
          }
       });
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
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


}
