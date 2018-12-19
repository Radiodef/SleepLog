package com.radiodef.sleeplog.db;

import com.radiodef.sleeplog.app.*;
import com.radiodef.sleeplog.util.*;

import java.sql.*;
import java.time.*;

public class Database implements AutoCloseable {
    private static final String DB_NAME = "sleeplog-db";
    private static final String DATES_TABLE = "sleeplog_dates";
    
    private static final String ID_COL = "id";
    private static final String START_COL = "start_instant";
    private static final String END_COL = "end_instant";
    
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
        
        if (conn != null && createInstantType()
                         && createDatesTable()) {
            printAllRows();
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
                + Instant.class.getCanonicalName()
                + "' LANGUAGE JAVA";
        
        try (var statement = conn.createStatement()) {
            statement.execute(createTypeStatement);
            Log.note("Instant data type created");
            
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
    
    /**
     * @return true if the table exists
     */
    private boolean createDatesTable() {
        if (!didConnect())
            return false;
        
        final String createTableStatement =
            "CREATE TABLE " + DATES_TABLE
            + "("
            + ID_COL + " INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
            + START_COL + " INSTANT,"
            + END_COL + "end_instant INSTANT"
            + ")";
        
        try (var statement = conn.createStatement()) {
            statement.execute(createTableStatement);
            Log.note("Dates table created");
        
        } catch (SQLException x) {
            if (isTableAlreadyExistsException(x)) {
                Log.note(x.getMessage());
            } else {
                Log.caught(x);
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isTableAlreadyExistsException(SQLException x) {
        // https://stackoverflow.com/a/5866339/2891664
        return "X0Y32".equals(x.getSQLState());
    }
    
    private void printAllRows() {
        if (didConnect()) {
            try (var statement = conn.createStatement()) {
                var rs = statement.executeQuery("SELECT * FROM " + DATES_TABLE);
                int rows = 0;
                
                while (rs.next()) {
                    ++rows;
                    
                    var id = rs.getInt(ID_COL);
                    var start = (Instant) rs.getObject(START_COL);
                    var end = (Instant) rs.getObject(END_COL);
                    
                    Log.note("id = %d, start = %s, end = %s",
                        id, Tools.formatInstant(start), Tools.formatInstant(end));
                }
                
                Log.note("total row count = %d", rows);
                
            } catch (SQLException x) {
                Log.caught(x);
            }
        }
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