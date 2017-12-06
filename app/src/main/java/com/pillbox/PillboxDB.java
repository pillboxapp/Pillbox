package com.pillbox;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Debug;
import android.text.TextUtils;
import android.util.DebugUtils;
import android.util.Log;

import java.io.Console;
import java.sql.Blob;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import com.pillbox.DailyViewContent.DailyViewRow;
import com.prolificinteractive.materialcalendarview.CalendarDay;

/**
 * Created by aschey on 11/3/2017.
 */

class PillboxDB {
    /**
     * This should be treated as a static class. The sqliteDB should be set when the app is started
     * and the app should use that instance for its entire lifecycle.
     */
    private static SQLiteDatabase sqliteDB;
    private static MainActivity context;
    private static final int WEEKS_TO_ADD = 4;

    private PillboxDB() { }

    static void setDB(SQLiteDatabase newSqliteDB) {
        sqliteDB = newSqliteDB;
    }

    static void setContext(MainActivity activity) {
        context = activity;
    }

    static void close() {
        sqliteDB.close();
    }

    static void createTables() {
        createTable("Medication", new String[] {
                "ID INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT",
                "Name VARCHAR",
                "Description VARCHAR",
                "Picture BLOB"
            },
            null,
            new String[] {
                "Name"
            }
        );

        createTable("User", new String[]{
                "ID INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT",
                "Name VARCHAR",
                "Description VARCHAR"
            },
            null,
            new String[] {
                "Name"
            }
        );

        createTable("Status", new String[] {
                "ID INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT",
                "Name VARCHAR"
            },
            null,
            new String[] {
                "Name"
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
            },
            new String[] {
                "User_ID",
                "Medication_ID",
                "Day_Of_Week",
                "Time"
            }
        );

        createTable("Header", new String[] {
                "ID INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT",
                "User_ID INTEGER",
                "Medication_ID INTEGER",
                "Dosage REAL",
                "Date DATETIME",
                "Status_ID INTEGER",
                "Alarm_Code INTEGER",
                "Alarm_Time INTEGER"
            },
            new String[] {
                "User",
                "Medication",
                "Status"
            },
            new String[] {
                "User_ID",
                "Medication_ID",
                "Date"
            }
        );

        insertStatuses();
    }

    static void insertMedication(String medicationName, String description, byte[] picture) {
        ContentValues cv = new ContentValues();
        cv.put("Name", medicationName);
        cv.put("Description", description);
        cv.put("Picture", picture);
        sqliteDB.insertOrThrow("Medication", null, cv);
    }

    static void insertMedicationSchedule(String medicationName, double dosage, Globals.DayOfWeek dayOfWeek, String time) {
        int medicationID = getID("Medication", medicationName);

        ContentValues cv = new ContentValues();
        cv.put("User_ID", Globals.userID);
        cv.put("Medication_ID", medicationID);
        cv.put("Dosage", dosage);
        cv.put("Day_Of_Week", dayOfWeek.toString());
        cv.put("Time", time);
        sqliteDB.insertOrThrow("MedicationSchedule", null, cv);

        insertHeadersForMedication(medicationID, medicationName, dosage, dayOfWeek, time);
    }

    static void addMissingHeaders() {
        Cursor cursor = runFormattedQuery("Select MS.User_ID, M.Name MedicationName, MS.Medication_ID, MS.Dosage, MS.Day_Of_Week, MS.Time " +
                "From MedicationSchedule MS " +
                "Inner Join Medication M On M.ID = MS.Medication_ID");
        while (cursor.moveToNext()) {
            int userID = getCursorInt(cursor, "User_ID");
            int medicationID = getCursorInt(cursor, "Medication_ID");
            String medicationName = getCursorString(cursor, "MedicationName");
            int dosage = getCursorInt(cursor, "Dosage");
            Globals.DayOfWeek day =  getCursorEnum(cursor, "Day_Of_Week", Globals.DayOfWeek.class);
            String time = getCursorString(cursor, "Time");

            insertHeadersForMedication(userID, medicationID, medicationName, dosage, day, time);
        }
    }

    static void deleteMedicationSchedule(String medicationName) {
        String medID = getStringID("Medication", medicationName);
        Cursor cursor = runFormattedQuery("Select Alarm_Code From Header Where Medication_ID = {0}", medID);
        while (cursor.moveToNext()) {
            int alarmCode = getCursorInt(cursor, "Alarm_Code");
            Globals.deleteAlarm(context, alarmCode);
        }
        String[] params = { Integer.toString(Globals.userID), medID };
        sqliteDB.delete("MedicationSchedule", "User_ID = ? and Medication_ID = ?", params);
        // Delete all headers in the future
        sqliteDB.delete("Header", "User_ID = ? and Medication_ID = ? and Date >= datetime(CURRENT_TIMESTAMP, 'localtime')", params);
    }

    static void updateMedicationSchedule(String medicationName, double dosage, Globals.DayOfWeek dayOfWeek, String time) {
        deleteMedicationSchedule(medicationName);
        insertMedicationSchedule(medicationName, dosage, dayOfWeek, time);
    }

    private static void insertHeadersForMedication(int medicationID, String medicationName, double dosage, Globals.DayOfWeek dayOfWeek, String time) {
        insertHeadersForMedication(Globals.userID, medicationID, medicationName, dosage, dayOfWeek, time);
    }

    private static void insertHeadersForMedication(int userID, int medicationID, String medicationName, double dosage, Globals.DayOfWeek dayOfWeek, String time) {
        int statusID = getID("Status", Globals.Status.UPCOMING.toString());

        for (int i = 0; i < WEEKS_TO_ADD; i++) {
            Calendar pillDateTime = Globals.nextDateTime(i, dayOfWeek, time);
            // Don't add an entry for the current day if the time has already passed
            if (pillDateTime != null) {
                String pillDateTimeString = Globals.formatDate("yyyy-MM-dd HH:mm", pillDateTime);
                long pillMilliTime = pillDateTime.getTimeInMillis();
                int alarmCode = (int)Globals.getTimestamp();
                ContentValues cv = new ContentValues();
                cv.put("User_ID", userID);
                cv.put("Medication_ID", medicationID);
                cv.put("Status_ID", statusID);
                cv.put("Dosage", dosage);
                cv.put("Date", pillDateTimeString);
                cv.put("Alarm_Code", alarmCode);
                cv.put("Alarm_Time", pillMilliTime);
                try {
                    sqliteDB.insertOrThrow("Header", null, cv);

                    String pillTime = Globals.reformatDate("yyyy-MM-dd HH:mm", "hh:mm a", pillDateTimeString);
                    Globals.createAlarm(context, pillMilliTime, medicationName, pillTime, alarmCode);
                }
                catch (SQLiteConstraintException ex) {
                    // Already added, don't need to do anything
                }

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
        ContentValues cv = new ContentValues();
        cv.put("Name", status.toString());

        sqliteDB.insertOrThrow("Status", null, cv);
    }

    static void updateStatus(int rowID, Globals.Status newStatus) {
        ContentValues cv = new ContentValues();
        cv.put("Status_ID", getID("Status", newStatus.toString()));
        sqliteDB.update("Header", cv, "ID = ?", new String[] { Integer.toString(rowID) });
    }

    static void insertDummyData() {
        sqliteDB.execSQL("DELETE FROM User");
        sqliteDB.execSQL("INSERT INTO User Values(NULL, 'Test User', 'Test Description')");
    }

    static void insertUser(String name){
        ContentValues cv = new ContentValues();
        cv.put("Name", name);
        cv.put("Description", "");

        sqliteDB.insertOrThrow("User", null, cv);
    }

    static ArrayList<DailyViewRow> getHeadersForDay(Date currentDate) {
        ArrayList<DailyViewRow> headers = new ArrayList<>();

        String dateString = Globals.formatDate("YYYY-MM-dd", currentDate);

        // Get all headers for the current day
        Cursor cursor = runFormattedQuery("SELECT H.ID HeaderID, M.Name MedName, M.Description, H.Date, H.Dosage, " +
                "S.Name StatusName, M.Picture, H.Alarm_Code " +
                "FROM Header H " +
                "INNER JOIN Medication M On M.ID = H.Medication_ID " +
                "INNER JOIN Status S On S.ID = H.Status_ID " +
                "WHERE H.Date >= date(''{0}'') " +
                "AND H.Date < date(''{0}'', ''+1 day'') " +
                "AND H.User_ID = {1}", dateString, Globals.userID);

        // Create header objects from returned data
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int rowID = getCursorInt(cursor, "HeaderID");
                String pillName = getCursorString(cursor, "MedName");
                String pillDesc = getCursorString(cursor, "Description");
                double dosage = getCursorDouble(cursor, "Dosage");
                String date = getCursorString(cursor, "Date");
                Globals.Status status = getCursorEnum(cursor, "StatusName", Globals.Status.class);
                byte[] blob = getCursorBlob(cursor, "Picture");
                int alarmCode = getCursorInt(cursor, "Alarm_Code");

                headers.add(new DailyViewRow(rowID, pillName, pillDesc, dosage, date, status, blob, alarmCode));
            }
            cursor.close();
        }
        return headers;
    }

    static ArrayList<String> getMedications() {
        ArrayList<String> meds = new ArrayList<>();
        // Get only medications that have schedules for the current user
        Cursor cursor = runFormattedQuery("SELECT DISTINCT Name from Medication M " +
                "Inner Join MedicationSchedule S On M.ID = S.Medication_ID " +
                "Where S.User_ID = {0}", Globals.userID);

        if (cursor != null) {
            while(cursor.moveToNext()){
                String pillName = getCursorString(cursor, "Name");
                meds.add(pillName);
            }
            cursor.close();
        }
        return meds;
    }

    static ArrayList<String> getUsers() {
        ArrayList<String> users = new ArrayList<>();
        Cursor cursor = runFormattedQuery("SELECT DISTINCT Name from User");

        if (cursor != null) {
            while(cursor.moveToNext()){
                String name = getCursorString(cursor, "Name");
                users.add(name);
            }
            cursor.close();
        }
        return users;
    }

    static ArrayList<CalendarDay> getRedDates() throws ParseException {
        ArrayList<CalendarDay> dates = new ArrayList<>();
        //Cursor cursor = runFormattedQuery("select Date from Header H "+
          //      "Inner join Status S on H.Status_ID = S.ID " + "Where S.Name == ''{0}''", "Skipped;");

        Cursor cursor = runFormattedQuery("select Distinct Date from Header H " +
                                                "INNER JOIN Status S on H.Status_id = S.ID " +
                                                "where S.name = ''{0}'' and " +
                                                "H.User_ID = {1}", Globals.Status.SKIPPED, Globals.userID);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        if (cursor != null){
            while(cursor.moveToNext()){
                String s = getCursorString(cursor, "Date");
                Date date = formatter.parse(s);
                CalendarDay day = CalendarDay.from(date);
                dates.add(day);
            }
        }
        return dates;
    }

    static ArrayList<CalendarDay> getRedDates(String medication) throws ParseException {
        ArrayList<CalendarDay> dates = new ArrayList<>();
        //Cursor cursor = runFormattedQuery("select Date from Header H "+
        //      "Inner join Status S on H.Status_ID = S.ID " + "Where S.Name == ''{0}''", "Skipped;");

        Cursor cursor = runFormattedQuery("select Distinct Date from Header H " +
                " INNER JOIN Status S on H.Status_id = S.ID " +
                "INNER JOIN Medication M on H.Medication_ID = M.ID " +
                "where S.name = ''{0}'' and M.Name = ''{1}'' and " +
                "H.User_ID = {2}", Globals.Status.SKIPPED, medication, Globals.userID);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        if (cursor != null){
            while(cursor.moveToNext()){
                String s = getCursorString(cursor, "Date");
                Date date = formatter.parse(s);
                CalendarDay day = CalendarDay.from(date);
                dates.add(day);
            }
        }
        return dates;
    }

    static ArrayList<CalendarDay> getGreenDates() throws ParseException {
        ArrayList<CalendarDay> green = new ArrayList<>();
        ArrayList<CalendarDay> red = getRedDates();
        Cursor cursor = runFormattedQuery("select Distinct Date from Header H " +
                                                "INNER JOIN Status S on H.Status_id = S.ID " +
                                                "where S.name = ''{0}'' and H.User_ID = {1}", Globals.Status.TAKEN, Globals.userID);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        if (cursor != null){
            while(cursor.moveToNext()){
                String s = getCursorString(cursor, "Date");
                Date date = formatter.parse(s);
                CalendarDay day = CalendarDay.from(date);
                if(!red.contains(day)){
                    green.add(day);
                }
            }
        }
        return green;
    }

    static ArrayList<CalendarDay> getGreenDates(String medication) throws ParseException {
        ArrayList<CalendarDay> green = new ArrayList<>();
        ArrayList<CalendarDay> red = getRedDates(medication);

        Cursor cursor = runFormattedQuery("select Distinct Date from Header H " +
                "INNER JOIN Status S on H.Status_id = S.ID " +
                "INNER JOIN Medication M on H.Medication_ID = M.ID " +
                "where S.name = ''{0}'' and M.Name = ''{1}'' and " +
                "H.User_ID = {2}", Globals.Status.TAKEN, medication, Globals.userID);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        if (cursor != null){
            while(cursor.moveToNext()){
                String s = getCursorString(cursor, "Date");
                Date date = formatter.parse(s);
                CalendarDay day = CalendarDay.from(date);
                if(!red.contains(day)){
                    green.add(day);
                }
            }
        }
        return green;
    }

    static int getUserID(String name){
        Cursor cursor = runFormattedQuery("Select ID From User Where Name = ''{0}''", name);
        int id = 0;
        while (cursor.moveToNext()) {
            id = getCursorInt(cursor, "ID");
        }
        return id;
    }

    static void deleteUser(int id){
        sqliteDB.execSQL("Delete From User Where ID = " + id);
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

    private static byte[] getCursorBlob(Cursor cursor, String colName) {
        return cursor.getBlob(cursor.getColumnIndex(colName));
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

    private static void createTable(String tableName, String[] columns, String[] foreignKeys, String[] uniqueCols) {
        ArrayList<String> colList = new ArrayList<>(Arrays.asList(columns));

        if (foreignKeys != null) {
            for (String key : foreignKeys) {
                colList.add(MessageFormat.format("FOREIGN KEY({0}_ID) REFERENCES {0}(ID)", key));
            }
        }

        String uniqueClause = "";
        if (uniqueCols != null) {
            uniqueClause = MessageFormat.format(", UNIQUE({0})", TextUtils.join(", ", uniqueCols));
        }


        execFormattedSql("CREATE TABLE IF NOT EXISTS {0} ({1} {2})", tableName, TextUtils.join(", ", colList), uniqueClause);
    }

    private static int getID(String table, String name) {
        Cursor cursor = runFormattedQuery("Select ID From {0} Where Name = ''{1}''", table, name);
        int id = 0;
        while (cursor.moveToNext()) {
            id = getCursorInt(cursor, "ID");
        }

        return id;
    }

    private static String getStringID(String table, String name) {
        return Integer.toString(getID(table, name));
    }
}
