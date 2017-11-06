package com.pillbox;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import com.pillbox.DailyViewContent.DailyViewRow;

/**
 * Created by aschey on 11/3/2017.
 */

class PillboxDB {
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

    private static void insertData() {
        sqliteDB.execSQL("DELETE FROM Status");
        insertStatus(Globals.Status.SKIPPED);
        insertStatus(Globals.Status.TAKEN);
        insertStatus(Globals.Status.TIME_TO_TAKE);
        insertStatus(Globals.Status.UPCOMING);
    }

    private static void insertStatus(Globals.Status status) {
        sqliteDB.execSQL(MessageFormat.format("INSERT INTO Status Values(NULL, ''{0}'')", status.toString()));
    }

    static void insertDummyData() {
        sqliteDB.execSQL("DELETE FROM Medication");
        sqliteDB.execSQL("INSERT INTO Medication Values(NULL, 'Test Medication', 'Test Description', NULL, NULL, NULL)");

        sqliteDB.execSQL("DELETE FROM User");
        sqliteDB.execSQL("INSERT INTO User Values(NULL, 'Test User', 'Test Description')");

        sqliteDB.execSQL("DELETE FROM Header");
        sqliteDB.execSQL("INSERT INTO Header Values(NULL, 1, 1, 1, datetime(CURRENT_TIMESTAMP, 'localtime'), 1, 1)");
        sqliteDB.execSQL("INSERT INTO Header Values(NULL, 1, 1, 1, datetime(CURRENT_TIMESTAMP, 'localtime'), 1, 2)");
        sqliteDB.execSQL("INSERT INTO Header Values(NULL, 1, 1, 1, datetime(CURRENT_TIMESTAMP, 'localtime'), 1, 3)");
        sqliteDB.execSQL("INSERT INTO Header Values(NULL, 1, 1, 1, datetime(CURRENT_TIMESTAMP, 'localtime'), 1, 4)");
    }

    static ArrayList<DailyViewRow> getHeadersForDay(Date currentDate) {
        ArrayList<DailyViewRow> headers = new ArrayList<>();
        String dateString = new SimpleDateFormat("YYYY-MM-dd", Locale.US).format(currentDate);

        // Get all headers for the current day
        String query = MessageFormat.format("SELECT M.Name, H.Date, S.Name FROM Header H " +
                "INNER JOIN Medication M On M.ID = H.Medication_ID " +
                "INNER JOIN Status S On S.ID = H.Status_ID " +
                "WHERE Date >= date(''{0}'') " +
                "AND Date < date(''{0}'', ''+1 day'')", dateString);

        Cursor cursor = sqliteDB.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String pillName = cursor.getString(0);
                String date = cursor.getString(1);
                Globals.Status status = Globals.Status.valueOf(cursor.getString(2));
                headers.add(new DailyViewRow(pillName, date, status));
            }
            cursor.close();
        }
        return headers;
    }

    private static void createTable(String tableName, String[] columns) {
        createTable(tableName, columns, new String[] {});
    }

    private static void createTable(String tableName, String[] columns, String[] foreignKeys) {
        ArrayList<String> colList = new ArrayList<>(Arrays.asList(columns));

        for (String key: foreignKeys) {
            colList.add(MessageFormat.format("FOREIGN KEY({0}_ID) REFERENCES {0}(ID)", key));
        }
        sqliteDB.execSQL(MessageFormat.format("CREATE TABLE IF NOT EXISTS {0} ({1})", tableName, TextUtils.join(", ", colList)));
    }
}
