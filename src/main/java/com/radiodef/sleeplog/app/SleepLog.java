package com.radiodef.sleeplog.app;

import javafx.application.*;
import javafx.stage.*;

public final class SleepLog extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        setUserAgentStylesheet(STYLESHEET_MODENA);
        
        primaryStage.show();
    }
}