package com.radiodef.sleeplog.app;

final class Log {
    private Log() {
    }
    
    static void note(String fmt, Object... args) {
        System.out.printf(fmt, args);
        System.out.println();
    }
}