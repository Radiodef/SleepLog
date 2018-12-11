package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;

import java.util.*;

final class Log {
    private Log() {
    }
    
    static void enter() {
        note(Tools.getCaller(), "entered");
    }
    
    static void note(String fmt, Object... args) {
        note(Tools.getCaller(), fmt, args);
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void note(Optional<StackWalker.StackFrame> caller, String fmt, Object... args) {
        caller.ifPresent(c -> {
            System.out.print(Tools.getSimpleName(c.getDeclaringClass()));
            System.out.print('.');
            System.out.print(c.getMethodName());
            System.out.print(": ");
        });
        
        System.out.printf(fmt, args);
        System.out.println();
    }
}