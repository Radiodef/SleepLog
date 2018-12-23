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
    
    private final RadioButton amButton;
    private final RadioButton pmButton;
    
    private final ObjectProperty<LocalDateTime> dateTime = new SimpleObjectProperty<>();
    
    DateTimeEntryPane() {
        getStyleClass().add("date-time-entry-pane");
        
        monthField = new TextField();
        dayField = new TextField();
        yearField = new TextField();
        hourField = new TextField();
        minuteField = new TextField();
        
        amButton = new RadioButton("AM");
        pmButton = new RadioButton("PM");
        
        var apGroup = new ToggleGroup();
        List.of(amButton, pmButton).forEach(b -> b.setToggleGroup(apGroup));
        amButton.setSelected(true);
        
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
        
        List.of(date, time, amButton, pmButton)
            .forEach(p -> p.getStyleClass().add("date-time-entry-pane-group"));
        
        getChildren().addAll(date, time, amButton, pmButton);
        
        fields().forEach(f -> f.textProperty().addListener((a, b, c) -> setDateTime()));
        List.of(amButton, pmButton).forEach(b -> b.selectedProperty().addListener((x, y, z) -> setDateTime()));
        
        setToNow();
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
        
        LocalDateTime val = null;
        
        if (vals.length == fields.size()) {
            if (amButton.isSelected()) {
                if (vals[3] == 12)
                    vals[3] = 0;
            } else {
                if (vals[3] > 12)
                    vals[3] -= 12;
            }
            
            try {
                val = LocalDateTime.of(vals[2], vals[0], vals[1], vals[3], vals[4]);
            } catch (DateTimeException x) {
                Log.note(x.getMessage());
//                Log.caught(x);
            }
        }
        
        dateTime.set(val);
    }
    
    private void setToNow() {
        dateTime.set(LocalDateTime.now());
        setTextFields();
    }
    
    private void setTextFields() {
        var dt = dateTime.get();
        
        if (dt == null) {
            fields().forEach(field -> field.setText(""));
        } else {
            monthField.setText(Integer.toString(dt.getMonthValue()));
            dayField.setText(Integer.toString(dt.getDayOfMonth()));
            yearField.setText(Integer.toString(dt.getYear()));
            
            var hour = dt.getHour();
            if (hour == 0) {
                hour = 12;
                amButton.setSelected(true);
            } else if (hour >= 12) {
                if (hour > 12)
                    hour -= 12;
                pmButton.setSelected(true);
            } else {
                amButton.setSelected(true);
            }
            
            hourField.setText(Integer.toString(hour));
            minuteField.setText(Integer.toString(dt.getMinute()));
        }
    }
}