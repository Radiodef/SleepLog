package com.radiodef.sleeplog.app;

import javafx.scene.layout.*;
import javafx.scene.control.*;

final class DateTimeEntryPane extends HBox {
    private final TextField monthField;
    private final TextField dayField;
    private final TextField yearField;
    
    private final TextField hourField;
    private final TextField minuteField;
    
    DateTimeEntryPane() {
        monthField = new TextField();
        dayField = new TextField();
        yearField = new TextField();
        hourField = new TextField();
        minuteField = new TextField();
        
        monthField.setPrefColumnCount(2);
        dayField.setPrefColumnCount(2);
        yearField.setPrefColumnCount(4);
        hourField.setPrefColumnCount(2);
        minuteField.setPrefColumnCount(2);
        
        getChildren().addAll(monthField, dayField, yearField, hourField, minuteField);
    }
}