package com.radiodef.sleeplog.util;

import javafx.stage.*;
import javafx.beans.value.*;

import java.util.*;
import com.google.common.collect.*;

// failed experiment
@SuppressWarnings("unused")
public enum StageToFrontListener implements ChangeListener<Boolean> {
    INSTANCE;
    
    @Override
    public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
        if (n) {
            var windows = List.copyOf(Lists.reverse(Window.getWindows()));
            
            for (var w : windows) {
                if (w instanceof Stage) {
                    w.focusedProperty().removeListener(this);
                    try {
                        ((Stage) w).toFront();
                    } finally {
                        w.focusedProperty().addListener(this);
                    }
                }
            }
        }
    }
    
    public static Stage add(Stage s) {
        s.focusedProperty().addListener(INSTANCE);
        return s;
    }
}
