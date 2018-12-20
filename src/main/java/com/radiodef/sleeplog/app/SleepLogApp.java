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
    private Stage tableViewStage;
    
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
        
        stage.setTitle("Sleep Log");
    }
    
    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        menuBar.setId("menu-bar");
        
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
        
        tableItem.setSelected(tableViewStage != null && tableViewStage.isShowing());
        tableItem.selectedProperty().addListener((p, o, val) -> setTableViewVisible(val));
        
        windowMenu.getItems().add(tableItem);
        menuBar.getMenus().add(windowMenu);
        
        return menuBar;
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
    
    private void exit() {
        db.close();
        Platform.exit();
    }
    
    private Stage createTableViewStage() {
        var content = new BorderPane();
        var table = new DatabaseTablePane(db);
        table.setId("db-table");
        
        content.setCenter(table);
    
        if (Tools.isMac()) {
            content.setTop(createMenuBar());
        }
        
        var stage = new Stage();
        stage.setScene(new Scene(content));
        
        stage.setWidth(primaryStage.getWidth());
        stage.setHeight(primaryStage.getHeight());
        
        var bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        
        stage.setTitle("Data");
        stage.setOnCloseRequest(e -> setTableViewVisible(false));
        return stage;
    }
    
    private void setTableViewVisible(boolean visible) {
        if (visible) {
            if (tableViewStage == null) {
                tableViewStage = createTableViewStage();
            }
            tableViewStage.show();
            tableViewStage.toFront();
        } else {
            if (tableViewStage != null) {
                tableViewStage.hide();
            }
        }
        Window.getWindows().stream()
            .map(w -> (MenuBar) w.getScene().lookup("#menu-bar"))
            .flatMap(b -> b.getMenus().stream().flatMap(m -> m.getItems().stream()))
            .filter(CheckMenuItem.class::isInstance)
            .filter(i -> i.getText().contains("Table"))
            .forEach(i -> ((CheckMenuItem) i).setSelected(visible));
    }
}