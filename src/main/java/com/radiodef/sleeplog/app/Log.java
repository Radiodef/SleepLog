package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;

final class Log {
    private Log() {
    }
    
    static void note(String fmt, Object... args) {
        Tools.getCaller().ifPresent(c -> {
            System.out.print(Tools.getSimpleName(c.getDeclaringClass()));
            System.out.print('.');
            System.out.print(c.getMethodName());
            System.out.print(": ");
        });
        
        System.out.printf(fmt, args);
        System.out.println();
    }
}