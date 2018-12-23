package com.radiodef.sleeplog.db;

import com.radiodef.sleeplog.util.*;

import java.time.*;
import java.util.*;

import javafx.beans.property.*;

import org.apache.commons.lang3.builder.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class SleepPeriod {
    private final ReadOnlyIntegerProperty id;
    private final ReadOnlyObjectProperty<Instant> start;
    private final ReadOnlyObjectProperty<Instant> end;
    private final ReadOnlyBooleanProperty manualEntry;
    
    public SleepPeriod(int id, Instant start, Instant end) {
        this(id, start, end, false);
    }
    
    public SleepPeriod(int id, Instant start, Instant end, boolean manualEntry) {
        this.id = new SimpleIntegerProperty(this, "id", id);
        this.start = new SimpleObjectProperty<>(this, "start", Objects.requireNonNull(start, "start"));
        this.end = new SimpleObjectProperty<>(this, "end", Objects.requireNonNull(end, "end"));
        this.manualEntry = new SimpleBooleanProperty(this, "manualEntry", manualEntry);
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
    
    public boolean wasManualEntry() {
        return manualEntry.get();
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
    
    public ReadOnlyBooleanProperty manualEntryProperty() {
        return manualEntry;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getID())
            .append(getStart())
            .append(getEnd())
            .append(wasManualEntry())
            .toHashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof SleepPeriod) {
            var that = (SleepPeriod) o;
            return new EqualsBuilder()
                .append(this.getID(), that.getID())
                .append(this.getStart(), that.getStart())
                .append(this.getEnd(), that.getEnd())
                .append(this.wasManualEntry(), that.wasManualEntry())
                .isEquals();
        }
        return false;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getID())
            .append("start", Tools.formatDetailedInstant(getStart()))
            .append("end", Tools.formatDetailedInstant(getEnd()))
            .append("manualEntry", wasManualEntry())
            .toString();
    }
}