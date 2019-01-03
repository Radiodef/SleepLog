package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;
import com.radiodef.sleeplog.util.*;

import javafx.collections.*;
import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.*;
import java.time.*;
import java.math.*;

final class SleepLengthGraph extends BorderPane {
    private static final int SECS_IN_HR = 60 * 60;
    private static final int SECS_IN_DAY = 60 * 60 * 24;
    
    private final Database db;
    
    private final AreaChart<Number, Number> chart;
    
    private final Label meanLabel;
    private final Label standardLabel;
    
    SleepLengthGraph(Database db) {
        this.db = Objects.requireNonNull(db, "db");
        
        this.meanLabel = new Label();
        setMean(BigInteger.ZERO);
        
        this.standardLabel = new Label("Standard Deviation: 0 hours");
        setStandardDeviation(BigInteger.ZERO);
        
        var bottom = new HBox();
        bottom.getStyleClass().add("graph-bottom-label-pane");
        bottom.getChildren().addAll(meanLabel, standardLabel);
        
        setBottom(bottom);
        
        this.chart = new AreaChart<>(createXAxis(), createYAxis());
        
        update();
        db.getAllSleepPeriods().addListener((ListChangeListener<SleepPeriod>) e -> update());
        
        setCenter(chart);
        
//        var series = createSeries();
//        setData(Tools.observableArrayList(series));
//
//        setBounds();
    }
    
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
    
    private void update() {
        Log.enter();
        var periods = db.getAllSleepPeriods();
        
        var durationSeries = new AreaChart.Series<Number, Number>();
        durationSeries.setName("Sleep Duration");
        
        var meanSeries = new AreaChart.Series<Number, Number>();
        meanSeries.setName("Mean");
        
        var meanDuration = BigInteger.ONE;
        
        var minDate = Long.MAX_VALUE;
        var maxDate = Long.MIN_VALUE;
        
        var minSeconds = Long.MAX_VALUE;
        var maxSeconds = Long.MIN_VALUE;
        
        for (var p : periods) {
            var date = Tools.toStartOfDay(p.getStart()).getEpochSecond();
            var duration = Duration.between(p.getStart(), p.getEnd()).toSeconds();
            
            meanDuration = meanDuration.add(BigInteger.valueOf(duration));
            
            minDate = Math.min(minDate, date);
            maxDate = Math.max(maxDate, date);
            
            minSeconds = Math.min(minSeconds, duration);
            maxSeconds = Math.max(maxSeconds, duration);
            
            durationSeries.getData().add(new AreaChart.Data<>(date, duration));
        }
        
        if (!periods.isEmpty())
            meanDuration = meanDuration.divide(BigInteger.valueOf(periods.size()));
        setMean(meanDuration);
        
        var variance = BigInteger.ZERO;
        
        var i = 0;
        for (var p : periods) {
            var duration = Duration.between(p.getStart(), p.getEnd()).toSeconds();
            var difference = meanDuration.subtract(BigInteger.valueOf(duration));
            
            variance = variance.add(difference.multiply(difference));
            
            var date = durationSeries.getData().get( i++ ).getXValue();
            meanSeries.getData().add(new AreaChart.Data<>(date, meanDuration));
        }
        
        if (!periods.isEmpty())
            variance = variance.divide(BigInteger.valueOf(periods.size()));
        setStandardDeviation(variance.sqrt());
        
        var xAxis = (NumberAxis) chart.getXAxis();
        
        xAxis.setLowerBound(minDate);
        xAxis.setUpperBound(maxDate);
        
        Log.notef("x axis: %s to %s", Tools.formatDate(Instant.ofEpochSecond(minDate)),
                                      Tools.formatDate(Instant.ofEpochSecond(maxDate)));
        
        var yAxis = (NumberAxis) chart.getYAxis();
        
        var yLowerBound = 0.0; // Math.floor(minSeconds / (double) SECS_IN_HR);
        var yUpperBound = Math.ceil(maxSeconds / (double) SECS_IN_HR);
        yAxis.setLowerBound(yLowerBound * SECS_IN_HR);
        yAxis.setUpperBound(yUpperBound * SECS_IN_HR);
        
        Log.notef("y axis: %f hours to %f hours", yLowerBound, yUpperBound);
        
        chart.setData(Tools.observableArrayList(durationSeries, meanSeries));
    }
    
    private void setMean(BigInteger seconds) {
        setHours(meanLabel, "Mean", seconds);
    }
    
    private void setStandardDeviation(BigInteger seconds) {
        setHours(standardLabel, "Standard Deviation", seconds);
    }
    
    private static void setHours(Label label, String prefix, BigInteger seconds) {
        var hours =
            new BigDecimal(seconds)
                .divide(BigDecimal.valueOf(SECS_IN_HR), 1, RoundingMode.HALF_UP);
        
        var suffix = BigDecimal.ONE.equals(hours) ? " hour" : " hours";
        
        label.setText(prefix + ": " + hours + suffix);
    }
    
    /*
    private AreaChart.Series<Number, Number> createSeries() {
        Log.enter();
        var series = new AreaChart.Series<Number, Number>();
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
        
        for (var data : chart.getData().get(0).getData()) {
            var date = data.getXValue().longValue();
            var duration = data.getYValue().longValue();
            
            minDate = Math.min(minDate, date);
            maxDate = Math.max(maxDate, date);
            
            minSeconds = Math.min(minSeconds, duration);
            maxSeconds = Math.max(maxSeconds, duration);
        }
        
        var xAxis = (NumberAxis) chart.getXAxis();
        
        xAxis.setLowerBound(minDate);
        xAxis.setUpperBound(maxDate);
        
        Log.notef("x axis: %s to %s", Tools.formatDate(Instant.ofEpochSecond(minDate)),
                                      Tools.formatDate(Instant.ofEpochSecond(maxDate)));
        
        var yAxis = (NumberAxis) chart.getYAxis();
        
        var yLowerBound = 0.0; // Math.floor(minSeconds / (double) SECS_IN_HR);
        var yUpperBound = Math.ceil(maxSeconds / (double) SECS_IN_HR);
        yAxis.setLowerBound(yLowerBound * SECS_IN_HR);
        yAxis.setUpperBound(yUpperBound * SECS_IN_HR);
        
        Log.notef("y axis: %f hours to %f hours", yLowerBound, yUpperBound);
    }
    
    private enum DataFunction implements Function<SleepPeriod, AreaChart.Data<Number, Number>> {
        INSTANCE;
        
        @Override
        public AreaChart.Data<Number, Number> apply(SleepPeriod period) {
            var start = period.getStart();
            var end = period.getEnd();
            
            var date = Tools.toStartOfDay(start).getEpochSecond();
            var duration = Duration.between(start, end).toSeconds();
            
            return new AreaChart.Data<>(date, duration);
        }
    }
    */
}
