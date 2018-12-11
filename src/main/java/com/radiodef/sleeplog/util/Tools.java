package com.radiodef.sleeplog.util;

public final class Tools {
    private Tools() {
    }
    
    public static <T> T requireNonNullState(T obj, String desc) {
        if (obj == null)
            throw new IllegalStateException(desc);
        return obj;
    }
}