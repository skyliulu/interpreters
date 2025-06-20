package com.interpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final String name;
    private final Expr.Function declaration;
    private final Environment closure;
    private final boolean inInitializer;

    public LoxFunction(Expr.Function declaration, Environment closure, boolean inInitializer) {
        this(null, declaration, closure, inInitializer);
    }

    public LoxFunction(String name, Expr.Function declaration, Environment closure, boolean inInitializer) {
        this.inInitializer = inInitializer;
        this.name = name;
        this.declaration = declaration;
        this.closure = closure;
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment, inInitializer);
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
            if (inInitializer) {
                return closure.getAt(0, "this");
            }
            return returnStmt.getValue();
        }
        if (inInitializer) {
            return closure.getAt(0, "this");
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
