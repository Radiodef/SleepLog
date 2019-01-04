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
        var stdDevCol = stats.createColumn(Duration.class, "StandardDeviation", "stdDev", SleepStartEndGraph::formatStdDev);
        
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
        
        stats.getData().get(0).stdDevProperty().set(start.getStandardDeviation());
        stats.getData().get(1).stdDevProperty().set(end.getStandardDeviation());
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
        
        private BigInteger getMeanAsBigInteger() {
            return totalTime.divide(BigInteger.valueOf(series.getData().size()));
        }
        
        private LocalTime getMean() {
            return LocalTime.ofSecondOfDay(getMeanAsBigInteger().intValue());
        }
        
        private Duration getStandardDeviation() {
            var mean = getMeanAsBigInteger();
            var sum = BigInteger.ZERO;
            
            for (var data : series.getData()) {
                var time = BigInteger.valueOf(data.getYValue().longValue());
                var dif = time.subtract(mean);
                
                sum = sum.add(dif.multiply(dif));
            }
            
            if (!series.getData().isEmpty()) {
                sum = sum.divide(BigInteger.valueOf(series.getData().size()));
            }
            return Duration.ofSeconds(sum.sqrt().longValue());
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
        private final ObjectProperty<Duration> stdDev;
        
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
        
        public ObjectProperty<Duration> stdDevProperty() {
            return stdDev;
        }
    }
    
    private static String formatStdDev(Duration d) {
        return BigDecimal.valueOf(d.getSeconds())
            .divide(BigDecimal.valueOf(GraphsPane.SECS_PER_HR), 1, RoundingMode.HALF_EVEN)
            .toString();
    }
}