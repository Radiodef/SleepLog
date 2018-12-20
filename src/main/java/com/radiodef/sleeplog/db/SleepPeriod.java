package com.radiodef.sleeplog.db;

import java.time.*;
import java.util.*;

import javafx.beans.property.*;

import org.apache.commons.lang3.builder.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class SleepPeriod {
    private final ReadOnlyIntegerProperty id;
    private final ReadOnlyObjectProperty<Instant> start;
    private final ReadOnlyObjectProperty<Instant> end;
    
    public SleepPeriod(int id, Instant start, Instant end) {
        this.id = new SimpleIntegerProperty(this, "id", id);
        this.start = new SimpleObjectProperty<>(this, "start", Objects.requireNonNull(start, "start"));
        this.end = new SimpleObjectProperty<>(this, "end", Objects.requireNonNull(end, "end"));
    }
    
    public int getID() {
        return id.intValue();
    }
    
    public Instant getStart() {
        return start.get();
    }
    
    public Instant getEnd() {
        return end.get();
    }
    
    public ReadOnlyIntegerProperty idProperty() {
        return id;
    }
    
    public ReadOnlyObjectProperty<Instant> startProperty() {
        return start;
    }
    
    public ReadOnlyObjectProperty<Instant> endProperty() {
        return end;
    }
    
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}