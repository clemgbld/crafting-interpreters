package com.craftinginterpreters.lox;

import java.util.function.Consumer;

public class Interpreter implements Expr.Visitor<Object>{

    private final Consumer<Object> log;

    private final Consumer<RuntimeError> logRuntimeError;

    public Interpreter(Consumer<Object> log, Consumer<RuntimeError> logRuntimeError) {
        this.log = log;
        this.logRuntimeError = logRuntimeError;
    }

    public void interpret(Expr expression){
        try{
            Object value = evaluate(expression);
            log.accept(stringify(value));
        }catch (RuntimeError runtimeError){
            logRuntimeError.accept(runtimeError);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
         switch (expr.operator.type) {
             case GREATER:
                 checkNumberOperand(expr.operator,left,right);
                 return (double) left > (double) right;
             case GREATER_EQUAL:
                 checkNumberOperand(expr.operator,left,right);
                 return (double) left >= (double) right;
             case LESS:
                 checkNumberOperand(expr.operator,left,right);
                 return (double) left < (double) right;
             case LESS_EQUAL:
                 checkNumberOperand(expr.operator,left,right);
                 return (double) left <= (double) right;
             case BANG_EQUAL:
                 return isEqual(left,right);
             case EQUAL_EQUAL:
                 return !isEqual(left,right);
            case MINUS :
                checkNumberOperand(expr.operator,left,right);
                return (double) left - (double) right;
            case SLASH :
                checkNumberOperand(expr.operator,left,right);
                return (double) left / (double) right;
            case STAR :
                checkNumberOperand(expr.operator,left,right);
                return (double) left * (double) right;
            case PLUS :
                    if(left instanceof Double && right instanceof Double){
                        return (double) left + (double) right;
                    }
                    if(left instanceof String && right instanceof String){
                        return  left + (String) right;
                    }

                    if(left instanceof String && right instanceof Double){
                        return  left + stringify(right);
                    }

                    if(left instanceof Double && right instanceof String){
                    return stringify(left) +  right;
                    }

                    throw new RuntimeError(expr.operator,"Operands must be either number or strings.");
        };
        return null;
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
    public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);
         switch (expr.operator.type) {
            case MINUS :
                    checkNumberOperand(expr.operator,right);
                    return -(double) right;
             case BANG : return !isTruthy(right);
        };
         return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if(operand instanceof Double) return;
        throw new RuntimeError(operator,"Operand must be a number.");
    }

    private void checkNumberOperand(Token operator, Object left,Object right) {
        if(left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator,"Operands must be a number.");
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    private boolean isEqual(Object a, Object b){
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";
        if(object instanceof Double){
            String text = object.toString();
            if(text.endsWith(".0")){
                return text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private boolean isTruthy(Object object) {
        if(object == null) return false;
        if(object instanceof Boolean) return (boolean) object;
        return true;
    }
}
