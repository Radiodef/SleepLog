package com.radiodef.sleeplog.app;

import javafx.application.*;
import javafx.stage.*;

public final class SleepLogApp extends Application {
    static void launch() {
        Log.enter();
        launch(SleepLogApp.class, SleepLogMain.getArguments().toArray(new String[0]));
    }
    
    @Override
    public void start(Stage primaryStage) {
        setUserAgentStylesheet(STYLESHEET_MODENA);
        
        primaryStage.show();
    }
}