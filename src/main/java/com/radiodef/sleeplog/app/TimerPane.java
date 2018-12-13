package com.radiodef.sleeplog.app;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.event.*;

import java.time.*;
import java.time.format.*;
import org.apache.commons.lang3.time.*;

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
    
    private static final DateTimeFormatter START_FORMATTER =
        DateTimeFormatter.ofPattern("h:mm a");
    private static String formatInstant(Instant i) {
        return START_FORMATTER.format(LocalTime.ofInstant(i, ZoneId.systemDefault()));
    }
    private static String formatDuration(Duration d) {
        return DurationFormatUtils.formatDuration(d.toMillis(), "HH:mm:ss");
    }
    
    private void timerTicked(Instant start, Instant current) {
        startTime.setText(formatInstant(start));
        duration.setText(formatDuration(Duration.between(start, current)));
    }
}
