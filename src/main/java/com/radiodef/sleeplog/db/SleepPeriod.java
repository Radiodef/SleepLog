package com.radiodef.sleeplog.db;

import java.time.*;
import java.util.*;

import org.apache.commons.lang3.builder.*;

public final class SleepPeriod {
    private final int id;
    private final Instant start;
    private final Instant end;
    
    public SleepPeriod(int id, Instant start, Instant end) {
        this.id = id;
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
    }
    
    public int getID() {
        return id;
    }
    
    public Instant getStart() {
        return start;
    }
    
    public Instant getEnd() {
        return end;
    }
    
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}