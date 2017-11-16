package com.pillbox;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.sql.Blob;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import com.pillbox.DailyViewContent.DailyViewRow;

import static java.lang.Enum.valueOf;

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
                "User_ID INTEGER",
                "Medication_ID INTEGER",
                "Dosage REAL",
                "Day_Of_Week VARCHAR",
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
                "Dosage REAL",
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

        insertStatuses();

//        sqliteDB.execSQL("DELETE FROM User");
//        sqliteDB.execSQL("INSERT INTO User Values(NULL, 'Test User', 'Test Description')");
    }

    static void insertMedication(String medicationName, String description, Blob picture) {
        //execFormattedSql("INSERT INTO Medication Values(NULL, ''{0}'', ''{1}'', {2}, NULL, NULL)", medicationName, description, picture);
        ContentValues cv = new ContentValues();
        cv.put("Name", medicationName);
        cv.put("Description", description);

        sqliteDB.insertOrThrow("Medication", null, cv);
    }

    static void insertMedicationSchedule(String user, String medicationName, double dosage, Globals.DayOfWeek dayOfWeek, String time) {
        Cursor cursor1 = runFormattedQuery("Select * From Medication");
        while (cursor1.moveToNext()) {
            String name = getCursorString(cursor1, "Name");
            System.out.println(name);
        }
        Cursor cursor2 = runFormattedQuery("Select * From User");
        while (cursor2.moveToNext()) {
            String username = getCursorString(cursor2, "Name");
            System.out.println(username);
        }
        Cursor userCursor = runFormattedQuery("Select ID From User Where Name = ''{0}''", user);
        int userID = 0;
        while (userCursor.moveToNext()) {
            userID = getCursorInt(userCursor, "ID");
        }

        Cursor medCursor = runFormattedQuery("Select ID From Medication Where Name = ''{0}''", medicationName);
        int medID = 0;
        while (medCursor.moveToNext()) {
            medID = getCursorInt(medCursor, "ID");
        }
//        execFormattedSql("INSERT INTO MedicationSchedule Values(NULL, (Select ID From User Where Name = ''{0}''), " +
//                "(Select ID From Medication Where Name = ''{1}''), {2}, ''{3}'', ''{4}'')",
//                user, medicationName, dosage, dayOfWeek, time);
        ContentValues cv = new ContentValues();
        cv.put("User_ID", userID);
        cv.put("Medication_ID", medID);
        cv.put("Dosage", dosage);
        cv.put("Day_Of_Week", dayOfWeek.toString());
        cv.put("Time", time);
        sqliteDB.insertOrThrow("MedicationSchedule", null, cv);


        insertHeadersForMedication(userID, medID, dosage, dayOfWeek, time);
    }

    private static void insertHeadersForMedication(int userID, int medicationID, double dosage, Globals.DayOfWeek dayOfWeek, String time) {
        final int NUM_WEEKS = 4;

        Cursor statusCursor = runFormattedQuery("Select ID From Status Where Name = ''{0}''", Globals.Status.UPCOMING);
        int statusID = 0;
        while (statusCursor.moveToNext()) {
            statusID = getCursorInt(statusCursor, "ID");
        }

        for (int i = 0; i < NUM_WEEKS; i++) {
            String pillTime = Globals.nextDateTime(i, dayOfWeek, time);
            // Don't add an entry for the current day if the time has already passed
            if (pillTime != null) {
//                execFormattedSql("INSERT Into Header Values(NULL, (Select ID From User Where Name = ''{0}''), " +
//                        "(Select ID From Medication Where Name = ''{1}''), {2}, ''{3}'', 1, " +
//                        "(Select ID From Status Where Name = ''UPCOMING''))", user, medicationName, dosage, pillTime);
                ContentValues cv = new ContentValues();
                cv.put("User_ID", userID);
                cv.put("Medication_ID", medicationID);
                cv.put("Status_ID", statusID);
                cv.put("Dosage", dosage);
                cv.put("Date", pillTime);
                cv.put("Active_Flag", 1);
                sqliteDB.insertOrThrow("Header", null, cv);

            }
        }
    }

    private static void insertStatuses() {
        Cursor cursor = runFormattedQuery("Select * From Status");
        if (cursor == null || cursor.getCount() == 0) {
            insertStatus(Globals.Status.SKIPPED);
            insertStatus(Globals.Status.TAKEN);
            insertStatus(Globals.Status.TIME_TO_TAKE);
            insertStatus(Globals.Status.UPCOMING);
        }
    }

    private static void insertStatus(Globals.Status status) {
        execFormattedSql("INSERT INTO Status Values(NULL, ''{0}'')", status);
    }

    static void updateStatus(int rowID, Globals.Status newStatus) {
        execFormattedSql("UPDATE Header Set Status_ID = (Select ID From Status Where Name = ''{0}'') Where ID = {1}", newStatus, rowID);
    }

    static void insertDummyData() {
        //sqliteDB.execSQL("DELETE FROM Medication");

//        insertMedication("Pill Name", "Description", null);


        sqliteDB.execSQL("DELETE FROM User");
        sqliteDB.execSQL("INSERT INTO User Values(NULL, 'Test User', 'Test Description')");

        sqliteDB.execSQL("DELETE FROM Header");

        sqliteDB.execSQL("DELETE FROM MedicationSchedule");

        insertMedication("Test Medication", "Test Description", null);
        insertMedicationSchedule("Test User", "Test Medication", 1, Globals.DayOfWeek.THURSDAY, "06:00");
        insertMedication("Test Medication2", "Test Description2", null);
        insertMedicationSchedule("Test User", "Test Medication2", 1.5, Globals.DayOfWeek.THURSDAY, "22:00");
        insertMedicationSchedule("Test User", "Test Medication2", 1.5, Globals.DayOfWeek.MONDAY, "20:00");
        insertMedication("Test Medication3", "Test Description3", null);
        insertMedicationSchedule("Test User", "Test Medication3", 2, Globals.DayOfWeek.WEDNESDAY, "18:00");
    }

    static ArrayList<DailyViewRow> getHeadersForDay(Date currentDate) {
        ArrayList<DailyViewRow> headers = new ArrayList<>();

        String dateString = Globals.formatDate("YYYY-MM-dd", currentDate);
        // Get all headers for the current day

        Cursor cursor = runFormattedQuery("SELECT H.ID HeaderID, M.Name MedName, M.Description, H.Date, H.Dosage, S.Name StatusName FROM Header H " +
                "INNER JOIN Medication M On M.ID = H.Medication_ID " +
                "INNER JOIN Status S On S.ID = H.Status_ID " +
                "WHERE Date >= date(''{0}'') " +
                "AND Date < date(''{0}'', ''+1 day'')", dateString);

        // Create header objects from returned data
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int rowID = getCursorInt(cursor, "HeaderID");
                String pillName = getCursorString(cursor, "MedName");
                String pillDesc = getCursorString(cursor, "Description");
                double dosage = getCursorDouble(cursor, "Dosage");
                String date = getCursorString(cursor, "Date");
                Globals.Status status = getCursorEnum(cursor, "StatusName", Globals.Status.class);

                headers.add(new DailyViewRow(rowID, pillName, pillDesc, dosage, date, status));
            }
            cursor.close();
        }
        return headers;
    }

    static ArrayList<String> getMedications(){
        ArrayList<String> meds = new ArrayList<>();
        Cursor cursor = runFormattedQuery("SELECT Name from Medication");
        if (cursor != null) {
            while(cursor.moveToNext()){
                String pillName = getCursorString(cursor, "Name");
                meds.add(pillName);
            }
            cursor.close();
        }
        return  meds;
    }

    private static void execFormattedSql(String query, Object... formatArgs) {
        sqliteDB.beginTransaction();
        try {
            sqliteDB.execSQL(MessageFormat.format(query, formatArgs));
            sqliteDB.setTransactionSuccessful();
        }
        finally {
            sqliteDB.endTransaction();
        }
    }

    private static Cursor runFormattedQuery(String query, Object... formatArgs) {
        return sqliteDB.rawQuery(MessageFormat.format(query, formatArgs), null);
    }

    private static String getCursorString(Cursor cursor, String colName) {
        return cursor.getString(cursor.getColumnIndex(colName));
    }

    private static int getCursorInt(Cursor cursor, String colName) {
        return cursor.getInt(cursor.getColumnIndex(colName));
    }

    private static double getCursorDouble(Cursor cursor, String colName) {
        return cursor.getDouble(cursor.getColumnIndex(colName));
    }

    private static <T extends Enum<T>> T getCursorEnum(Cursor cursor, String colName, Class<T> c) {
        return T.valueOf(c, getCursorString(cursor, colName));
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
