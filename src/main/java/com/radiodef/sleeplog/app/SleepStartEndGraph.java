package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;
import com.radiodef.sleeplog.db.*;

import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.*;
import javafx.beans.property.*;

import java.util.*;
import java.time.*;

final class SleepStartEndGraph extends BorderPane {
    private final Database db;
    
    private final LineChart<Number, Number> chart;
    private final PseudoTable<StatsRow> stats;
    
    SleepStartEndGraph(Database db) {
        this.db = Objects.requireNonNull(db, "db");
        
        this.chart = new LineChart<>(GraphsPane.createDayAxis(), GraphsPane.createTimeAxis());
        setCenter(chart);
        
        this.stats = new PseudoTable<>();
        
        var nameCol = new PseudoTable.Column<StatsRow>("Time", "name");
        var meanCol = new PseudoTable.Column<StatsRow>("Mean", "mean");
        var stdDevCol = new PseudoTable.Column<StatsRow>("StandardDeviation", "stdDev");
        
        stats.addColumns(nameCol, meanCol, stdDevCol);
        stats.getData().addAll(new StatsRow("Start"), new StatsRow("End"));
        
        setBottom(stats);
        
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
    
    @SuppressWarnings("unused")
    public static final class StatsRow {
        private final ObjectProperty<String> name;
        private final ObjectProperty<LocalTime> mean;
        private final DoubleProperty stdDev;
        
        private StatsRow(String name) {
            this.name = new SimpleObjectProperty<>(name);
            this.mean = new SimpleObjectProperty<>();
            this.stdDev = new SimpleDoubleProperty();
        }
        
        public ObjectProperty<String> nameProperty() {
            return name;
        }
        
        public ObjectProperty<LocalTime> meanProperty() {
            return mean;
        }
        
        public DoubleProperty stdDevProperty() {
            return stdDev;
        }
    }
}