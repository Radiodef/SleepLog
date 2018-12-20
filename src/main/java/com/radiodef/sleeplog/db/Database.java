package com.radiodef.sleeplog.db;

import com.radiodef.sleeplog.app.*;
import com.radiodef.sleeplog.util.*;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.nio.file.*;

import javafx.collections.*;

public final class Database implements AutoCloseable {
    private static final String DB_NAME = "sleeplog-db";
    private static final String DATES_TABLE = "sleeplog_dates";
    
    private static final String ID_COL = "id";
    private static final String START_COL = "start_instant";
    private static final String END_COL = "end_instant";
    
    private final Connection conn;
    
    private final PreparedStatement insertPeriodRow;
    
    public Database() {
        this(null);
    }
    
    public Database(Path dbPath) {
        Log.enter();
        Connection conn = null;
        
        try {
            if (dbPath == null)
                dbPath = Paths.get("");
            
            // https://db.apache.org/derby/docs/10.8/devguide/cdevdvlp40350.html
            String dir = dbPath.resolve(DB_NAME)
                               .toAbsolutePath()
                               .toUri()
                               .toURL()
                               .getPath();
            
            conn = DriverManager.getConnection("jdbc:derby:directory:" + dir + ";create=true");
            
        } catch (Exception x) {
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
        final var createTypeStatement =
            "CREATE TYPE INSTANT EXTERNAL NAME '"
                + Instant.class.getCanonicalName()
                + "' LANGUAGE JAVA";
        if (executeStatement(createTypeStatement, "X0Y68")) {
            Log.note("Instant type created or already existed");
            return true;
        }
        return false;
    }
    
    /**
     * @return true if the table exists
     */
    private boolean createDatesTable() {
        final var createTableStatement =
            "CREATE TABLE " + DATES_TABLE
            + "("
            + ID_COL + " INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
            + START_COL + " INSTANT,"
            + END_COL + " INSTANT"
            + ")";
        // https://stackoverflow.com/a/5866339/2891664
        if (executeStatement(createTableStatement, "X0Y32")) {
            Log.note("Dates table created or already existed");
            return true;
        }
        return false;
    }
    
    private boolean executeStatement(String statement, String... okStates) {
        if (!didConnect())
            return false;
        
        try (var s = conn.createStatement()) {
            s.execute(statement);
            Log.note(statement);
            
        } catch (SQLException x) {
            if (Arrays.asList(okStates).contains(x.getSQLState())) {
                Log.note(x.getMessage());
            } else {
                Log.caught(x);
                return false;
            }
        }
        
        return true;
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
    
    public ObservableList<SleepPeriod> getAllSleepPeriods() {
        var list = FXCollections.<SleepPeriod>observableArrayList();
        
        if (didConnect()) {
            try (var statement = conn.createStatement()) {
                var rs = statement.executeQuery("SELECT * FROM " + DATES_TABLE);
                
                while (rs.next()) {
                    var id = rs.getInt(ID_COL);
                    var start = (Instant) rs.getObject(START_COL);
                    var end = (Instant) rs.getObject(END_COL);
                    
                    list.add(new SleepPeriod(id, start, end));
                }
            } catch (SQLException x) {
                Log.caught(x);
            }
        }
        
        return list;
    }
    
    public void printAllRows() {
        var rows = getAllSleepPeriods();
        Log.notef("total row count = %d", rows.size());
        
        for (var p : rows) {
            Log.notef("id = %d, start = %s, end = %s",
                p.getID(),
                Tools.formatInstant(p.getStart()),
                Tools.formatInstant(p.getEnd()));
        }
    }
    
    public boolean insertNewPeriod(Instant start, Instant end) {
        Log.enter();
        return executePreparedStatement(insertPeriodRow, Map.of(1, start, 2, end));
    }
    
    private boolean executePreparedStatement(PreparedStatement statement,
                                             Map<Integer, Object> params) {
        Log.enter();
        if (!didConnect() || statement == null)
            return false;
        
        boolean success;
        
        try {
            for (var e : params.entrySet())
                statement.setObject(e.getKey(), e.getValue());
            
            statement.execute();
            success = true;
            
        } catch (SQLException x) {
            Log.caught(x);
            success = false;
        } finally {
            try {
                statement.clearParameters();
            } catch (SQLException x) {
                Log.caught(x);
            }
        }
        
        return success;
    }
    
    @SuppressWarnings("unused")
    private void wipe() {
        if (didConnect()) {
            Log.notef("table dropped = %b", executeStatement("DROP TABLE " + DATES_TABLE));
            Log.notef("instant dropped = %b", executeStatement("DROP TYPE INSTANT RESTRICT"));
        }
    }
    
    @Override
    public void close() {
        Log.enter();
        
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