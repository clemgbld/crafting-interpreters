package com.craftinginterpreters.lox;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RpnPrinterTest {

    final RpnPrinter rpnPrinter = new RpnPrinter();

    @Test
    public void shouldPrintNilWhenThereIsNoLiteral(){
        assertEquals(rpnPrinter.visitLiteralExpr(new Expr.Literal(null)), "nil");
    }

    @Test
    public void shouldPrintTheLiteralInHisStringForm(){
        assertEquals(rpnPrinter.visitLiteralExpr(new Expr.Literal(1.5)), "1.5");
    }

    @Test
    public void shouldBeAbleToPrintAUnaryExpression(){
        assertEquals(rpnPrinter.visitUnaryExpr(new Expr.Unary(new Token(TokenType.MINUS, "-", null , 1) ,new Expr.Literal(2))), "2 -");
    }

    @Test
    public void shouldBeAbleToPrintABinaryExpression(){
        assertEquals(rpnPrinter.visitBinaryExpr(new Expr.Binary(
                new Expr.Binary(
                        new Expr.Literal(1),
                        new Token(TokenType.PLUS, "+", null , 1),
                        new Expr.Literal(2)
                ),
                new Token(TokenType.STAR, "*", null , 1),
                new Expr.Binary(
                        new Expr.Literal(4),
                        new Token(TokenType.MINUS, "-", null , 1),
                        new Expr.Literal(3)
                ))), "1 2 + 4 3 - *");
    }


}
