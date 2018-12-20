package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;

import java.time.*;
import java.util.*;

class DatabaseTablePane extends BorderPane {
    static final String ID = "db-table-pane";
    
    private final Database db;
    private final TableView<SleepPeriod> table;
    
    DatabaseTablePane(Database db) {
        this.db = Objects.requireNonNull(db, "db");
        
        setId(ID);
        
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
        
        deleteButton.setOnAction(e -> deleteSelection());
        
        var tools = new ToolBar();
        tools.setOrientation(Orientation.HORIZONTAL);
        tools.getItems().add(deleteButton);
        setTop(tools);
        
        update();
    }
    
    void update() {
        table.setItems(db.getAllSleepPeriods());
    }
    
    private void deleteSelection() {
        var index = table.getSelectionModel().getSelectedIndex();
        assert index >= 0 : index;
        
        var items = table.getItems();
        var item = items.remove(index);
        
        if (db.deletePeriod(item.getID())) {
            Log.note("deletion succeeded");
        } else {
            Log.note("deletion failed");
        }
    }
}