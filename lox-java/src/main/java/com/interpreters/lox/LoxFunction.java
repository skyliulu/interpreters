package com.interpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final String name;
    private final Expr.Function declaration;
    private final Environment closure;

    public LoxFunction(Expr.Function declaration, Environment closure) {
        this(null, declaration, closure);
    }

    public LoxFunction(String name, Expr.Function declaration, Environment closure) {
        this.name = name;
        this.declaration = declaration;
        this.closure = closure;
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment);
    }

    @Override
    public int arity() {
        return declaration.getParams().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < arguments.size(); i++) {
            environment.define(declaration.getParams().get(i).getLexeme(), arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.getBody(), environment);
        } catch (Return returnStmt) {
            return returnStmt.getValue();
        }
        return null;
    }

    @Override
    public String toString() {
        if (null == name) {
            return "<fn>";
        } else {
            return String.format("<fn %s>", name);
        }
    }
}
