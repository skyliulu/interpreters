package com.interpreters.lox;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    public static final Object uninitialized = new Object();
    private final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, null == value ? uninitialized : value);
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.getLexeme())) {
            values.put(name.getLexeme(), value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "'.");
    }

    public Object get(Token name) {
        if (values.containsKey(name.getLexeme())) {
            Object value = values.get(name.getLexeme());
            // challenge 8.2
            if (uninitialized == value) {
                throw new RuntimeError(name, "Variable must be initialized before use.");
            }
            return value;
        }
        if (null != enclosing) {
            return enclosing.get(name);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "'.");
    }

    public Object getAt(int distance, Token name) {
        return ancestor(distance).values.get(name.getLexeme());
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.getLexeme(), value);
    }

    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            if (environment != null) {
                environment = environment.enclosing;
            }
        }
        return environment;
    }
}
