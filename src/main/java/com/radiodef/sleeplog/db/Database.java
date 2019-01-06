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
    private static final String DB_NAME = Tools.SLEEPLOG_LOWER + "-db";
    private static final String DATES_TABLE = Tools.SLEEPLOG_LOWER + "_dates";
    private static final String NOTES_TABLE = Tools.SLEEPLOG_LOWER + "_notes";
    
    private static final String ID_COL = "id";
    private static final String START_COL = "start_instant";
    private static final String END_COL = "end_instant";
    private static final String MANUAL_COL = "manual_entry";
    
    private static final String DATE_ID_COL = "date_id";
    private static final String TEXT_COL = "text";
    
    private final Connection conn;
    
    private final PreparedStatement insertPeriodRow;
    private final PreparedStatement deletePeriodRow;
    private final PreparedStatement getRowById;
    
    private final ObservableList<SleepPeriod> rows;
    private final ObservableList<SleepPeriod> unmodifiableRows;
    
    private final PreparedStatement insertNoteRow;
    private final PreparedStatement getNoteById;
    private final PreparedStatement getNotesForPeriodId;
    
    private final ObservableList<Note> notes;
    private final ObservableList<Note> unmodifiableNotes;
    
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
                         && createDatesTable()
                         && createNotesTable()) {
            Log.note("Success");
        } else {
            Log.note("Fail");
        }
        
        this.insertPeriodRow = prepareInsertPeriodRow();
        this.deletePeriodRow = prepareDeletePeriodRow();
        this.getRowById = prepareGetRowById();
        
        this.rows = getAllSleepPeriodsImpl();
        this.unmodifiableRows = FXCollections.unmodifiableObservableList(rows);
        
        this.insertNoteRow = prepareInsertNoteRow();
        this.getNoteById = prepareGetNoteById();
        this.getNotesForPeriodId = prepareGetNotesForPeriodId();
        
        this.notes = getAllNotesImpl();
        this.unmodifiableNotes = FXCollections.unmodifiableObservableList(notes);
        
        printAllNotes();
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
        return createTable(createTableStatement, "Dates");
    }
    
    /**
     * @return true if the table exists
     */
    private boolean createNotesTable() {
        final var createTableStatement =
            "CREATE TABLE " + NOTES_TABLE
            + "("
            + ID_COL + " INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
            + DATE_ID_COL + " INTEGER,"
            + TEXT_COL + " LONG VARCHAR"
            + ")";
        return createTable(createTableStatement, "Notes");
    }
    
    private boolean createTable(String createTableStatement, String name) {
        // https://stackoverflow.com/a/5866339/2891664
        if (executeStatement(createTableStatement, "X0Y32")) {
            Log.note(name + " table created or already existed");
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
    
    private PreparedStatement prepareInsertNoteRow() {
        return prepareStatement(
            "INSERT INTO " + NOTES_TABLE
            + " (" + DATE_ID_COL + ", " + TEXT_COL + ")"
            + " VALUES (?, ?)"
        );
    }
    
    private PreparedStatement prepareGetNoteById() {
        return prepareStatement("SELECT * FROM " + NOTES_TABLE + " WHERE id = ?");
    }
    
    private PreparedStatement prepareGetNotesForPeriodId() {
        return prepareStatement("SELECT * FROM " + NOTES_TABLE + " WHERE " + DATE_ID_COL + " = ?");
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
        var keys = executeInsert(insertPeriodRow, start, end, manual);
        
        return rows.addAll(
            keys.stream()
                .map(this::getRowById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList())
        );
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
    
    private List<Integer> executeInsert(PreparedStatement statement, Object... params) {
        Log.enter();
        return
            executePreparedStatement(Log.catchingSQL(ps -> {
                var rs = ps.getGeneratedKeys();
                var ids = new ArrayList<Integer>();
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
                return ids;
            }),
            statement, params)
            .orElseGet(ArrayList::new);
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
        return unmodifiableRows;
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
    
    public boolean insertNewNote(int dateId, String text) {
        var keys = executeInsert(insertNoteRow, dateId, text);
        
        return notes.addAll(
            keys.stream()
                .map(this::getNoteById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList())
        );
    }
    
    public Optional<Note> getNoteById(int id) {
        Log.enter();
        return executePreparedStatement(Log.catchingSQL(Statement::getResultSet), getNoteById, id)
            .map(Log.catchingSQL(rs -> rs.next() ? getNote(rs) : null));
    }
    
    public ObservableList<Note> getAllNotes() {
        return unmodifiableNotes;
    }
    
    private ObservableList<Note> getAllNotesImpl() {
        var list = FXCollections.<Note>observableArrayList();
        
        if (didConnect()) {
            try (var statement = conn.createStatement()) {
                var rs = statement.executeQuery("SELECT * FROM " + NOTES_TABLE);
                
                while (rs.next()) {
                    list.add(getNote(rs));
                }
            } catch (SQLException x) {
                Log.caught(x);
            }
        }
        
        return list;
    }
    
    private static Note getNote(ResultSet rs) throws SQLException {
        var id = rs.getInt(ID_COL);
        var dateId = rs.getInt(DATE_ID_COL);
        var text = rs.getString(TEXT_COL);
        
        return new Note(id, dateId, text);
    }
    
    public List<Note> getNotesForPeriodId(int id) {
        Log.enter();
        var rsOpt = executePreparedStatement(Log.catchingSQL(Statement::getResultSet), getNotesForPeriodId, id);
        return rsOpt.map(Log.catchingSQL(rs -> {
            var list = new ArrayList<Note>();
            
            while (rs.next()) {
                list.add(getNote(rs));
            }
            
            return list;
        }))
        .orElseGet(ArrayList::new);
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
    
    public void printAllNotes() {
        for (var note : getAllNotes()) {
            Log.notef("id = %d, dateId = %d, text = %s",
                note.getID(),
                note.getDateID(),
                note.getText());
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