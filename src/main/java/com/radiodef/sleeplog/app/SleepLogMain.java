package com.radiodef.sleeplog.app;

final class SleepLogMain {
    private SleepLogMain() {
    }
    
    public static void main(String[] args) {
        // https://github.com/javafxports/openjdk-jfx/issues/236#issuecomment-426583174
        SleepLog.main(args);
    }
}