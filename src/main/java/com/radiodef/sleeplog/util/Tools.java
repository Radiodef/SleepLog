package com.radiodef.sleeplog.util;

import java.util.*;
import java.util.function.*;
import java.time.*;
import java.time.format.*;

import javafx.event.*;
import javafx.stage.*;
import javafx.collections.*;

import com.google.common.base.Strings;
import org.apache.commons.lang3.*;
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
    
    private static final DateTimeFormatter DETAIL_FORMATTER =
        DateTimeFormatter.ofPattern("EEE, d LLL uu, h:mm:ss a");
    
    public static String formatDetailedInstant(Instant i) {
        return DETAIL_FORMATTER.format(LocalDateTime.ofInstant(i, ZoneId.systemDefault()));
    }
    
    public static String formatDuration(Duration d) {
        return DurationFormatUtils.formatDuration(d.toMillis(), "HH:mm:ss");
    }
    
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ISO_LOCAL_DATE;
    
    public static String formatDate(Instant i) {
        return DATE_FORMATTER.format(LocalDate.ofInstant(i, ZoneId.systemDefault()));
    }
    
    public static Instant toInstant(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }
    
    public static Instant toStartOfDay(Instant i) {
        var zone = ZoneId.systemDefault();
        return LocalDate.ofInstant(i, zone).atStartOfDay(zone).toInstant();
    }
    
    public static boolean isMac() {
        // https://developer.apple.com/library/content/technotes/tn2002/tn2110.html
        // http://archive.is/w6JC0
        return StringUtils.contains(SystemUtils.OS_NAME, "OS X");
    }
    
    public static boolean isWindows() {
        return StringUtils.contains(SystemUtils.OS_NAME, "Windows");
    }
    
    public static void beforeFirstShow(Stage s, Consumer<? super Stage> action) {
        var prev = s.onShowingProperty().get();
        
        // noinspection Convert2Lambda
        s.setOnShowing(new EventHandler<>() {
            @Override
            public void handle(WindowEvent e) {
                if (prev != null)
                    prev.handle(e);
                s.setOnShowing(prev);
                action.accept(s);
            }
        });
    }
    
    @SafeVarargs
    public static <E> ObservableList<E> observableArrayList(E... elements) {
        var list = FXCollections.<E>observableArrayList();
        Collections.addAll(list, elements);
        return list;
    }
    
    public static final int SECS_PER_HR = 60 * 60;
    public static final int SECS_PER_DAY = 60 * 60 * 24;
}