package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;

import java.time.*;
import java.util.*;

class DatabaseTablePane extends BorderPane {
    private final Database db;
    private final TableView<SleepPeriod> table;
    
    DatabaseTablePane(Database db) {
        this.db = Objects.requireNonNull(db, "db");
        
        var idCol = new TableColumn<SleepPeriod, Integer>("ID");
        var startCol = new TableColumn<SleepPeriod, Instant>("Start");
        var endCol = new TableColumn<SleepPeriod, Instant>("End");
        
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        startCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("end"));
        
        this.table = new TableView<>();
        table.setEditable(false);
        
        Collections.addAll(table.getColumns(), idCol, startCol, endCol);
        setCenter(table);
        
        var deleteButton = new Button("Delete");
        deleteButton.setDisable(true);
        
        table.getSelectionModel()
             .selectedIndexProperty()
             .addListener((a, b, value) -> deleteButton.setDisable(value.intValue() == -1));
        
        var tools = new ToolBar();
        tools.setOrientation(Orientation.HORIZONTAL);
        tools.getItems().add(deleteButton);
        setTop(tools);
        
        update();
    }
    
    void update() {
        table.setItems(db.getAllSleepPeriods());
    }
}