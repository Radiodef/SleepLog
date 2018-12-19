package com.radiodef.sleeplog.app;

import java.time.*;
import java.util.function.*;

@FunctionalInterface
interface InstantBiConsumer extends BiConsumer<Instant, Instant> {
}
