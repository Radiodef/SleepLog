package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;
import com.radiodef.sleeplog.util.*;

import javafx.application.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
        
        primaryStage.setOnCloseRequest(e -> exit());
        
        var timerPane = new TimerPane();
        timerPane.addSleepPeriodListener(this::sleepPeriodAdded);
        
        var content = new BorderPane();
        content.setTop(createMenuBar());
        content.setCenter(timerPane);
        
        var scene = new Scene(content);
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
    
    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        
        if (Tools.isMac()) {
            menuBar.useSystemMenuBarProperty().set(true);
            
        } else {
            var fileMenu = new Menu("File");
            var exit = new MenuItem("Exit");
            
            exit.setOnAction(e -> exit());
            
            fileMenu.getItems().add(exit);
            menuBar.getMenus().add(fileMenu);
        }
        
        return menuBar;
    }
    
    private void sleepPeriodAdded(Instant start, Instant end) {
        if (db.insertNewPeriod(start, end)) {
            Log.note("insertion succeeded");
            db.printAllRows();
        } else {
            Log.note("insertion failed");
        }
    }
    
    private void exit() {
        db.close();
        Platform.exit();
    }
}