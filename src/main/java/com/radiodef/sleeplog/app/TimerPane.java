package com.radiodef.sleeplog.app;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import javafx.event.*;

import java.time.*;
import java.time.format.*;
import org.apache.commons.lang3.time.*;

class TimerPane extends BorderPane {
    private final Label startTime;
    private final Label duration;
    
    private final Button startButton;
    
    private final SleepTimer timer;
    
    TimerPane() {
        startTime = new Label("--");
        duration = new Label("--");
        
        startButton = new Button("Start");
        
        var rows = new VBox();
        rows.setSpacing(10);
        rows.setAlignment(Pos.CENTER);
        
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
        DateTimeFormatter.ofPattern("hh:mm a");
    private static String formatInstant(Instant i) {
        return START_FORMATTER.format(LocalTime.ofInstant(i, ZoneId.systemDefault()));
    }
    private static String formatDuration(Duration d) {
        return DurationFormatUtils.formatDuration(d.toMillis(), "HH:MM:ss");
    }
    
    private void timerTicked(Instant start, Instant current) {
        startTime.setText(formatInstant(start));
        duration.setText(formatDuration(Duration.between(start, current)));
    }
}
