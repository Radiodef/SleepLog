package com.radiodef.sleeplog.db;

import com.radiodef.sleeplog.app.*;
import com.radiodef.sleeplog.util.*;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.nio.file.*;

import javafx.collections.*;

public final class Database implements AutoCloseable {
    private static final String DB_NAME = "sleeplog-db";
    private static final String DATES_TABLE = "sleeplog_dates";
    
    private static final String ID_COL = "id";
    private static final String START_COL = "start_instant";
    private static final String END_COL = "end_instant";
    private static final String MANUAL_COL = "manual_entry";
    
    private final Connection conn;
    
    private final PreparedStatement insertPeriodRow;
    private final PreparedStatement deletePeriodRow;
    private final PreparedStatement getRowById;
    
    private final ObservableList<SleepPeriod> rows;
    
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
    
        // noinspection StatementWithEmptyBody
        if (conn != null && createInstantType()
                         && createDatesTable()) {
//            printAllRows();
        }
        
        this.insertPeriodRow = prepareInsertPeriodRow();
        this.deletePeriodRow = prepareDeletePeriodRow();
        this.getRowById = prepareGetRowById();
        
        this.rows = getAllSleepPeriodsImpl();
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
            + END_COL + " INSTANT, "
            + MANUAL_COL + " BOOLEAN WITH DEFAULT FALSE"
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
        final var statement =
            "INSERT INTO " + DATES_TABLE
            + " (" + START_COL + ", " + END_COL + ", " + MANUAL_COL + ")"
            + " VALUES (?, ?, ?)";
        return prepareStatement(statement);
    }
    
    private PreparedStatement prepareDeletePeriodRow() {
        return prepareStatement("DELETE FROM " + DATES_TABLE + " WHERE id = ?");
    }
    
    private PreparedStatement prepareGetRowById() {
        return prepareStatement("SELECT * FROM " + DATES_TABLE + " WHERE id = ?");
    }
    
    private PreparedStatement prepareStatement(String statement) {
        try {
            if (didConnect())
                return conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException x) {
            Log.caught(x);
        }
        return null;
    }
    
    public boolean insertNewPeriod(Instant start, Instant end) {
        return insertNewPeriod(start, end, false);
    }
    
    public boolean insertNewPeriod(Instant start, Instant end, boolean manual) {
        Log.enter();
        var keys =
            executePreparedStatement(ps -> {
                try {
                    var rs = ps.getGeneratedKeys();
                    var ids = new ArrayList<Integer>();
                    while (rs.next()) {
                        ids.add(rs.getInt(1));
                    }
                    return ids;
                } catch (SQLException x) {
                    Log.caught(x);
                    return null;
                }
            },
            insertPeriodRow, start, end, manual);
        
        return keys.map(list ->
            rows.addAll(
                list.stream()
                    .map(this::getRowById)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList())
            )
        ).orElse(false);
    }
    
    public boolean deletePeriod(int id) {
        Log.enter();
        if (executePreparedStatement(deletePeriodRow, id)) {
            rows.removeIf(period -> period.getID() == id);
            return true;
        }
        return false;
    }
    
    private Optional<SleepPeriod> getRowById(int id) {
        Log.enter();
        return executePreparedStatement(Log.catchingSQL(Statement::getResultSet), getRowById, id)
            .map(Log.catchingSQL(rs -> rs.next() ? getSleepPeriod(rs) : null));
    }
    
    private boolean executePreparedStatement(PreparedStatement statement, Object... params) {
        return executePreparedStatement(ps -> true, statement, params).isPresent();
    }
    
    private <T> Optional<T> executePreparedStatement(Function<? super PreparedStatement, ? extends T> fn,
                                                     PreparedStatement statement, Object... params) {
        Log.enter();
        if (!didConnect() || statement == null)
            return Optional.empty();
        
        Optional<T> result;
        
        try {
            for (int i = 0; i < params.length; ++i)
                statement.setObject(i + 1, params[i]);
            
            statement.execute();
            result = Optional.ofNullable(fn.apply(statement));
            
        } catch (SQLException x) {
            Log.caught(x);
            result = Optional.empty();
        } finally {
            try {
                statement.clearParameters();
            } catch (SQLException x) {
                Log.caught(x);
            }
        }
        
        return result;
    }
    
    public ObservableList<SleepPeriod> getAllSleepPeriods() {
        return FXCollections.unmodifiableObservableList(rows);
    }
    
    private ObservableList<SleepPeriod> getAllSleepPeriodsImpl() {
        var list = FXCollections.<SleepPeriod>observableArrayList();
        
        if (didConnect()) {
            try (var statement = conn.createStatement()) {
                var rs = statement.executeQuery("SELECT * FROM " + DATES_TABLE);
                
                while (rs.next()) {
                    list.add(getSleepPeriod(rs));
                }
            } catch (SQLException x) {
                Log.caught(x);
            }
        }
        
        return list;
    }
    
    private static SleepPeriod getSleepPeriod(ResultSet rs) throws SQLException {
        var id = rs.getInt(ID_COL);
        var start = (Instant) rs.getObject(START_COL);
        var end = (Instant) rs.getObject(END_COL);
        var manual = rs.getBoolean(MANUAL_COL);
        return new SleepPeriod(id, start, end, manual);
    }
    
    @SuppressWarnings("unused")
    public void printAllRows() {
        var rows = getAllSleepPeriods();
        Log.notef("total row count = %d", rows.size());
        
        for (var p : rows) {
            Log.notef("id = %d, start = %s, end = %s, manual = %b",
                p.getID(),
                Tools.formatInstant(p.getStart()),
                Tools.formatInstant(p.getEnd()),
                p.wasManualEntry());
        }
    }
    
    @SuppressWarnings("unused")
    private void wipe() {
        if (didConnect()) {
            Log.notef("table dropped = %b", executeStatement("DROP TABLE " + DATES_TABLE));
            Log.notef("instant dropped = %b", executeStatement("DROP TYPE INSTANT RESTRICT"));
            rows.clear();
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