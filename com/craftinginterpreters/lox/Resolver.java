package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.*;
import com.craftinginterpreters.lox.Stmt.*;
import com.craftinginterpreters.lox.Stmt.Class;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiConsumer;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    private final Interpreter interpreter;

    private final BiConsumer<Token,String> logError;
    
    private final Stack<Map<String,Boolean>> scopes = new Stack<>();

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
    public Void visitGetExpr(Get expr) {
        resolve(expr.object);
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
    public Void visitSetExpr(Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitInnerExpr(Inner expr) {
        if(currentClass == ClassType.NONE){
            logError.accept(expr.keyword,"Can't use 'inner' outside of a class.");
        }
        resolveLocal(expr,expr.keyword);
        return null;
    }

    @Override
    public Void visitThisExpr(This expr) {
        if(currentClass == ClassType.NONE){
            logError.accept(expr.keyword,"Can't use 'this' outside a class.");
        }
        resolveLocal(expr,expr.keyword);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Variable expr) {
        if(!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE){
            logError.accept(expr.name,"Can't read local variable in its own initializer.");
        }
        resolveLocal(expr,expr.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        resolve(expr.value);
        resolveLocal(expr,expr.name);
        return null;
    }

    @Override
    public Void visitLoxListExpr(Expr.LoxList expr) {
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
            if(currentFunction == FunctionType.INITIALIZER){
                logError.accept(stmt.keyword,"Can't return a value form an initializer");
            }
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
       Map<String,Boolean> scope = scopes.peek();
       if(scope.containsKey(name.lexeme)){
           logError.accept(name,"Already a variable with this name in this scope.");
       }
       scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if(scopes.isEmpty()) return;
        Map<String,Boolean> scope = scopes.peek();
        scope.put(name.lexeme, true);
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

    @Override
    public Void visitClassStmt(Class stmt) {
        declare(stmt.name);
        define(stmt.name);
        if(stmt.superClass != null && stmt.name.lexeme.equals(stmt.superClass.name.lexeme)){
           logError.accept(stmt.superClass.name,"A class can't inherit from itself.");
        }
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
            beginScope();
            scopes.peek().put("inner",true);
        beginScope();
        scopes.peek().put("this",true);
        stmt.methods.forEach(method ->
                {
                    var delcaration = FunctionType.METHOD;
                    if(method.name.lexeme.equals("init")){
                       delcaration = FunctionType.INITIALIZER;
                    }
                    resolveFunction(method,delcaration);
                }
        );
        endScope();
        endScope();
        currentClass = enclosingClass;
        return null;
    }

    private void endScope() {
        scopes.pop();
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private enum ClassType {
        NONE, CLASS
    }

    private ClassType currentClass = ClassType.NONE;


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
