package com.radiodef.sleeplog.app;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.application.*;

import java.util.*;
import java.io.*;

import org.apache.commons.lang3.*;

final class HardwareHistoryPane extends BorderPane {
    HardwareHistoryPane() {
        var text = new TextArea("Loading...");
        text.setEditable(false);
        setCenter(text);
        
        new Thread(() -> {
            final var lines = getHardwareHistory();
            Platform.runLater(() -> {
                var joined = String.join("\n", lines);
                text.setText(joined);
                
                var lastLine = joined.lastIndexOf("\n");
                if (lastLine >= 0) {
                    text.positionCaret(lastLine + 1);
                }
//                else {
//                    text.end();
//                }
//                text.setScrollTop(Double.MAX_VALUE);
            });
        }).start();
    }
    
    // https://apple.stackexchange.com/questions/52064/how-to-find-out-the-start-time-of-last-sleep
    private static final String PMSET_COMMAND = "pmset -g log";
    
    @SuppressWarnings("WeakerAccess")
    static List<String> getHardwareHistory() {
        var lines = new ArrayList<String>();
        
        try {
            var proc = Runtime.getRuntime().exec(PMSET_COMMAND);
            
            try (var in = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                in.lines()
                    .filter(ln -> !ln.contains("Assertions"))
                    .filter(ln ->
//                           ln.contains(" Sleep ")
//                        || ln.contains(" Wake ")
                           StringUtils.containsIgnoreCase(ln, "sleep")
                        || StringUtils.containsIgnoreCase(ln, "wake")
                        || StringUtils.containsIgnoreCase(ln, "display is turned")
//                        || true
                    )
                    .forEach(lines::add);
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