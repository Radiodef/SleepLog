package com.radiodef.sleeplog.util;

public final class Tools {
    private Tools() {
    }
    
    public static void requireEventDispatchThread() {
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(Thread.currentThread().getName());
        }
    }
}