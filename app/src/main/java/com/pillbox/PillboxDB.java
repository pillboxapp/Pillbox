package com.pillbox;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.sql.Blob;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import com.pillbox.DailyViewContent.DailyViewRow;

/**
 * Created by aschey on 11/3/2017.
 */

class PillboxDB {
    /**
     * This should be treated as a static class. The sqliteDB should be set when the app is started
     * and the app should use that instance for its entire lifecycle.
     */
    private static SQLiteDatabase sqliteDB;

    private PillboxDB() { }

    static void setDB(SQLiteDatabase newSqliteDB) {
        sqliteDB = newSqliteDB;
    }

    static void close() {
        sqliteDB.close();
    }

    static void createTables() {
        createTable("Medication", new String[] {
                "ID INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT",
                "Name VARCHAR",
                "Description VARCHAR",
                "Picture BLOB",
                "Color VARCHAR",
                "Shape VARCHAR"
            }
        );

        createTable("User", new String[]{
                "ID INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT",
                "Name VARCHAR",
                "Description VARCHAR"
            }
        );

        createTable("Status", new String[] {
                "ID INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT",
                "Name VARCHAR"
            }
        );

        // This is the table that will be used to create rows in the Header table
        createTable("MedicationSchedule", new String[]{
                "ID INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT",
                "User_ID",
                "Medication_ID INTEGER",
                "Dosage INTEGER",
                "Day_Of_Week INTEGER",
                "Time VARCHAR"
            },
            new String[] {
                "Medication"
            }
        );

        createTable("Header", new String[] {
                "ID INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT",
                "User_ID INTEGER",
                "Medication_ID INTEGER",
                "Dosage INTEGER",
                "Date DATETIME",
                "Active_Flag INTEGER",
                "Status_ID INTEGER"
            },
            new String[] {
                "User",
                "Medication",
                "Status"
            }
        );

        insertData();
    }

    static void insertMedication(String medicationName, String description, Blob picture) {
        execFormattedSql("INSERT INTO Medication Values(NULL, ''{0}'', ''{1}'', {2}, NULL, NULL)", medicationName, description, picture);
    }

    static void insertMedicationSchedule(String user, String medicationName, int dosage, Globals.DayOfWeek dayOfWeek, String time) {
        execFormattedSql("INSERT INTO MedicationSchedule Values(NULL, (Select ID From User Where Name = ''{0}''), " +
                "(Select ID From Medication Where Name = ''{1}''), ''{2}'', ''{3}'', ''{4}'')",
                user, dosage, medicationName, dayOfWeek, time);
    }

    private static void insertData() {
        sqliteDB.execSQL("DELETE FROM Status");
        insertStatus(Globals.Status.SKIPPED);
        insertStatus(Globals.Status.TAKEN);
        insertStatus(Globals.Status.TIME_TO_TAKE);
        insertStatus(Globals.Status.UPCOMING);
    }

    private static void insertStatus(Globals.Status status) {
        execFormattedSql("INSERT INTO Status Values(NULL, ''{0}'')", status.toString());
    }

    static void insertDummyData() {
        sqliteDB.execSQL("DELETE FROM Medication");
        insertMedication("Test Medication", "Test Description", null);
        insertMedication("Test Medication2", "Test Description2", null);

        sqliteDB.execSQL("DELETE FROM User");
        sqliteDB.execSQL("INSERT INTO User Values(NULL, 'Test User', 'Test Description')");

        sqliteDB.execSQL("DELETE FROM MedicationSchedule");
        insertMedicationSchedule("Test User", "Test Medication", 1, Globals.DayOfWeek.MONDAY, "06:00");

        sqliteDB.execSQL("DELETE FROM Header");
        sqliteDB.execSQL("INSERT INTO Header Values(NULL, 1, 1, 1, datetime(CURRENT_TIMESTAMP, 'localtime'), 1, 1)");
        sqliteDB.execSQL("INSERT INTO Header Values(NULL, 1, 1, 2, datetime(CURRENT_TIMESTAMP, 'localtime'), 1, 2)");
        sqliteDB.execSQL("INSERT INTO Header Values(NULL, 1, 2, 3, datetime(CURRENT_TIMESTAMP, 'localtime'), 1, 3)");
        sqliteDB.execSQL("INSERT INTO Header Values(NULL, 1, 2, 4, datetime(CURRENT_TIMESTAMP, 'localtime'), 1, 4)");
        sqliteDB.execSQL("INSERT INTO Header Values(NULL, 1, 1, 1, datetime(datetime(CURRENT_TIMESTAMP, 'localtime'), '+1 day'), 1, 1)");
        sqliteDB.execSQL("INSERT INTO Header Values(NULL, 1, 2, 3, datetime(datetime(CURRENT_TIMESTAMP, 'localtime'), '-1 day'), 1, 3)");
    }

    static ArrayList<DailyViewRow> getHeadersForDay(Date currentDate) {
        ArrayList<DailyViewRow> headers = new ArrayList<>();

        String dateString = Globals.formatDate("YYYY-MM-dd", currentDate);
        // Get all headers for the current day
        String query = MessageFormat.format("SELECT M.Name MedName, M.Description, H.Date, H.Dosage, S.Name StatusName FROM Header H " +
                "INNER JOIN Medication M On M.ID = H.Medication_ID " +
                "INNER JOIN Status S On S.ID = H.Status_ID " +
                "WHERE Date >= date(''{0}'') " +
                "AND Date < date(''{0}'', ''+1 day'')", dateString);

        Cursor cursor = runQuery(query);

        // Create header objects from returned data
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String pillName = getCursorString(cursor, "MedName");
                String pillDesc = getCursorString(cursor, "Description");
                int dosage = getCursorInt(cursor, "Dosage");
                String date = getCursorString(cursor, "Date");
                Globals.Status status = Globals.Status.valueOf(getCursorString(cursor, "StatusName"));

                headers.add(new DailyViewRow(pillName, pillDesc, dosage, date, status));
            }
            cursor.close();
        }
        return headers;
    }

    private static void execFormattedSql(String query, Object... params) {
        sqliteDB.execSQL(MessageFormat.format(query, params));
    }

    private static Cursor runQuery(String query) {
        return sqliteDB.rawQuery(query, null);
    }

    private static String getCursorString(Cursor cursor, String colName) {
        return cursor.getString(cursor.getColumnIndex(colName));
    }

    private static int getCursorInt(Cursor cursor, String colName) {
        return cursor.getInt(cursor.getColumnIndex(colName));
    }

    private static void createTable(String tableName, String[] columns) {
        createTable(tableName, columns, new String[] {});
    }

    private static void createTable(String tableName, String[] columns, String[] foreignKeys) {
        ArrayList<String> colList = new ArrayList<>(Arrays.asList(columns));

        for (String key: foreignKeys) {
            colList.add(MessageFormat.format("FOREIGN KEY({0}_ID) REFERENCES {0}(ID)", key));
        }

        execFormattedSql("CREATE TABLE IF NOT EXISTS {0} ({1})", tableName, TextUtils.join(", ", colList));
    }
}
