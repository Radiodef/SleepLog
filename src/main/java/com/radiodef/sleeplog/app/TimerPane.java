package com.radiodef.sleeplog.app;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import javafx.event.*;

class TimerPane extends BorderPane {
    private final Label startTime;
    private final Label elapsedTime;
    
    private final Button startButton;
    
    private final SleepTimer timer;
    
    TimerPane() {
        startTime = new Label("--");
        elapsedTime = new Label("--");
        
        startButton = new Button("Start");
        
        var rows = new VBox();
        rows.setSpacing(10);
        rows.setAlignment(Pos.CENTER);
        
        rows.getChildren()
            .addAll(new Label("Start Time:"),
                    startTime,
                    new Label("Elapsed Time:"),
                    elapsedTime,
                    startButton);
        
        setCenter(rows);
        
        timer = new SleepTimer();
        startButton.setOnAction(this::startClicked);
    }
    
    private void startClicked(ActionEvent e) {
        Log.enter();
        
        timer.toggle();
        startButton.setText(timer.isRunning() ? "Stop" : "Start");
    }
}
