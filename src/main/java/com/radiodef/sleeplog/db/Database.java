package com.radiodef.sleeplog.db;

import com.radiodef.sleeplog.app.*;

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
    }
    
    public boolean didConnect() {
        return conn != null;
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