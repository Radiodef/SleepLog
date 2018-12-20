package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;
import com.radiodef.sleeplog.util.*;

import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.util.*;

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
        
        startCol.setCellFactory(InstantStringConverter.CELL_FACTORY);
        endCol.setCellFactory(InstantStringConverter.CELL_FACTORY);
        
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
//        idCol.prefWidthProperty().bind(table.widthProperty().divide(5));
//        startCol.prefWidthProperty().bind(table.widthProperty().multiply(2).divide(5));
//        endCol.prefWidthProperty().bind(table.widthProperty().multiply(2).divide(5));
        
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
    
    private static final class InstantStringConverter extends StringConverter<Instant> {
        private static final Callback<TableColumn<SleepPeriod, Instant>, TableCell<SleepPeriod, Instant>> CELL_FACTORY =
            TextFieldTableCell.forTableColumn(new InstantStringConverter());
        
        @Override
        public String toString(Instant inst) {
            return (inst == null) ? "null" : Tools.formatDetailedInstant(inst);
        }
        
        @Override
        public Instant fromString(String str) {
            throw new AssertionError(str);
        }
    }
}