package com.radiodef.sleeplog.db;

import com.radiodef.sleeplog.app.*;

import java.sql.*;

public class Database {
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
    
    public boolean connected() {
        return conn != null;
    }
}