module sleeplog {
    requires javafx.controls;
    requires java.sql;
    requires com.google.common;
    requires org.apache.commons.lang3;
    
    exports com.radiodef.sleeplog.app;
    exports com.radiodef.sleeplog.util;
    
    opens com.radiodef.sleeplog.db to javafx.base, org.apache.commons.lang3;
}