package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.*;
import com.craftinginterpreters.lox.Stmt.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{

    private final Interpreter interpreter;
    
    private final Stack<Map<String,Boolean>> scopes = new Stack<>();

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        return null;
    }

    @Override
    public Void visitCallExpr(Call expr) {
        return null;
    }

    @Override
    public Void visitGroupingExpr(Grouping expr) {
        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Logical expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        return null;
    }

    @Override
    public Void visitVariableExpr(Variable expr) {
        return null;
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        return null;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        decalre(stmt.name);
        if(stmt.initializer != null){
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
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
        scopes.pop();
    }

    private void beginScope() {
        scopes.push(new HashMap<String,Boolean>());
    }


    private void resolve(List<Stmt> statements) {
        statements.forEach(this::resolve);
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }


}
