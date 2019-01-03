package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;
import com.radiodef.sleeplog.db.*;

import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.util.*;

import java.time.*;
import java.time.Duration;

@SuppressWarnings("WeakerAccess")
final class GraphsPane extends BorderPane {
    GraphsPane(Database db) {
        var tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        var lengthTab = new Tab("Duration");
        lengthTab.setContent(new SleepLengthGraph(db));
        
        var startEndTab = new Tab("Start & End");
        startEndTab.setContent(new SleepStartEndGraph(db));
        
        tabs.getTabs().addAll(startEndTab, lengthTab);
        setCenter(tabs);
    }
    
    static final int SECS_PER_HR = 60 * 60;
    static final int SECS_PER_DAY = 60 * 60 * 24;
    
    static NumberAxis createDayAxis() {
        var axis = new NumberAxis();
        axis.setAutoRanging(false);
        axis.setTickUnit(SECS_PER_DAY);
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
    
    static NumberAxis createDurationAxis() {
        var axis = new NumberAxis();
        axis.setAutoRanging(false);
        axis.setTickUnit(SECS_PER_HR);
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
    
    static NumberAxis createTimeAxis() {
        var axis = new NumberAxis();
        axis.setAutoRanging(false);
        axis.setTickUnit(SECS_PER_HR);
        axis.setMinorTickCount(2);
        axis.setLowerBound(0);
        axis.setUpperBound(24 * SECS_PER_HR - 1);
        axis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number n) {
                return Tools.formatTimeOfDay(LocalTime.ofSecondOfDay(n.longValue()));
            }
            @Override
            public Number fromString(String s) {
                throw new AssertionError(s);
            }
        });
        return axis;
    }
}
