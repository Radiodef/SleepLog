package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;

import java.util.*;

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

class SleepPeriodDialog extends Stage {
    private SleepPeriod period;
    
    private SleepPeriodDialog(Stage owner) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(Objects.requireNonNull(owner));
        setResizable(false);
        setTitle("Add New Sleep Period");
        
        var startTitledPane = new TitledPane("Start Date/Time", new DateTimeEntryPane());
        var endTitledPane = new TitledPane("End Date/Time", new DateTimeEntryPane());
        
        List.of(startTitledPane, endTitledPane)
            .forEach(p -> p.getStyleClass().add("no-collapse-titled-pane"));
        
        var dates = new VBox(startTitledPane, endTitledPane);
        dates.setId("period-dialog-vbox");
        dates.getStyleClass().add("dialog-content");
        
        var cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> cancel());
        
        var doneButton = new Button("Done");
        
        var buttons = new HBox(cancelButton, doneButton);
        buttons.getStyleClass().add("dialog-buttons");
        
        var content = new BorderPane();
        content.setCenter(dates);
        content.setBottom(buttons);
        
        var scene = new Scene(content);
        scene.getStylesheets().add("styles.css");
        setScene(scene);
    }
    
    private void cancel() {
        period = null;
        hide();
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