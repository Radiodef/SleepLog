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
import java.util.stream.*;
import java.time.*;
import java.time.Duration;

import static java.util.Collections.*;

class DatabaseTablePane extends BorderPane {
    private static final String ID = "db-table-pane";
    private static final String DELETE_PERIOD_ID = "delete-period-button";
    private static final String ADD_NOTE_ID = "add-note-button";
    
    private final Database db;
    private final TableView<SleepPeriod> table;
    private final TableView<Note> notes;
    
    DatabaseTablePane(Database db) {
        this.db = Objects.requireNonNull(db, "db");
        setId(ID);
        
        table = createPeriodsTable();
        var periodsTools = createPeriodsToolBar();
        var top = new BorderPane();
        top.setTop(periodsTools);
        top.setCenter(table);
        
        notes = createNotesTable();
        var notesTools = createNotesToolBar();
        var bot = new BorderPane();
        bot.setTop(notesTools);
        bot.setCenter(notes);
        
        var split = new SplitPane(top, bot);
        split.setOrientation(Orientation.VERTICAL);
        setCenter(split);
        
        split.setDividerPosition(0, 0.25);
        Platform.runLater(() -> table.scrollTo(table.getItems().size() - 1));
    }
    
    private TableView<SleepPeriod> createPeriodsTable() {
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
        
        var table = new TableView<SleepPeriod>();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setEditable(false);
        
        addAll(table.getColumns(), idCol, startCol, endCol, durationCol, manualCol);
        
        var periods = new SortedList<>(db.getAllSleepPeriods());
        periods.comparatorProperty().bind(table.comparatorProperty());
        
        table.setItems(periods);
        table.getSortOrder().add(startCol);
        
        table.getSelectionModel()
             .getSelectedIndices()
             .addListener(this::selectionChanged);
        table.getSelectionModel()
             .setSelectionMode(SelectionMode.MULTIPLE);
        
        return table;
    }
    
    private ToolBar createPeriodsToolBar() {
        var deleteButton = new Button("Delete");
        deleteButton.setId(DELETE_PERIOD_ID);
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> deleteSelection());
        
        var addButton = new Button("Add New");
        addButton.setOnAction(e -> addNewRow());
        
        var bar = new ToolBar();
        bar.setOrientation(Orientation.HORIZONTAL);
        bar.getItems().addAll(addButton, deleteButton);
        return bar;
    }
    
    private TableView<Note> createNotesTable() {
        var idCol = new TableColumn<Note, Integer>("ID");
        var dateIdCol = new TableColumn<Note, Integer>("Period ID");
        var textCol = new TableColumn<Note, String>("Note Text");
        
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateIdCol.setCellValueFactory(new PropertyValueFactory<>("dateId"));
        textCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        
        var table = new TableView<Note>();
        addAll(table.getColumns(), idCol, dateIdCol, textCol);
        
        table.getSortOrder().add(dateIdCol);
        
        db.getAllNotes().addListener(Tools.listChangeListener(c -> fillNotesTable()));
        return table;
    }
    
    private ToolBar createNotesToolBar() {
        var addButton = new Button("Add Note");
        addButton.setId(ADD_NOTE_ID);
        addButton.setDisable(true);
        addButton.setOnAction(e -> addNewNote());
        
        var bar = new ToolBar();
        bar.setOrientation(Orientation.HORIZONTAL);
        bar.getItems().addAll(addButton);
        return bar;
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
    
    private void selectionChanged(ListChangeListener.Change<? extends Integer> c) {
        var noSelection = c.getList().isEmpty();
        
        Stream.of(getScene().lookup("#" + DELETE_PERIOD_ID),
                  getScene().lookup("#" + ADD_NOTE_ID))
            .filter(Objects::nonNull)
            .forEach(btn -> btn.setDisable(noSelection));
        
        fillNotesTable();
    }
    
    private void fillNotesTable() {
        ObservableList<Note> notes = FXCollections.observableArrayList();
        
        for (var item : table.getSelectionModel().getSelectedItems()) {
            notes.addAll(db.getNotesForPeriodId(item.getID()));
        }
        
        var sort = List.copyOf(this.notes.getSortOrder());
        this.notes.setItems(notes);
        this.notes.getSortOrder().setAll(sort);
    }
    
    private void addNewRow() {
        Log.enter();
        
        SleepPeriodDialog.show(this).ifPresent(period -> {
            Log.note(period);
            db.insertNewPeriod(period.getStart(), period.getEnd(), true);
        });
    }
    
    private void addNewNote() {
        Log.enter();
        
        var dialog = new TextInputDialog();
        dialog.initOwner(getScene().getWindow());
        dialog.showAndWait();
        
        var text = dialog.getResult();
        if (text != null) {
            Log.note("Note text = " + text);
            for (var period : table.getSelectionModel().getSelectedItems()) {
                db.insertNewNote(period.getID(), text);
            }
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