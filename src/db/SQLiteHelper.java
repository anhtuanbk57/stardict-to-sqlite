/*
 * A simple tool converting stardict database format (v2.4.2) to SQLite.
 * Copyright (C) 2015, Nguyễn Anh Tuấn
 * Email: anhtuanbk57@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class SQLiteHelper {

    private Connection connection;
    private Statement statement;
    private String databaseName;

    public SQLiteHelper() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void createDatabase(String databaseName) {
        this.databaseName = databaseName;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:");
            statement = connection.createStatement();
            createMainTable();
            createSynTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createMainTable() {
        String sql =    "DROP TABLE IF EXISTS main; CREATE TABLE main (" +
                        "id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "word       VARCHAR NOT NULL," +
                        "meaning    VARCHAR NOT NULL);" +
                        "CREATE INDEX word ON main (word ASC);";
        try {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createSynTable() {
        String sql =    "DROP TABLE IF EXISTS syn; CREATE TABLE syn (" +
                        "synonym      VARCHAR NOT NULL," +
                        "word_id      INTEGER);" +
                        "CREATE INDEX synonym ON syn (synonym ASC);";
        try {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertToMainTable(String word, String definition) {
        String insertSQL = "INSERT INTO main (word, meaning) VALUES (?,?);";
        try {
            PreparedStatement stmt = connection.prepareStatement(insertSQL);
            stmt.setString(1, word);
            stmt.setString(2, definition);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertToSynTable(String synonym, int wordIndex) {
        String sql = "INSERT INTO syn (synonym, word_id) VALUES (?,?);";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, synonym);
            // Need to add 1 to synIndex before inserting, because by default the start value
            // of SQLite autoincrement is 1, whereas stardict synonym indexes start at 0
            stmt.setInt(2, wordIndex + 1);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void writeDatabaseToDisk() {
        String sql = "backup to " + databaseName.replaceAll(" ", "") + ".db";
        try {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String escapeSqlString(String input) {
        // Only deal with single quote.
        if (input.lastIndexOf('\'') == -1)
            return input;

        char[] chars = input.toCharArray();
        // Allocate new char array with additional space for
        // escape characters.
        char[] newChars = new char[chars.length * 3 / 2];

        int j = 0;
        for (int i = 0; i < chars.length; i++, j++) {
            if (chars[i] == '\'') {
                newChars[j] = '\'';
                newChars[++j] = '\'';
            } else
                newChars[j] = chars[i];
        }

        return new String(newChars, 0, j);
    }

    public void closeDatabase() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
