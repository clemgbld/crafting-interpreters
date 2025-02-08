package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Assign;
import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Expr.Logical;
import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Stmt.*;

import java.util.ArrayList;
import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;

    public Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public int arity() {
                return 0;
            }

           @Override
            public String toString(){
                return "<native fn>";
           }
        });
    }

    public void interpret(List<Stmt> statements) {
        try {
            statements.forEach(this::execute);
        } catch (RuntimeError runtimeError) {
            Lox.runtimeError(runtimeError);
        }
    }
    private void execute(Stmt statement) {
        statement.accept(this);
    }
    
    public void executeBlock(List<Stmt> statements, Environment environment) {
       Environment previous = this.environment;
       try{
        this.environment = environment;
        statements.forEach(this::execute);
       }finally {
        this.environment = previous;
       }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperand(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperand(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double) left <= (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case MINUS:
                checkNumberOperand(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperand(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperand(expr.operator, left, right);
                return (double) left * (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");
        }
        ;
        return null;
    }

    @Override
    public Object visitCallExpr(Call expr) {
       Object calle = evaluate(expr.callee);
       List<Object> arguments = new ArrayList<>();
       expr.arguments.forEach(arg -> {
           arguments.add(evaluate(arg));
       });
       if(!(calle instanceof LoxCallable function)){
           throw new RuntimeError(expr.paren,"Can only call functions and classes.");
       }
        if(arguments.size() != function.arity()){
            throw new RuntimeError(expr.paren,"Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }
        return function.call(this,arguments);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        Object left = evaluate(expr.left);
        if(expr.operator.type == TokenType.OR){
            if(isTruthy(left)) return left;
        }else{
            if(!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
        }
        ;
        return null;
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        Object value = evaluate(expr.value);
         environment.assign(expr.name,value);
         return value;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        environment.define(stmt.name.lexeme, new LoxFunction(stmt,environment));
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if(isTruthy(evaluate(stmt.condition))){
            execute(stmt.thenBranch);
        }else if(stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        Object value = null;
        if(stmt.value != null){
            value = evaluate(stmt.value);
        }
        throw new ReturnException(value);
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        environment.define(stmt.name.lexeme, stmt.initializer != null ? evaluate(stmt.initializer) : null);
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        while (isTruthy(evaluate(stmt.condition))){
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements,new Environment(environment));
        return null;
    }



    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperand(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be a number.");
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                return text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }
}
