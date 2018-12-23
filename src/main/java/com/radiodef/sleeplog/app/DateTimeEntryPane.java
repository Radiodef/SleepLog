package com.radiodef.sleeplog.app;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.beans.property.*;

import java.time.*;
import java.util.*;

import org.apache.commons.lang3.math.*;

final class DateTimeEntryPane extends HBox {
    private final TextField monthField;
    private final TextField dayField;
    private final TextField yearField;
    
    private final TextField hourField;
    private final TextField minuteField;
    
    private final ObjectProperty<LocalDateTime> dateTime = new SimpleObjectProperty<>();
    
    DateTimeEntryPane() {
        getStyleClass().add("date-time-entry-pane");
        
        monthField = new TextField();
        dayField = new TextField();
        yearField = new TextField();
        hourField = new TextField();
        minuteField = new TextField();
        
        monthField.setId("month-field");
        dayField.setId("day-field");
        yearField.setId("year-field");
        hourField.setId("hour-field");
        minuteField.setId("minute-field");
        
        monthField.setPrefColumnCount(2);
        dayField.setPrefColumnCount(2);
        yearField.setPrefColumnCount(4);
        hourField.setPrefColumnCount(2);
        minuteField.setPrefColumnCount(2);
        
        monthField.setPromptText("MM");
        dayField.setPromptText("DD");
        yearField.setPromptText("YYYY");
        hourField.setPromptText("hh");
        minuteField.setPromptText("mm");
        
        var date = new HBox(monthField, dayField, yearField);
        var time = new HBox(hourField, minuteField);
        
        List.of(date, time)
            .forEach(p -> p.getStyleClass().add("date-time-entry-pane-group"));
        
        getChildren().addAll(date, time);
        
        fields().forEach(f -> f.textProperty().addListener((a, b, c) -> setDateTime()));
    }
    
    LocalDateTime getDateTime() {
        return dateTime.get();
    }
    
    ReadOnlyObjectProperty<LocalDateTime> dateTimeProperty() {
        return dateTime;
    }
    
    private List<TextField> fields() {
        return List.of(monthField,
                       dayField,
                       yearField,
                       hourField,
                       minuteField);
    }
    
    private void setDateTime() {
        var fields = fields();
        
        int[] vals =
            fields.stream()
                .map(TextField::getText)
                .mapToLong(text -> NumberUtils.toLong(text, Long.MIN_VALUE))
                .filter(val -> (Integer.MIN_VALUE <= val) && (val <= Integer.MAX_VALUE))
                .mapToInt(val -> (int) val)
                .toArray();
        
        if (vals.length != fields.size()) {
            dateTime.set(null);
            return;
        }
        
        try {
            dateTime.set(LocalDateTime.of(vals[2], vals[0], vals[1], vals[3], vals[4]));
        } catch (DateTimeException x) {
            Log.caught(x);
        }
    }
}