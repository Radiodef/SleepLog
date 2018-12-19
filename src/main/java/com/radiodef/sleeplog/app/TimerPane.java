package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.event.*;

import java.time.*;

class TimerPane extends BorderPane {
    private final Label startTime;
    private final Label duration;
    
    private final Button startButton;
    
    private final SleepTimer timer;
    
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
    
    private void startClicked(ActionEvent e) {
        Log.enter();
        
        timer.toggle();
        startButton.setText(timer.isRunning() ? "Stop" : "Start");
    }
    
    private void timerTicked(Instant start, Instant current) {
        startTime.setText(Tools.formatInstant(start));
        duration.setText(Tools.formatDuration(Duration.between(start, current)));
    }
}
