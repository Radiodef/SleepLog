package com.radiodef.sleeplog.db;

import javafx.beans.property.*;

import org.apache.commons.lang3.*;
import org.apache.commons.lang3.builder.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class Note {
    private final ReadOnlyIntegerProperty id;
    private final ReadOnlyIntegerProperty dateId;
    private final ReadOnlyStringProperty text;
    
    public Note(int id, int dateId, String text) {
        this.id = new SimpleIntegerProperty(this, "id", id);
        this.dateId = new SimpleIntegerProperty(this, "dateId", dateId);
        this.text = new SimpleStringProperty(this, "text", StringUtils.defaultString(text));
    }
    
    public ReadOnlyIntegerProperty idProperty() {
        return id;
    }
    
    public ReadOnlyIntegerProperty dateIdProperty() {
        return dateId;
    }
    
    public ReadOnlyStringProperty textProperty() {
        return text;
    }
    
    public int getID() {
        return id.get();
    }
    
    public int getDateID() {
        return dateId.get();
    }
    
    public String getText() {
        return text.get();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getID())
            .append(getDateID())
            .append(getText())
            .toHashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Note) {
            var that = (Note) o;
            return new EqualsBuilder()
                .append(this.getID(), that.getID())
                .append(this.getDateID(), that.getDateID())
                .append(this.getText(), that.getText())
                .isEquals();
        }
        return false;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getID())
            .append("dateId", getDateID())
            .append("text", getText())
            .toString();
    }
}
