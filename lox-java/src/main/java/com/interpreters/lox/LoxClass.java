package com.interpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoxClass extends LoxInstance implements LoxCallable {
    private final String name;
    private final LoxClass superclass;
    private final Map<String, LoxFunction> methods = new HashMap<>();

    public LoxClass(LoxClass metaClass, LoxClass superclass, String name, Map<String, LoxFunction> methods) {
        super(metaClass);
        this.name = name;
        this.superclass = superclass;
        this.methods.putAll(methods);
    }

    public String getName() {
        return name;
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (null != superclass) {
            return superclass.findMethod(name);
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        return null == initializer ? 0 : initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (null != initializer) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }
}
