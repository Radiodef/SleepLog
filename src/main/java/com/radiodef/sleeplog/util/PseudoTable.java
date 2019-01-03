package com.radiodef.sleeplog.util;

import com.radiodef.sleeplog.app.*;

import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.beans.property.*;
import javafx.beans.value.*;

import java.util.*;

@SuppressWarnings("unused")
public class PseudoTable<R> extends BorderPane {
    private static final String PSEUDO_TABLE = "pseudo-table";
    private static final String PANE_CLASS = PSEUDO_TABLE + "-pane";
    private static final String COLS_HBOX_CLASS = PSEUDO_TABLE + "-columns-hbox";
    private static final String COL_VBOX_CLASS = PSEUDO_TABLE + "-column-vbox";
    
    private final ObjectProperty<Class<R>> rowClass;
    
    private final ObservableList<Column<R>> columns = FXCollections.observableArrayList();
    private final ObservableList<R> data = FXCollections.observableArrayList();
    
    private final HBox columnsBox = new HBox();
    
    public PseudoTable(Class<R> rowClass) {
        this.rowClass = new SimpleObjectProperty<>(Objects.requireNonNull(rowClass, "rowClass"));
        
        getStyleClass().add(PANE_CLASS);
        columnsBox.getStyleClass().add(COLS_HBOX_CLASS);
        
        columns.addListener(Tools.listChangeListener(c -> layoutComponents()));
        data.addListener(Tools.listChangeListener(c -> layoutComponents()));
        
        setCenter(columnsBox);
    }
    
    public ObservableList<Column<R>> getColumns() {
        return columns;
    }
    
    @SafeVarargs
    public final void addColumns(Column<R>... columns) {
        this.columns.addAll(columns);
    }
    
    public ObservableList<R> getData() {
        return data;
    }
    
    private void layoutComponents() {
        Log.enter();
        columnsBox.getChildren().clear();
        
        for (var col : columns) {
            var box = col.node.get();
            columnsBox.getChildren().add(box);
        }
        
        for (var elem : data) {
        }
    }
    
    public static class Column<R> {
        private final ObjectProperty<String> label;
        private final ObjectProperty<String> property;
        private final ObjectProperty<VBox> node;
        
        private final IdentityHashMap<R, ChangeListener<?>> listeners;
        
        public Column(String label, String property) {
            this.label = new SimpleObjectProperty<>(label);
            
            this.property = new SimpleObjectProperty<>(property);
            
            var box = new VBox();
            box.getStyleClass().add(COL_VBOX_CLASS);
            
            this.node = new SimpleObjectProperty<>(box);
            
            this.listeners = new IdentityHashMap<>();
        }
        
        public ObjectProperty<String> labelProperty() {
            return label;
        }
        
        public ReadOnlyObjectProperty<VBox> nodeProperty() {
            return node;
        }
    }
}