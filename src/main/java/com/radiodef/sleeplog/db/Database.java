package com.radiodef.sleeplog.db;

import com.radiodef.sleeplog.app.*;
import com.radiodef.sleeplog.util.*;

import java.sql.*;

public class Database implements AutoCloseable {
    private static final String DB_NAME = "sleeplog-db";
    
    private final Connection conn;
    
    public Database() {
        Log.enter();
        Connection conn = null;
        
        try {
            conn = DriverManager.getConnection("jdbc:derby:" + DB_NAME + ";create=true");
        } catch (SQLException x) {
            Log.caught(x);
        }
        
        this.conn = conn;
        
        if (conn != null) {
            if (createInstantType()) {
                Log.note("Instant type created, or already existed");
            }
        }
    }
    
    public boolean didConnect() {
        return conn != null;
    }
    
    /**
     * @return true if the type was created successfully
     */
    private boolean createInstantType() {
        if (!didConnect())
            return false;
        
        final String createTypeStatement =
            "CREATE TYPE INSTANT EXTERNAL NAME '"
                + java.time.Instant.class.getCanonicalName()
                + "' LANGUAGE JAVA";
        
        try (var statement = conn.createStatement()) {
            statement.execute(createTypeStatement);
            
        } catch (SQLException x) {
            if (isTypeAlreadyExistsException(x)) {
                Log.note(x.getMessage());
            } else {
                Log.caught(x);
                return false;
            }
        }
        return true;
    }
    
    private boolean isTypeAlreadyExistsException(SQLException x) {
        return "X0Y68".equals(x.getSQLState());
    }
    
    @Override
    public void close() {
        if (didConnect()) {
            try {
                if (!conn.isClosed())
                    conn.close();
            } catch (SQLException x) {
                Log.caught(x);
            }
        }
    }
}