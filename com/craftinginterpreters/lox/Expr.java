package com.craftinginterpreters.lox;

import java.util.Objects;

abstract class Expr {
 interface Visitor<R> {
 R visitBinaryExpr(Binary expr);
 R visitGroupingExpr(Grouping expr);
 R visitLiteralExpr(Literal expr);
 R visitUnaryExpr(Unary expr);

  }
public static class Binary extends Expr {
     Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

  @Override
  <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

   final Expr left;
   final Token operator;
   final Expr right;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Binary binary)) return false;
        return Objects.equals(operator, binary.operator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator);
    }
}
public static class Grouping extends Expr {
     Grouping(Expr expression) {
      this.expression = expression;
    }

  @Override
  <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

   final Expr expression;
   }
public static class Literal extends Expr {
     Literal(Object value) {
      this.value = value;
    }

  @Override
  <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

   final Object value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Literal literal)) return false;
        return Objects.equals(value, literal.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
public static class Unary extends Expr {
     Unary(Token operator , Expr right) {
      this.operator = operator;
      this.right = right;
    }

  @Override
  <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

   final Token operator ;
   final Expr right;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Unary unary)) return false;
        return Objects.equals(operator, unary.operator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator);
    }
}

   abstract <R> R accept(Visitor<R> visitor);
}
