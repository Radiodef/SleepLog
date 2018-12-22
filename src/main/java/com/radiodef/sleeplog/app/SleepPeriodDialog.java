package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;

import java.util.*;
import javafx.stage.*;
import javafx.scene.*;

class SleepPeriodDialog extends Stage {
    private SleepPeriodDialog(Stage owner) {
//        super(StageStyle.UTILITY);
        
        initModality(Modality.WINDOW_MODAL);
        initOwner(Objects.requireNonNull(owner));
        
        setResizable(false);
    }
    
    static SleepPeriod show(Node owner) {
        return show((Stage) owner.getScene().getWindow());
    }
    
    static SleepPeriod show(Stage owner) {
        var diag = new SleepPeriodDialog(owner);
        
        diag.showAndWait();
        Log.note("done");
        
        return null;
    }
}
