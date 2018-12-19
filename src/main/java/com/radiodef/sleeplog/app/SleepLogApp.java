package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;

import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;

import java.time.*;

public final class SleepLogApp extends Application {
    private Database db;
    
    static void launch() {
        Log.enter();
        launch(SleepLogApp.class, SleepLogMain.getArguments().toArray(new String[0]));
    }
    
    @Override
    public void start(Stage primaryStage) {
        db = new Database();
        
        if (!db.didConnect()) {
            System.exit(0xBAD_DB);
        }
        
        setUserAgentStylesheet(STYLESHEET_MODENA);
        
        Platform.setImplicitExit(false);
        
        primaryStage.setOnCloseRequest(e -> {
            db.close();
            Platform.exit();
        });
        
        var timerPane = new TimerPane();
        timerPane.addSleepPeriodListener(this::sleepPeriodAdded);
        
        var scene = new Scene(timerPane);
        scene.getStylesheets().add("styles.css");
        primaryStage.setScene(scene);
        
        configure(primaryStage);
        primaryStage.show();
    }
    
    private void configure(Stage stage) {
        var bounds = Screen.getPrimary().getVisualBounds();
        Log.notef("primary screen bounds = %s", bounds);
        
        final double ratio = 0.5;
        stage.setWidth(ratio * bounds.getWidth());
        stage.setHeight(ratio * bounds.getHeight());
        
        stage.setX(bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2);
    }
    
    private void sleepPeriodAdded(Instant start, Instant end) {
        if (db.insertNewPeriod(start, end)) {
            Log.note("insertion succeeded");
            db.printAllRows();
        } else {
            Log.note("insertion failed");
        }
    }
}