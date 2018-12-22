package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;

import java.util.*;
import java.time.*;

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

class SleepPeriodDialog extends Stage {
    private SleepPeriodDialog(Stage owner) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(Objects.requireNonNull(owner));
        setResizable(false);
        
        var dates = new VBox();
        dates.getStyleClass().add("dialog-content");
        dates.getChildren().addAll(new DateTimeEntryPane(), new DateTimeEntryPane());
        
        var cancelButton = new Button("Cancel");
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
    
    static SleepPeriod show(Node owner) {
        return show((Stage) owner.getScene().getWindow());
    }
    
    static SleepPeriod show(Stage owner) {
        var diag = new SleepPeriodDialog(owner);
        
        diag.showAndWait();
        Log.note("done");
        
        return new SleepPeriod(0, Instant.now(), Instant.now(), true);
    }
}