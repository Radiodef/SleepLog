package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;
import com.radiodef.sleeplog.util.*;

import javafx.collections.*;
import javafx.scene.chart.*;
import javafx.util.StringConverter;

import java.util.*;
import java.util.function.*;
import java.time.*;

final class SleepLengthGraph extends AreaChart<Number, Number> {
    private static final int SECS_IN_HR = 60 * 60;
    private static final int SECS_IN_DAY = 60 * 60 * 24;
    
    private final Database db;
    
    private static NumberAxis createXAxis() {
        var axis = new NumberAxis();
        axis.setAutoRanging(false);
        axis.setTickUnit(SECS_IN_DAY);
        axis.setMinorTickLength(0);
        axis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number n) {
                return Tools.formatDate(Instant.ofEpochSecond(n.longValue()));
            }
            @Override
            public Number fromString(String s) {
                throw new AssertionError(s);
            }
        });
        return axis;
    }
    
    private static NumberAxis createYAxis() {
        var axis = new NumberAxis();
        axis.setAutoRanging(false);
        axis.setTickUnit(SECS_IN_HR);
        axis.setMinorTickCount(2);
        axis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number n) {
                return Tools.formatDuration(Duration.ofSeconds(n.longValue()));
            }
            @Override
            public Number fromString(String s) {
                throw new AssertionError(s);
            }
        });
        return axis;
    }
    
    SleepLengthGraph(Database db) {
        super(createXAxis(), createYAxis());
        
        this.db = Objects.requireNonNull(db, "db");
        
        update();
        db.getAllSleepPeriods().addListener((ListChangeListener<SleepPeriod>) e -> update());
        
//        var series = createSeries();
//        setData(Tools.observableArrayList(series));
//
//        setBounds();
    }
    
    private Series<Number, Number> createSeries() {
        Log.enter();
        var series = new Series<Number, Number>();
        series.setName("Sleep Duration");
        
        var periods = db.getAllSleepPeriods();
        series.setData(new MappedList<>(periods, DataFunction.INSTANCE));
        
        return series;
    }
    
    private void setBounds() {
        Log.enter();
        
        var minDate = Long.MAX_VALUE;
        var maxDate = Long.MIN_VALUE;
        
        var minSeconds = Long.MAX_VALUE;
        var maxSeconds = Long.MIN_VALUE;
        
        for (var data : getData().get(0).getData()) {
            var date = data.getXValue().longValue();
            var duration = data.getYValue().longValue();
            
            minDate = Math.min(minDate, date);
            maxDate = Math.max(maxDate, date);
            
            minSeconds = Math.min(minSeconds, duration);
            maxSeconds = Math.max(maxSeconds, duration);
        }
        
        var xAxis = (NumberAxis) getXAxis();
        
        xAxis.setLowerBound(minDate);
        xAxis.setUpperBound(maxDate);
        
        Log.notef("x axis: %s to %s", Tools.formatDate(Instant.ofEpochSecond(minDate)),
                                      Tools.formatDate(Instant.ofEpochSecond(maxDate)));
        
        var yAxis = (NumberAxis) getYAxis();
        
        var yLowerBound = 0.0; // Math.floor(minSeconds / (double) SECS_IN_HR);
        var yUpperBound = Math.ceil(maxSeconds / (double) SECS_IN_HR);
        yAxis.setLowerBound(yLowerBound * SECS_IN_HR);
        yAxis.setUpperBound(yUpperBound * SECS_IN_HR);
        
        Log.notef("y axis: %f hours to %f hours", yLowerBound, yUpperBound);
    }
    
    private enum DataFunction implements Function<SleepPeriod, Data<Number, Number>> {
        INSTANCE;
        
        @Override
        public Data<Number, Number> apply(SleepPeriod period) {
            var start = period.getStart();
            var end = period.getEnd();
            
            var date = Tools.toStartOfDay(start).getEpochSecond();
            var duration = Duration.between(start, end).toSeconds();
            
            return new Data<>(date, duration);
        }
    }
    
    private void update() {
        Log.enter();
        var periods = db.getAllSleepPeriods();
        var series = new Series<Number, Number>();
        series.setName("Sleep Duration");
        
        var minDate = Long.MAX_VALUE;
        var maxDate = Long.MIN_VALUE;
        
        var minSeconds = Long.MAX_VALUE;
        var maxSeconds = Long.MIN_VALUE;
        
        for (var p : periods) {
            var date = Tools.toStartOfDay(p.getStart()).getEpochSecond();
            var duration = Duration.between(p.getStart(), p.getEnd()).toSeconds();
            
            minDate = Math.min(minDate, date);
            maxDate = Math.max(maxDate, date);
            
            minSeconds = Math.min(minSeconds, duration);
            maxSeconds = Math.max(maxSeconds, duration);
            
            series.getData().add(new Data<>(date, duration));
        }
        
        setData(Tools.observableArrayList(series));
        
        var xAxis = (NumberAxis) getXAxis();
        
        xAxis.setLowerBound(minDate);
        xAxis.setUpperBound(maxDate);
        
        Log.notef("x axis: %s to %s", Tools.formatDate(Instant.ofEpochSecond(minDate)),
                                      Tools.formatDate(Instant.ofEpochSecond(maxDate)));
        
        var yAxis = (NumberAxis) getYAxis();
        
        var yLowerBound = 0.0; // Math.floor(minSeconds / (double) SECS_IN_HR);
        var yUpperBound = Math.ceil(maxSeconds / (double) SECS_IN_HR);
        yAxis.setLowerBound(yLowerBound * SECS_IN_HR);
        yAxis.setUpperBound(yUpperBound * SECS_IN_HR);
        
        Log.notef("y axis: %f hours to %f hours", yLowerBound, yUpperBound);
    }
}
