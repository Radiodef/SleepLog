package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;

import java.util.*;
import java.util.function.*;
import java.sql.*;

public final class Log {
    private Log() {
    }
    
    public static void enter() {
        note(Tools.getCaller(), "entered", null, false);
    }
    
    public static void caught(Throwable x) {
        x.printStackTrace(System.out);
    }
    
    public static void note(Object msg) {
        note(Tools.getCaller(), String.valueOf(msg), null, false);
    }
    
    public static void notef(String fmt, Object... args) {
        note(Tools.getCaller(), fmt, args, true);
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void note(Optional<StackWalker.StackFrame> caller,
                             String str,
                             Object[] args,
                             boolean isFormat) {
        caller.ifPresent(c -> {
            System.out.print(Tools.getSimpleName(c.getDeclaringClass()));
            System.out.print('.');
            System.out.print(c.getMethodName());
            System.out.print(": ");
        });
        
        if (isFormat) {
            System.out.printf(str, args);
        } else {
            System.out.print(str);
        }
        System.out.println();
    }
    
    public static <T, U> Function<T, U> catchingSQL(ThrowingFunction<T, U, SQLException> fn) {
        return arg -> {
            try {
                return fn.apply(arg);
            } catch (SQLException x) {
                caught(x);
                return null;
            }
        };
    }
}