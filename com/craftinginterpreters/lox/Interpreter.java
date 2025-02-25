package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.*;
import com.craftinginterpreters.lox.Stmt.*;
import com.craftinginterpreters.lox.Stmt.Class;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;

    private final Map<Expr, Integer> locals = new HashMap<>();

    private final Consumer<String> log;

    private final Consumer<RuntimeError> logError;

    public Interpreter(Consumer<String> log, Consumer<RuntimeError> logError) {
        this.log = log;
        this.logError = logError;
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
            public String toString() {
                return "<native fn>";
            }
        });
    }

    public void interpret(List<Stmt> statements) {
        try {
            statements.forEach(this::execute);
        } catch (RuntimeError runtimeError) {
            logError.accept(runtimeError);
        }
    }

    private void execute(Stmt statement) {
        statement.accept(this);
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            statements.forEach(this::execute);
        } finally {
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
        if (!(calle instanceof LoxCallable function)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Get expr) {
        Object object = evaluate(expr.object);
        if(object instanceof  LoxInstance){
            return ((LoxInstance) object).get(expr.name);
        }
        throw new RuntimeError(expr.name,"Only instances have properties");
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
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Set expr) {
        Object object = evaluate(expr.object);
        if(!(object instanceof LoxInstance)){
            throw new RuntimeError(expr.name,"Only instances have fields");
        }
        Object value = evaluate(expr.value);
        ((LoxInstance) object).set(expr.name,value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Super expr) {
        int distance = locals.get(expr);
        LoxClass superclass = (LoxClass) environment.getAt(distance,"super");
        LoxInstance object = (LoxInstance) environment.getAt(distance - 1,"this");
        LoxFunction method = superclass.findMethod(expr.method.lexeme);
        if(method == null){
            throw new RuntimeError(expr.method,"Undefined property " + "'" + expr.method.lexeme + "'.");
        }
        return method.bind(object);
    }

    @Override
    public Object visitInnerExpr(Inner expr) {
        int distance = locals.get(expr);
        MethodInfo methodInfo = (MethodInfo) environment.getAt(distance,"inner");
        LoxInstance object = (LoxInstance) environment.getAt(distance - 1,"this");
         LoxClass superClass = object.klass.findSuperClassByName(methodInfo.className());
         LoxFunction innerMethod = superClass.findInnerMethod(methodInfo.name().lexeme);
        return innerMethod.bind(object);
    }

    @Override
    public Object visitThisExpr(This expr) {
        return lookupVariable(expr.keyword, expr);
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
        return lookupVariable(expr.name, expr);
    }

    private Object lookupVariable(Token name, Expr expr) {
        if (locals.containsKey(expr)) {
            int depth = locals.get(expr);
            return environment.getAt(depth, name.lexeme);
        }
        return globals.get(name);
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        Object value = evaluate(expr.value);
        if (locals.containsKey(expr)) {
            int depth = locals.get(expr);
            environment.assignAt(depth, expr.name.lexeme, value);
            return value;
        }
        globals.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        environment.define(stmt.name.lexeme, new LoxFunction(stmt, environment,false));
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        log.accept(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        Object value = null;
        if (stmt.value != null) {
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
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Class stmt) {
        Object superClass = null;
        if(stmt.superClass != null){
            superClass = evaluate(stmt.superClass);
            if(!(superClass instanceof LoxClass)){
                throw new RuntimeError(stmt.superClass.name,
                        "Superclass must be a class.");
            }
        }
        environment.define(stmt.name.lexeme, null);
        Map<String,LoxFunction> methods = new HashMap<>();
        stmt.methods.forEach(method -> {
            environment = new Environment(environment);
            environment.define("inner", new MethodInfo(method.name,stmt.name.lexeme));
            LoxFunction func = new LoxFunction(method,environment,method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme,func);
        });
        LoxClass klass = new LoxClass(stmt.name.lexeme,(LoxClass) superClass,methods);
        environment = environment.enclosing;
        if(superClass != null){
            ((LoxClass) superClass).setSubClass(klass);
        }
        environment.assign(stmt.name, klass);
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

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }
}
