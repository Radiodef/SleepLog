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
    
    private MenuBars menuBars;
    
    private Stage primaryStage;
    private Stage tableViewStage;
    
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
        
        setUserAgentStylesheet(STYLESHEET_MODENA);
        Platform.setImplicitExit(false);
        
        configurePrimaryStage(primaryStage);
        configureTableViewStage(tableViewStage);
        primaryStage.show();
    }
    
    Stage getPrimaryStage() {
        return primaryStage;
    }
    
    Stage getTableViewStage() {
        return tableViewStage;
    }
    
    private void configurePrimaryStage(Stage stage) {
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
        var content = new BorderPane();
        var table = new DatabaseTablePane(db);
        table.setId("db-table");
        
        content.setCenter(table);
    
        if (Tools.isMac()) {
            content.setTop(menuBars.get());
        }
        
        stage.setScene(new Scene(content));
        
        stage.setWidth(primaryStage.getWidth());
        stage.setHeight(primaryStage.getHeight());
        
        var bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        
        stage.setTitle("Data");
        stage.setOnCloseRequest(e -> setTableViewVisible(false));
    }
    
    void setTableViewVisible(boolean visible) {
        if (visible) {
            tableViewStage.show();
            tableViewStage.toFront();
        } else {
            if (tableViewStage != null) {
                tableViewStage.hide();
            }
        }
        Window.getWindows().stream()
            .map(w -> (MenuBar) w.getScene().lookup("#" + MenuBars.MENU_BAR_ID))
            .flatMap(b -> b.getMenus().stream().flatMap(m -> m.getItems().stream()))
            .filter(CheckMenuItem.class::isInstance)
            .filter(i -> i.getText().contains("Table"))
            .forEach(i -> ((CheckMenuItem) i).setSelected(visible));
    }
    
    private void sleepPeriodAdded(Instant start, Instant end) {
        if (db.insertNewPeriod(start, end)) {
            Log.note("insertion succeeded");
//            db.printAllRows();
            
            if (tableViewStage != null) {
                ((DatabaseTablePane) tableViewStage.getScene().lookup("#db-table")).update();
            }
            
        } else {
            Log.note("insertion failed");
        }
    }
    
    void exit() {
        db.close();
        Platform.exit();
    }
}