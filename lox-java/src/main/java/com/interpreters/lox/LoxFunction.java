package com.interpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;

    public LoxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.getParams().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.getGlobalEnv());
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
        return String.format("<fn %s>", declaration.getName().getLexeme());
    }
}
