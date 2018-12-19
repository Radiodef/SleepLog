package com.radiodef.sleeplog.util;

import java.util.*;
import java.sql.*;

import com.google.common.base.Strings;

public final class Tools {
    private Tools() {
    }
    
    public static <T> T requireNonNullState(T obj, String desc) {
        if (obj == null)
            throw new IllegalStateException(desc);
        return obj;
    }
    
    public static Optional<StackWalker.StackFrame> getCaller() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                          .walk(s -> s.skip(2).findFirst());
    }
    
    public static String getSimpleName(Class<?> c) {
        String s = c.getSimpleName();
        
        if (Strings.isNullOrEmpty(s)) {
            s = c.getName();
            int dot = s.lastIndexOf('.');
            if (dot >= 0) {
                s = s.substring(dot + 1);
            }
        }
        
        return s;
    }
    
    public static boolean isDerbyTableAlreadyExistsException(SQLException x) {
        // https://stackoverflow.com/a/5866339/2891664
        return "X0Y32".equals(x.getSQLState());
    }
}