package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;

import javafx.scene.layout.*;

import java.util.*;

final class SleepStartEndGraph extends BorderPane {
    private final Database db;
    
    SleepStartEndGraph(Database db) {
        this.db = Objects.requireNonNull(db, "db");
    }
}
