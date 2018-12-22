package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;

import java.util.*;

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

class SleepPeriodDialog extends Stage {
    private final DateTimeEntryPane startPane;
    private final DateTimeEntryPane endPane;
    
    private final Button doneButton;
    
    private SleepPeriod period;
    
    private SleepPeriodDialog(Stage owner) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(Objects.requireNonNull(owner));
        setResizable(false);
        setTitle("Add New Sleep Period");
        
        startPane = new DateTimeEntryPane();
        endPane = new DateTimeEntryPane();
        
        var startTitledPane = new TitledPane("Start Date/Time", startPane);
        var endTitledPane = new TitledPane("End Date/Time", endPane);
        
        List.of(startTitledPane, endTitledPane)
            .forEach(p -> p.getStyleClass().add("no-collapse-titled-pane"));
        
        var dates = new VBox(startTitledPane, endTitledPane);
        dates.setId("period-dialog-vbox");
        dates.getStyleClass().add("dialog-content");
        
        var cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> cancel());
        
        doneButton = new Button("Done");
        doneButton.setDisable(true);
        startPane.dateTimeProperty().addListener((a, b, c) -> refreshDoneButton());
        endPane.dateTimeProperty().addListener((a, b, c) -> refreshDoneButton());
        doneButton.setOnAction(e -> done());
        
        var buttons = new HBox(cancelButton, doneButton);
        buttons.getStyleClass().add("dialog-buttons");
        
        var content = new BorderPane();
        content.setCenter(dates);
        content.setBottom(buttons);
        
        var scene = new Scene(content);
        scene.getStylesheets().add("styles.css");
        setScene(scene);
    }
    
    private void refreshDoneButton() {
        doneButton.setDisable((startPane.getDateTime() == null) || (endPane.getDateTime() == null));
    }
    
    private void cancel() {
        period = null;
        hide();
    }
    
    private void done() {
        var start = startPane.getDateTime();
        var end = endPane.getDateTime();
        if (start == null || end == null) {
            cancel();
        } else {
            // TODO: implement
        }
    }
    
    static Optional<SleepPeriod> show(Node owner) {
        return show((Stage) owner.getScene().getWindow());
    }
    
    static Optional<SleepPeriod> show(Stage owner) {
        var diag = new SleepPeriodDialog(owner);
        
        diag.showAndWait();
        Log.note("done");
        
        return Optional.ofNullable(diag.period);
    }
}