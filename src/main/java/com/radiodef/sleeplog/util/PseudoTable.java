package com.radiodef.sleeplog.util;

import com.radiodef.sleeplog.app.*;

import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.beans.property.*;

@SuppressWarnings("unused")
public class PseudoTable<R> extends BorderPane {
    private static final String PSEUDO_TABLE = "pseudo-table";
    private static final String PANE_CLASS = PSEUDO_TABLE + "-pane";
    private static final String COL_BOX_CLASS = PSEUDO_TABLE + "-columns-box";
    
    private final ObservableList<Column<R>> columns = FXCollections.observableArrayList();
    
    private final HBox columnsBox = new HBox();
    
    public PseudoTable() {
        getStyleClass().add(PANE_CLASS);
        columnsBox.getStyleClass().add(COL_BOX_CLASS);
        
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
    }
    
    public static class Column<R> {
        private final ObjectProperty<String> name;
        
        public Column(String name) {
            this.name = new SimpleObjectProperty<>(name);
        }
        
        public ObjectProperty<String> nameProperty() {
            return name;
        }
    }
}