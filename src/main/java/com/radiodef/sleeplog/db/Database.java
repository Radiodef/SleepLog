package com.radiodef.sleeplog.db;

import com.radiodef.sleeplog.app.*;
import com.radiodef.sleeplog.util.*;

import java.sql.*;
import java.time.*;

public final class Database implements AutoCloseable {
    private static final String DB_NAME = "sleeplog-db";
    private static final String DATES_TABLE = "sleeplog_dates";
    
    private static final String ID_COL = "id";
    private static final String START_COL = "start_instant";
    private static final String END_COL = "end_instant";
    
    private final Connection conn;
    
    private final PreparedStatement insertPeriodRow;
    
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
        
        this.insertPeriodRow = prepareInsertPeriodRow();
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
            + END_COL + " INSTANT"
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
    
    private PreparedStatement prepareInsertPeriodRow() {
        try {
            if (didConnect()) {
                final var insertPeriodRowStatement =
                    "INSERT INTO " + DATES_TABLE
                    + " (" + START_COL + ", " + END_COL + ")"
                    + " VALUES (?, ?)";
                return conn.prepareStatement(insertPeriodRowStatement);
            }
        } catch (SQLException x) {
            Log.caught(x);
        }
        return null;
    }
    
    public void printAllRows() {
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
    
    public boolean insertNewPeriod(Instant start, Instant end) {
        Log.enter();
        if (!didConnect() || insertPeriodRow == null)
            return false;
        
        boolean success;
        
        try {
            insertPeriodRow.setObject(1, start);
            insertPeriodRow.setObject(2, end);
            
            insertPeriodRow.execute();
            success = true;
            
        } catch (SQLException x) {
            Log.caught(x);
            success = false;
        } finally {
            try {
                insertPeriodRow.clearParameters();
            } catch (SQLException x) {
                Log.caught(x);
            }
        }
        
        return success;
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