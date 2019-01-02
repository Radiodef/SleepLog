package com.radiodef.sleeplog.app;

import javafx.scene.chart.*;

final class SleepLengthGraph extends AreaChart<Number, Number> {
    SleepLengthGraph() {
        super(new NumberAxis(), new NumberAxis());
    }
}
