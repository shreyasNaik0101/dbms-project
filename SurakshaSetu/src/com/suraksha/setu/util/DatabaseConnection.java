package com.suraksha.setu.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for JDBC connection management (PostgreSQL).
 */
public class DatabaseConnection {

  private static final String URL = "jdbc:postgresql://localhost:5432/suraksha_setu";
  private static final String USER = "postgres";
  private static final String PASSWORD = "shreyas08"; // UPDATE THIS
  private static final String DRIVER = "org.postgresql.Driver";

  static {
    try {
      Class.forName(DRIVER);
    } catch (ClassNotFoundException e) {
      System.err.println("[ERROR] PostgreSQL JDBC Driver not found: " + e.getMessage());
    }
  }

  private DatabaseConnection() {
  }

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }

  public static void closeQuietly(AutoCloseable... resources) {
    for (AutoCloseable res : resources) {
      if (res != null) {
        try {
          res.close();
        } catch (Exception e) {
        }
      }
    }
  }
}
