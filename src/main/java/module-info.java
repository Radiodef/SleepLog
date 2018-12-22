module sleeplog {
    requires javafx.controls;
    requires java.sql;
    requires com.google.common;
    requires org.apache.commons.lang3;
    requires java.desktop;
    
    exports com.radiodef.sleeplog.app;
    
    opens com.radiodef.sleeplog.db to javafx.base, org.apache.commons.lang3;
}