package com.radiodef.sleeplog.util;

import com.radiodef.sleeplog.app.*;

import javafx.scene.layout.*;
import javafx.collections.*;

public class PseudoTable<R> extends BorderPane {
    private final ObservableList<Column<R>> columns = FXCollections.observableArrayList();
    
    public PseudoTable() {
        columns.addListener(Tools.listChangeListener(c -> layoutComponents()));
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
    }
    
    public static class Column<R> {
    }
}