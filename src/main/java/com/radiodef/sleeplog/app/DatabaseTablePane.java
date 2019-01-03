package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;
import com.radiodef.sleeplog.util.*;

import org.apache.commons.lang3.*;
import org.apache.commons.lang3.time.*;

import javafx.application.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.util.*;
import javafx.collections.*;
import javafx.collections.transformation.*;

import java.util.*;
import java.time.*;
import java.time.Duration;

class DatabaseTablePane extends BorderPane {
    private static final String ID = "db-table-pane";
    
    private final Database db;
    private final TableView<SleepPeriod> table;
    
    DatabaseTablePane(Database db) {
        this.db = Objects.requireNonNull(db, "db");
        
        setId(ID);
        
        var idCol = new TableColumn<SleepPeriod, Integer>("ID");
        var startCol = new TableColumn<SleepPeriod, Instant>("Start");
        var endCol = new TableColumn<SleepPeriod, Instant>("End");
        var durationCol = new TableColumn<SleepPeriod, Duration>("Duration");
        var manualCol = new TableColumn<SleepPeriod, Boolean>("Manual Entry");
        
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        startCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("end"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        manualCol.setCellValueFactory(new PropertyValueFactory<>("manualEntry"));
        
        startCol.setCellFactory(InstantStringConverter.CELL_FACTORY);
        endCol.setCellFactory(InstantStringConverter.CELL_FACTORY);
        durationCol.setCellFactory(DurationStringConverter.CELL_FACTORY);
        
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
//        idCol.prefWidthProperty().bind(table.widthProperty().divide(5));
//        startCol.prefWidthProperty().bind(table.widthProperty().multiply(2).divide(5));
//        endCol.prefWidthProperty().bind(table.widthProperty().multiply(2).divide(5));
        
        table.setEditable(false);
        
        Collections.addAll(table.getColumns(), idCol, startCol, endCol, durationCol, manualCol);
        setCenter(table);
        
        var deleteButton = new Button("Delete");
        deleteButton.setDisable(true);
        
        table.getSelectionModel()
             .getSelectedIndices()
             .addListener((ListChangeListener<Integer>) c -> deleteButton.setDisable(c.getList().isEmpty()));
        table.getSelectionModel()
             .setSelectionMode(SelectionMode.MULTIPLE);
        
        deleteButton.setOnAction(e -> deleteSelection());
        
        var addButton = new Button("Add New");
        
        addButton.setOnAction(e -> addNewRow());
        
        var tools = new ToolBar();
        tools.setOrientation(Orientation.HORIZONTAL);
        tools.getItems().addAll(addButton, deleteButton);
        setTop(tools);
        
        var periods = new SortedList<>(db.getAllSleepPeriods());
        periods.comparatorProperty().bind(table.comparatorProperty());
        
        table.setItems(periods);
        table.getSortOrder().add(startCol);
        
        Platform.runLater(() -> table.scrollTo(periods.size() - 1));
    }
    
    private void deleteSelection() {
        Log.enter();
        
        var items = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        int count = 0;
        
        for (var item : items) {
            count += BooleanUtils.toInteger(db.deletePeriod(item.getID()));
        }
        
        Log.notef("deleted %d items", count);
    }
    
    private void addNewRow() {
        Log.enter();
        
        SleepPeriodDialog.show(this).ifPresent(period -> {
            Log.note(period);
            db.insertNewPeriod(period.getStart(), period.getEnd(), true);
        });
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
    
    private static final class DurationStringConverter extends StringConverter<Duration> {
        private static final Callback<TableColumn<SleepPeriod, Duration>, TableCell<SleepPeriod, Duration>> CELL_FACTORY =
            TextFieldTableCell.forTableColumn(new DurationStringConverter());
        
        @Override
        public String toString(Duration dur) {
            return (dur == null) ? "null" : DurationFormatUtils.formatDuration(dur.toMillis(), "H:mm");
        }
        
        @Override
        public Duration fromString(String str) {
            throw new AssertionError(str);
        }
    }
}