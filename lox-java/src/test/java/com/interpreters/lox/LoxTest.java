package com.interpreters.lox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoxTest {

    @Test
    void testScopeValue() {
        Lox.run("""
                var a = "global a";
                var b = "global b";
                var c = "global c";
                {
                  var a = "outer a";
                  var b = "outer b";
                  {
                    var a = "inner a";
                    print a;
                    print b;
                    print c;
                  }
                  print a;
                  print b;
                  print c;
                }
                print a;
                print b;
                print c;
                """);
    }

    @Test
    void testLoop() {
        Lox.run("""
                var a = 0;
                var temp;
                var count = 0;
                
                for (var b = 1; a < 10000; b = temp + b) {
                  print a;
                  temp = a;
                  a = b;
                  count = count +1;
                  if(count > 10) {
                   break;
                  }
                }
                """);
    }

    @Test
    void testFun() {
        Lox.run("""
                fun sayHi(first, last) {
                  print "Hi, " + first + " " + last + "!";
                }
                sayHi("Dear", "Reader");
                """);
        Lox.run("""
                fun fib(n) {
                  if (n <= 1) return n;
                  return fib(n - 2) + fib(n - 1);
                }
                
                for (var i = 0; i < 20; i = i + 1) {
                  print fib(i);
                }
                """);
        Lox.run("""
                fun makeCounter() {
                  var i = 0;
                  fun count() {
                    i = i + 1;
                    print i;
                  }
                
                  return count;
                }
                
                var counter = makeCounter();
                counter();
                counter();
                """);
        Lox.run("""
                fun whichFn(fn) {
                  print fn;
                }
                
                whichFn(fun (b) {
                 print b;
                });
                
                fun named(a) { print a; }
                whichFn(named);
                """);
    }

    @Test
    void testClosure() {
        Lox.run("""
                var a = "global";
                {
                  fun showA() {
                    print a;
                  }
                
                  showA();
                  var a = "block";
                  showA();
                  print a;
                }
                """);
    }

    @Test
    void testClass() {
        Lox.run("""
                class Bacon {
                  eat() {
                    print "Crunch crunch crunch!";
                  }
                }
                
                Bacon().eat();
                """);
    }
}