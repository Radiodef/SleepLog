package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;
import com.radiodef.sleeplog.db.*;

import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.*;
import javafx.beans.property.*;

import java.util.*;
import java.time.*;
import java.math.*;

final class SleepStartEndGraph extends BorderPane {
    private final Database db;
    
    private final LineChart<Number, Number> chart;
    private final PseudoTable<StatsRow> stats;
    
    SleepStartEndGraph(Database db) {
        this.db = Objects.requireNonNull(db, "db");
        
        this.chart = new LineChart<>(GraphsPane.createDayAxis(), GraphsPane.createTimeAxis());
        setCenter(chart);
        
        this.stats = new PseudoTable<>(StatsRow.class);
        
        var nameCol = stats.createColumn(String.class, "Time", "name");
        var meanCol = stats.createColumn(LocalTime.class, "Mean", "mean", Tools::formatTimeOfDay);
        var stdDevCol = stats.createColumn(Double.class, "StandardDeviation", "stdDev");
        
        stats.addColumns(nameCol, meanCol, stdDevCol);
        stats.getData().addAll(new StatsRow("Start"), new StatsRow("End"));
        
        setBottom(stats);
        
        update();
        db.getAllSleepPeriods().addListener(Tools.listChangeListener(c -> update()));
    }
    
    private void update() {
        Log.enter();
        var periods = db.getAllSleepPeriods();
        
        var start = new SeriesBuilder("Start Time");
        var end = new SeriesBuilder("End Time");
        
        for (var p : periods) {
            start.add(p.getStart());
            end.add(p.getEnd());
        }
        
        chart.setData(Tools.observableArrayList(start.series, end.series));
        
        if (!periods.isEmpty()) {
            var xAxis = (NumberAxis) chart.getXAxis();
            
            var first = start.series.getData().get(0);
            var last = end.series.getData().get(periods.size() - 1);
            
            xAxis.setLowerBound(first.getXValue().doubleValue());
            xAxis.setUpperBound(last.getXValue().doubleValue());
        }
        
        stats.getData().get(0).meanProperty().set(start.getMean());
        stats.getData().get(1).meanProperty().set(end.getMean());
    }
    
    private static final class SeriesBuilder {
        private final Series<Number, Number> series = new Series<>();
        
        private BigInteger totalTime = BigInteger.ZERO;
        
        private SeriesBuilder(String name) {
            series.setName(name);
        }
        
        private void add(Instant i) {
            var date = getStartOfDay(i);
            var time = getSecondOfDay(i);
            
            series.getData().add(new Data<>(date, time));
            
            totalTime = totalTime.add(BigInteger.valueOf(time));
        }
        
        private LocalTime getMean() {
            var secs = totalTime.divide(BigInteger.valueOf(series.getData().size()));
            return LocalTime.ofSecondOfDay(secs.intValue());
        }
        
        private static long getStartOfDay(Instant i) {
            return Tools.toStartOfDay(i).getEpochSecond();
        }
        
        private static int getSecondOfDay(Instant i) {
            return LocalTime.ofInstant(i, ZoneId.systemDefault()).toSecondOfDay();
        }
    }
    
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static final class StatsRow {
        private final ObjectProperty<String> name;
        private final ObjectProperty<LocalTime> mean;
        private final ObjectProperty<Double> stdDev;
        
        private StatsRow(String name) {
            this.name = new SimpleObjectProperty<>(name);
            this.mean = new SimpleObjectProperty<>();
            this.stdDev = new SimpleObjectProperty<>();
        }
        
        public ObjectProperty<String> nameProperty() {
            return name;
        }
        
        public ObjectProperty<LocalTime> meanProperty() {
            return mean;
        }
        
        public ObjectProperty<Double> stdDevProperty() {
            return stdDev;
        }
    }
}