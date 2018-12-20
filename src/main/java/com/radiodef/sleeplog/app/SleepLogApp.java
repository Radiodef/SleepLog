package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;
import com.radiodef.sleeplog.util.*;

import javafx.application.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.*;

import java.time.*;

public final class SleepLogApp extends Application {
    private Database db;
    
    private Stage primaryStage;
    
    static void launch() {
        Log.enter();
        launch(SleepLogApp.class, SleepLogMain.getArguments().toArray(new String[0]));
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
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
            // TODO: figure out what this does on other systems
            menuBar.setUseSystemMenuBar(true);
            
        } else {
            var fileMenu = new Menu("_File");
            fileMenu.setMnemonicParsing(true);
            
            var exitItem = new MenuItem("E_xit");
            exitItem.setMnemonicParsing(true);
            // TODO: find out what the correct key is
//            exitItem.setAccelerator(KeyCombination.valueOf("Shortcut+X"));
            
            exitItem.setOnAction(e -> exit());
            
            fileMenu.getItems().add(exitItem);
            menuBar.getMenus().add(fileMenu);
        }
        
        var windowMenu = new Menu(Tools.isMac() ? "_Window" : "_View");
        windowMenu.setMnemonicParsing(true);
        
        var tableItem = new CheckMenuItem("Database _Table View");
        tableItem.setMnemonicParsing(true);
        tableItem.setAccelerator(KeyCombination.valueOf("Shortcut+T"));
        
        tableItem.selectedProperty().addListener((p, o, val) -> setTableViewVisible(val));
        
        windowMenu.getItems().add(tableItem);
        menuBar.getMenus().add(windowMenu);
        
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
    
    private void setTableViewVisible(boolean visible) {
        // TODO
        Log.notef("%b", visible);
    }
}