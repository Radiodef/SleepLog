package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;

import java.util.*;
import org.apache.commons.lang3.ArrayUtils;

public final class Log {
    private Log() {
    }
    
    public static void enter() {
        note(Tools.getCaller(), "entered", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }
    
    public static void caught(Throwable x) {
        x.printStackTrace(System.out);
    }
    
    public static void note(String fmt, Object... args) {
        note(Tools.getCaller(), fmt, args);
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void note(Optional<StackWalker.StackFrame> caller, String fmt, Object[] args) {
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