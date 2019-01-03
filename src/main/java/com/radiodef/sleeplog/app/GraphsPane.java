package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.db.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;

final class GraphsPane extends BorderPane {
    GraphsPane(Database db) {
        var tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        var lengthTab = new Tab("Duration");
        lengthTab.setContent(new SleepLengthGraph(db));
        
        var startEndTab = new Tab("Start & End");
        startEndTab.setContent(new SleepStartEndGraph(db));
        
        tabs.getTabs().addAll(startEndTab, lengthTab);
        setCenter(tabs);
    }
}
