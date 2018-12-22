package com.radiodef.sleeplog.app;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.beans.property.*;

import java.util.*;
import java.time.*;

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
    }
    
    ObjectProperty<LocalDateTime> dateTimeProperty() {
        return dateTime;
    }
}