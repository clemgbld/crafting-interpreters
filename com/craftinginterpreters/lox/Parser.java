package com.craftinginterpreters.lox;

import java.util.List;
import java.util.function.Supplier;

import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {

    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse(){
        try{
           return expression();
        }catch (ParseError error){
            return null;
        }
    }
    
    private Expr expression(){
        return block();
    }

    private Expr block() {
        Expr expr = equality();
        while (match(COMMA)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }


    private Expr equality() {
        return buildRule(this::comparison,BANG_EQUAL, EQUAL_EQUAL);
    }

    private Expr comparison() {
        return buildRule(this::term,GREATER,GREATER_EQUAL,LESS,LESS_EQUAL);
    }

    private Expr term() {
        return buildRule(this::factor,MINUS,PLUS);
    }

    private Expr factor() {
        return buildRule(this::unary,SLASH,STAR);
    }

    private Expr unary() {
        if(match(BANG,MINUS)){
            Token operator = previous();
            Expr expr = unary();
            return new Expr.Unary(operator,expr);
        }
        return primary();
    }

    private Expr primary() {
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);
        if(match(NUMBER,STRING)){
            return new Expr.Literal(previous().literal);
        }
        if(match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(),"Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();
        throw error(peek(),message);
    }

    private ParseError error(Token token, String message){
        Lox.error(token,message);
        return new ParseError();
    }

    private void synchronize(){
        advance();
        while (!isAtEnd()){
            if(previous().type == SEMICOLON) return;
            switch (peek().type){
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }


    private Expr buildRule(Supplier<Expr> rule, TokenType... types){
        Expr expr = rule.get();
        while (match(types)){
            Token operator = previous();
            Expr right = rule.get();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

    boolean match(TokenType... types){
       for(TokenType type : types){
           if(check(type)){
               advance();
               return true;
           }
       }
        return false;
    }

    private Token advance(){
        if(!isAtEnd()){
            current++;
        }
       return previous();
    }

    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

}
