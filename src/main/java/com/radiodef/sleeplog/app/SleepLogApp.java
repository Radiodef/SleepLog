package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;
import com.radiodef.sleeplog.util.*;

import javafx.application.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.*;

import java.time.*;

public final class SleepLogApp extends Application {
    private Database db;
    
    private MenuBars menuBars;
    
    private Stage primaryStage;
    private Stage tableViewStage;
    private Stage graphViewStage;
    private Stage hardwareStage;
    
    static void launch() {
        Log.enter();
        launch(SleepLogApp.class, SleepLogMain.getArguments().toArray(new String[0]));
    }
    
    @Override
    public void start(Stage inStage) {
        db = new Database();
        
        if (!db.didConnect()) {
            System.exit(0xBAD_DB);
        }
        
        menuBars = new MenuBars(this);
        
        primaryStage = inStage;
        tableViewStage = new Stage();
        graphViewStage = new Stage();
        hardwareStage = new Stage();
        
        setUserAgentStylesheet(STYLESHEET_MODENA);
        Platform.setImplicitExit(false);
        
        configurePrimaryStage(primaryStage);
        Tools.beforeFirstShow(tableViewStage, this::configureTableViewStage);
        Tools.beforeFirstShow(graphViewStage, this::configureGraphViewStage);
        Tools.beforeFirstShow(hardwareStage, this::configureHardwareStage);
        
        primaryStage.show();
    }
    
    Stage getPrimaryStage() {
        return Tools.requireNonNullState(primaryStage, "primaryStage");
    }
    
    Stage getTableViewStage() {
        return Tools.requireNonNullState(tableViewStage, "tableViewStage");
    }
    
    Stage getGraphViewStage() {
        return Tools.requireNonNullState(graphViewStage, "graphViewStage");
    }
    
    Stage getHardwareStage() {
        return Tools.requireNonNullState(hardwareStage, "hardwareStage");
    }
    
    private void configurePrimaryStage(Stage stage) {
        Log.enter();
        
        // exit listener
        
        stage.setOnCloseRequest(e -> exit());
        
        // content
        
        var timerPane = new TimerPane();
        timerPane.addSleepPeriodListener(this::sleepPeriodAdded);
        
        var content = new BorderPane();
        content.setTop(menuBars.get());
        content.setCenter(timerPane);
        
        var scene = new Scene(content);
        scene.getStylesheets().add("styles.css");
        stage.setScene(scene);
        
        // sizing
        
        var bounds = Screen.getPrimary().getVisualBounds();
        Log.notef("primary screen bounds = %s", bounds);
        
        final double ratio = 0.5;
        stage.setWidth(ratio * bounds.getWidth());
        stage.setHeight(ratio * bounds.getHeight());
        
        stage.setX(bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2);
        
        // title
        
        stage.setTitle("Sleep Log");
    }
    
    private void configureTableViewStage(Stage stage) {
        Log.enter();
        configureSecondaryStage(stage, new DatabaseTablePane(db));
        
        var bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX() + 10);
        stage.setY(bounds.getMinY() + 10);
        
        stage.setTitle("Data");
    }
    
    private void configureGraphViewStage(Stage stage) {
        Log.enter();
        configureSecondaryStage(stage, new GraphsPane(db));
        
        var bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMaxX() - stage.getWidth() - 10);
        stage.setY(bounds.getMaxY() - stage.getHeight() - 10);
        
        stage.setTitle("Graphs");
    }
    
    private void configureHardwareStage(Stage stage) {
        Log.enter();
        configureSecondaryStage(stage, new BorderPane());
        
        var bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMaxX() - stage.getWidth() - 10);
        stage.setY(bounds.getMinY() + 10);
        
        stage.setTitle("Hardware History");
    }
    
    private void configureSecondaryStage(Stage stage, Pane pane) {
        var content = new BorderPane();
        content.setCenter(pane);
        
        if (Tools.isMac()) {
            content.setTop(menuBars.get());
        }
        
        var scene = new Scene(content);
        scene.getStylesheets().add("styles.css");
        stage.setScene(scene);
        
        stage.setWidth(primaryStage.getWidth());
        stage.setHeight(primaryStage.getHeight());
    }
    
    private void sleepPeriodAdded(Instant start, Instant end) {
        if (db.insertNewPeriod(start, end)) {
            Log.note("insertion succeeded");
//            db.printAllRows();
        } else {
            Log.note("insertion failed");
        }
    }
    
    void exit() {
        db.close();
        Platform.exit();
    }
}