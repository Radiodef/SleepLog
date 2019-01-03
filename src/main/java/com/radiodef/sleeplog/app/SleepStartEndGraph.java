package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;
import com.radiodef.sleeplog.db.*;

import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.*;

import java.util.*;
import java.time.*;

final class SleepStartEndGraph extends BorderPane {
    private final Database db;
    
    private final LineChart<Number, Number> chart;
    
    SleepStartEndGraph(Database db) {
        this.db = Objects.requireNonNull(db, "db");
        
        this.chart = new LineChart<>(GraphsPane.createDayAxis(), GraphsPane.createTimeAxis());
        
        setCenter(chart);
        
        update();
        db.getAllSleepPeriods().addListener(Tools.listChangeListener(c -> update()));
    }
    
    private void update() {
        Log.enter();
        var periods = db.getAllSleepPeriods();
        
        var startSeries = new Series<Number, Number>();
        var endSeries = new Series<Number, Number>();
        
        startSeries.setName("Start Time");
        endSeries.setName("End Time");
        
        for (var p : periods) {
            startSeries.getData().add(createData(p.getStart()));
            endSeries.getData().add(createData(p.getEnd()));
        }
        
        chart.setData(Tools.observableArrayList(startSeries, endSeries));
        
        if (!periods.isEmpty()) {
            var xAxis = (NumberAxis) chart.getXAxis();
            
            var first = startSeries.getData().get(0);
            var last = endSeries.getData().get(periods.size() - 1);
            
            xAxis.setLowerBound(first.getXValue().doubleValue());
            xAxis.setUpperBound(last.getXValue().doubleValue());
        }
    }
    
    private static long getStartOfDay(Instant i) {
        return Tools.toStartOfDay(i).getEpochSecond();
    }
    
    private static int getSecondOfDay(Instant i) {
        return LocalTime.ofInstant(i, ZoneId.systemDefault()).toSecondOfDay();
    }
    
    private static Data<Number, Number> createData(Instant i) {
        return new Data<>(getStartOfDay(i), getSecondOfDay(i));
    }
}