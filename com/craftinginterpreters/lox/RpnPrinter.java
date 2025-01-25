package com.craftinginterpreters.lox;

import java.util.Arrays;

public class RpnPrinter implements Expr.Visitor<String> {
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return rpn(expr.operator.lexeme,expr.right,expr.left);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return null;
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value == null ? "nil" : expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return rpn(expr.operator.lexeme,expr.right);
    }

    private String rpn(String operator, Expr... exprs){
       return Arrays.stream(exprs).map(exp -> exp.accept(this))
                .reduce("",(acc,curr) -> curr + " " + acc) + operator;
    }
}
