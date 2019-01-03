package com.radiodef.sleeplog.util;

import com.radiodef.sleeplog.app.*;

import javafx.scene.layout.*;
import javafx.collections.*;

public class PseudoTable extends BorderPane {
    private final ObservableList<Column> columns = FXCollections.observableArrayList();
    
    public PseudoTable() {
        columns.addListener(Tools.listChangeListener(c -> layoutComponents()));
    }
    
    public ObservableList<Column> getColumns() {
        return columns;
    }
    
    private void layoutComponents() {
        Log.enter();
    }
    
    public static class Column {
    }
}