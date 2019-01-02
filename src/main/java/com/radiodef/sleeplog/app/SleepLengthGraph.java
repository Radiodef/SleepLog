package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;
import com.radiodef.sleeplog.util.*;

import javafx.scene.chart.*;
import java.util.*;

final class SleepLengthGraph extends AreaChart<Number, Number> {
    private final Database db;
    
    SleepLengthGraph(Database db) {
        super(new NumberAxis(), new NumberAxis());
        
        this.db = Objects.requireNonNull(db, "db");
        update();
    }
    
    void update() {
        var periods = db.getAllSleepPeriods();
        
        var series = new Series<Number, Number>();
        
        setData(Tools.observableArrayList(series));
    }
}
