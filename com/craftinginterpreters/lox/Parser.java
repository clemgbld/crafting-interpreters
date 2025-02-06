package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Logical;
import com.craftinginterpreters.lox.Stmt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {


    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;

    private final BiConsumer<Token,String> logParseError;
    private int current = 0;

    public Parser(List<Token> tokens, BiConsumer<Token, String> logParseError) {
        this.tokens = tokens;
        this.logParseError = logParseError;
    }

    List<Stmt> parse(){
       List<Stmt> statements = new ArrayList<>();
       while (!isAtEnd()){
           statements.add(declaration(false));
       }
           return statements;
    }
    private Stmt declaration(boolean isBreak){
        try{
            if(match(VAR)) return varDeclaration();
            return isBreak ? breakStatement() : statement();
        }catch (ParseError error){
            synchronize();
            return null;
        }
    }
    private Stmt varDeclaration(){
       Token name = consume(IDENTIFIER, "Expect variable name");
       Expr initializer = null;
       if(match(EQUAL)){
           initializer = expression();
       }
       consume(SEMICOLON, "Expect ';' after value");
       return new Var(name,initializer);
    }

    private Stmt statement() {
       return statement(false);
    }

    private Stmt statement(boolean isBreak) {
        if(match(WHILE)) return whileStatement();
        if(match(FOR)) return forStatement();
        if(match(IF)) return ifStatement(isBreak);
        if(match(PRINT)) return printStatement();
        if(match(LEFT_BRACE)) return new Block(block(isBreak));
        return expressionStatement();
    }

    private Stmt breakStatement() {
        if(match(BREAK)){
                var breakStmt = new Stmt.Break();
                consume(SEMICOLON,"Expect ';' after loop break.");
                return breakStmt;
        }
       return statement(true);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

/* Control Flow for-statement < Control Flow for-initializer
    // More here...
*/
//> for-initializer
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }
//< for-initializer
//> for-condition

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");
//< for-condition
//> for-increment

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
//< for-increment
//> for-body
        Stmt body = breakStatement();

//> for-desugar-increment
        if (increment != null) {
            body = new Stmt.Block(
                    List.of(
                            body,
                            new Stmt.Expression(increment)));
        }

//< for-desugar-increment
//> for-desugar-condition
        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

//< for-desugar-condition
//> for-desugar-initializer
        if (initializer != null) {
            body = new Stmt.Block(List.of(initializer, body));
        }
//< for-desugar-initializer
        return body;
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = breakStatement();

        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement(boolean isBreak) {
        consume(LEFT_PAREN,"Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt thenBranch = isBreak ? breakStatement() : statement();
        Stmt elseBranch = null;
        if(match(ELSE)){
            elseBranch = isBreak ? breakStatement() : statement();
        }
        return new Stmt.If(condition,thenBranch,elseBranch);
    }

    private List<Stmt> block(boolean isBreak) {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration(isBreak));
        }
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt printStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after value");
        return new Print(expr);
    }

    Stmt expressionStatement(){
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after value");
        return new Expression(expr);
    }

    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){

        Expr expr = or();

       if(match(EQUAL)){
           Token equals = previous();
           Expr value = assignment();
           if(expr instanceof Expr.Variable){
               Token name = ((Expr.Variable) expr).name;
               return  new Expr.Assign(name,value);
           }
           error(equals,"Invalid assignment target");
       }

        return expr;
    }

    private Expr or(){
        Expr expr = and();
        while (match(OR)){
            Token operator = previous();
            Expr right = and();
            expr = new Logical(expr,operator,right);
        }
       return expr;
    }

   private Expr and(){
        Expr expr = equality();
        while (match(AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Logical(expr,operator,right);
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

       if(match(IDENTIFIER)){
           return new Expr.Variable(previous());
       }

       if(check(BREAK)){
           throw error(peek(),"Break not in a loop.");
       }

        throw error(peek(),"Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();
        throw error(peek(),message);
    }

    private ParseError error(Token token, String message){
        logParseError.accept(token,message);
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
                case BREAK:
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
        return tokens.get(current -1);
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
