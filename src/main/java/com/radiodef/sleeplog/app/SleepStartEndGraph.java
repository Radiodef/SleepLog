package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;

import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.collections.*;

import java.util.*;

final class SleepStartEndGraph extends BorderPane {
    private final Database db;
    
    private final LineChart<Number, Number> chart;
    
    SleepStartEndGraph(Database db) {
        this.db = Objects.requireNonNull(db, "db");
        
        this.chart = new LineChart<>(GraphsPane.createDayAxis(), GraphsPane.createTimeAxis());
        
        setCenter(chart);
        
        update();
        db.getAllSleepPeriods().addListener((ListChangeListener<SleepPeriod>) (c -> update()));
    }
    
    private void update() {
        Log.enter();
    }
}