package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.*;

import java.util.Arrays;

public class AstPrinter implements Expr.Visitor<String> {
    public String print(Expr ast) {
        return ast.accept(this);
    }
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,expr.left,expr.right);
    }

    @Override
    public String visitCallExpr(Call expr) {
        return null;
    }

    @Override
    public String visitGetExpr(Get expr) {
        return null;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group",expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if(expr.value == null) return "nil";
        return expr.value.toString() ;
    }

    @Override
    public String visitLogicalExpr(Logical expr) {
        return null;
    }

    @Override
    public String visitSetExpr(Set expr) {
        return null;
    }

    @Override
    public String visitSuperExpr(Super expr) {
        return null;
    }

    @Override
    public String visitInnerExpr(Inner expr) {
        return null;
    }

    @Override
    public String visitThisExpr(This expr) {
        return null;
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme,expr.right);
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        return null;
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        return null;
    }

    private String parenthesize(String name,Expr... exprs){
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        Arrays.stream(exprs).forEach(expr -> {
           builder.append(" ");
           builder.append(expr.accept(this));
        });

        builder.append(")");
        return builder.toString();
    }


}
