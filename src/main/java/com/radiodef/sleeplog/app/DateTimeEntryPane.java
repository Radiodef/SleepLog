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
        
        getChildren().addAll(monthField, dayField, yearField, hourField, minuteField);
    }
}