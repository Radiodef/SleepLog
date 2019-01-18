package com.radiodef.sleeplog.app;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.application.*;

import java.util.*;
import java.io.*;

final class HardwareHistoryPane extends BorderPane {
    HardwareHistoryPane() {
        var text = new TextArea();
        
        text.setText(String.join("\n", getHardwareHistory()));
        Platform.runLater(() -> text.setScrollTop(Double.MAX_VALUE));
        
        setCenter(text);
    }
    
    // https://apple.stackexchange.com/questions/52064/how-to-find-out-the-start-time-of-last-sleep
    private static final String PMSET_ALL_COMMAND = "pmset -g log";
    
    static List<String> getHardwareHistory() {
        var lines = new ArrayList<String>();
        
        try {
            var proc = Runtime.getRuntime().exec(PMSET_ALL_COMMAND);
            
            try (var in = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                in.lines().forEach(lines::add);
            }
            try (var err = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                for (String ln : (Iterable<String>) err.lines()::iterator) {
                    Log.note(ln);
                }
            }
            
            var result = proc.waitFor();
            Log.notef("process returned %d", result);
            
        } catch (IOException | InterruptedException x) {
            Log.caught(x);
        }
        
        return lines;
    }
}