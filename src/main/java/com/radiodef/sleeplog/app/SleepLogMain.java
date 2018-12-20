package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;

import java.util.*;
import java.nio.file.*;

import org.apache.commons.lang3.*;

final class SleepLogMain {
    private SleepLogMain() {
    }
    
    private static volatile List<String> ARGS;
    
    static List<String> getArguments() {
        return Tools.requireNonNullState(ARGS, "arguments");
    }
    
    public static void main(String[] args) {
        ARGS = List.of(args);
        Log.notef("args = %s", getArguments());
        
        Log.notef("OS name = %s", SystemUtils.OS_NAME);
        Log.notef("working directory = %s", Paths.get("").toAbsolutePath());
        
        // https://github.com/javafxports/openjdk-jfx/issues/236#issuecomment-426583174
        SleepLogApp.launch();
    }
}