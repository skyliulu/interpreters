package com.interpreters.lox;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.getLexeme())) {
            values.put(name.getLexeme(), value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "'.");
    }

    public Object get(Token name) {
        if (values.containsKey(name.getLexeme())) {
            return values.get(name.getLexeme());
        }
        if (null != enclosing) {
            return enclosing.get(name);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "'.");
    }
}
