package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.event.*;

import java.time.*;
import java.util.*;

class TimerPane extends BorderPane {
    private final Label startTime;
    private final Label duration;
    
    private final Button startButton;
    
    private final SleepTimer timer;
    
    private final Set<InstantBiConsumer> listeners = new LinkedHashSet<>();
    
    private static Label createTimeLabel() {
        var l = new Label("--");
        l.getStyleClass().add("time-label");
        return l;
    }
    
    TimerPane() {
        getStyleClass().add("timer-pane");
        
        startTime = createTimeLabel();
        duration = createTimeLabel();
        
        startButton = new Button("Start");
        
        var rows = new VBox();
        rows.getStyleClass().add("timer-rows");
        
        rows.getChildren()
            .addAll(new Label("Start Time:"),
                    startTime,
                    new Label("Duration:"),
                    duration,
                    startButton);
        
        setCenter(rows);
        
        timer = new SleepTimer();
        timer.addListener(this::timerTicked);
        startButton.setOnAction(this::startClicked);
    }
    
    void addSleepPeriodListener(InstantBiConsumer l) {
        listeners.add(Objects.requireNonNull(l));
    }
    
    private void startClicked(ActionEvent e) {
        Log.enter();
        
        timer.toggle();
        startButton.setText(timer.isRunning() ? "Stop" : "Start");
        
        if (!timer.isRunning()) {
            var start = timer.getStart();
            var end = timer.getEnd();
            
            listeners.forEach(l -> l.accept(start, end));
        }
    }
    
    private void timerTicked(Instant start, Instant current) {
        startTime.setText(Tools.formatInstant(start));
        duration.setText(Tools.formatDuration(Duration.between(start, current)));
    }
}
