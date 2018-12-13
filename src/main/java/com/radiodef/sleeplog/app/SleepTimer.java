package com.radiodef.sleeplog.app;

import javafx.animation.*;
import javafx.event.*;
import javafx.util.*;

class SleepTimer {
    private static final int TICKS_PER_SECOND = 1;
    
    private final Timeline timeline;
    
    SleepTimer() {
        this(TICKS_PER_SECOND);
    }
    
    private SleepTimer(int ticksPerSecond) {
        final var dur = Duration.millis(1000.0 / ticksPerSecond);
        timeline = new Timeline(new KeyFrame(dur, this::tick));
        timeline.setCycleCount(Timeline.INDEFINITE);
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
                timeline.play();
            } else {
                timeline.stop();
            }
            Log.note("isRunning = %b", isRunning());
        }
    }
    
    private void tick(ActionEvent e) {
        Log.enter();
    }
}
