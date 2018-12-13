package com.radiodef.sleeplog.app;

import javafx.animation.*;
import javafx.event.*;

import java.time.*;
import java.util.*;
import java.util.function.*;

class SleepTimer {
    private static final int TICKS_PER_SECOND = 10;
    
    private final Timeline timeline;
    
    private final Set<BiConsumer<LocalDateTime, LocalDateTime>> listeners = new LinkedHashSet<>();
    
    private LocalDateTime start;
    private LocalDateTime current;
    
    SleepTimer() {
        this(TICKS_PER_SECOND);
    }
    
    private SleepTimer(int ticksPerSecond) {
        final var dur = javafx.util.Duration.millis(1000.0 / ticksPerSecond);
        timeline = new Timeline(new KeyFrame(dur, this::tick));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    void addListener(BiConsumer<LocalDateTime, LocalDateTime> l) {
        listeners.add(Objects.requireNonNull(l));
    }
    
    void toggle() {
        setRunning(!isRunning());
    }
    
    boolean isRunning() {
        return timeline.getStatus() == Timeline.Status.RUNNING;
    }
    
    private void setRunning(boolean isRunning) {
        if (isRunning != isRunning()) {
            if (isRunning) {
                start = current = LocalDateTime.now();
                notifyListeners();
                timeline.play();
            } else {
                timeline.stop();
            }
            Log.note("isRunning = %b", isRunning());
        }
    }
    
    private void notifyListeners() {
        listeners.forEach(l -> l.accept(start, current));
    }
    
    private void tick(ActionEvent e) {
//        Log.enter();
        current = LocalDateTime.now();
        notifyListeners();
    }
}
