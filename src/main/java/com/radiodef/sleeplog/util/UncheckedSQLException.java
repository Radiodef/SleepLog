package com.radiodef.sleeplog.util;

import java.util.*;
import java.sql.*;

public final class UncheckedSQLException extends RuntimeException {
    public UncheckedSQLException(SQLException cause) {
        super(Objects.requireNonNull(cause, "cause"));
    }
    
    @Override
    public SQLException getCause() {
        return (SQLException) super.getCause();
    }
}
