package com.radiodef.sleeplog.util;

import com.radiodef.sleeplog.app.*;

import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.beans.property.*;

@SuppressWarnings("unused")
public class PseudoTable<R> extends BorderPane {
    private static final String PSEUDO_TABLE = "pseudo-table";
    private static final String PANE_CLASS = PSEUDO_TABLE + "-pane";
    private static final String COLS_HBOX_CLASS = PSEUDO_TABLE + "-columns-hbox";
    private static final String COL_VBOX_CLASS = PSEUDO_TABLE + "-column-vbox";
    
    private final ObservableList<Column<R>> columns = FXCollections.observableArrayList();
    
    private final HBox columnsBox = new HBox();
    
    public PseudoTable() {
        getStyleClass().add(PANE_CLASS);
        columnsBox.getStyleClass().add(COLS_HBOX_CLASS);
        
        columns.addListener(Tools.listChangeListener(c -> layoutComponents()));
        
        setCenter(columnsBox);
    }
    
    public ObservableList<Column<R>> getColumns() {
        return columns;
    }
    
    @SafeVarargs
    public final void addColumns(Column<R>... columns) {
        this.columns.addAll(columns);
    }
    
    private void layoutComponents() {
        Log.enter();
        columnsBox.getChildren().clear();
        
        for (var col : columns) {
            var box = col.nodeProperty().get();
            
            if (box == null) {
                box = new VBox();
                box.getStyleClass().add(COL_VBOX_CLASS);
                col.nodeProperty().set(box);
            }
            
            columnsBox.getChildren().add(box);
        }
    }
    
    @SuppressWarnings("WeakerAccess")
    public static class Column<R> {
        private final ObjectProperty<String> name;
        private final ObjectProperty<VBox> node;
        
        public Column(String name) {
            this.name = new SimpleObjectProperty<>(name);
            this.node = new SimpleObjectProperty<>();
        }
        
        public ObjectProperty<String> nameProperty() {
            return name;
        }
        
        public ObjectProperty<VBox> nodeProperty() {
            return node;
        }
    }
}