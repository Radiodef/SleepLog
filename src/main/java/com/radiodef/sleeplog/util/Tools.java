package com.radiodef.sleeplog.util;

import java.util.*;

import com.google.common.base.Strings;
import java.time.*;
import java.time.format.*;
import org.apache.commons.lang3.time.*;

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
    
    private static final DateTimeFormatter START_FORMATTER =
        DateTimeFormatter.ofPattern("h:mm a");
    
    public static String formatInstant(Instant i) {
        return START_FORMATTER.format(LocalTime.ofInstant(i, ZoneId.systemDefault()));
    }
    
    public static String formatDuration(Duration d) {
        return DurationFormatUtils.formatDuration(d.toMillis(), "HH:mm:ss");
    }
}