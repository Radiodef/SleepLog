package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;

import javafx.animation.*;
import javafx.event.*;

import java.time.*;
import java.util.*;

class SleepTimer {
    private static final int TICKS_PER_SECOND = 10;
    
    private final Timeline timeline;
    
    private final Set<InstantBiConsumer> listeners = new LinkedHashSet<>();
    
    private Instant start;
    private Instant current;
    
    SleepTimer() {
        this(TICKS_PER_SECOND);
    }
    
    private SleepTimer(int ticksPerSecond) {
        final var dur = javafx.util.Duration.millis(1000.0 / ticksPerSecond);
        timeline = new Timeline(new KeyFrame(dur, this::tick));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    void addListener(InstantBiConsumer l) {
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
                start = current = Instant.now();
                notifyListeners();
                timeline.play();
            } else {
                timeline.stop();
            }
            Log.notef("isRunning = %b", isRunning());
        }
    }
    
    Instant getStart() {
        return Tools.requireNonNullState(start, "start");
    }
    
    Instant getEnd() {
        return Tools.requireNonNullState(current, "current");
    }
    
    private void notifyListeners() {
        listeners.forEach(l -> l.accept(start, current));
    }
    
    private void tick(ActionEvent e) {
//        Log.enter();
        current = Instant.now();
        notifyListeners();
    }
}
