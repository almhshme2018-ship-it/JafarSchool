package com.jafar.school;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import android.util.Base64;

public class DB extends SQLiteOpenHelper {

    private static final String DB_NAME = "school.db";
    private static final int DB_VERSION = 5;

    public DB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        createDefaultUser(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // تحديث آمن: إنشاء الجداول الحالية والناقصة دون فقدان البيانات القديمة
        createTables(db);
        createDefaultUser(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS subjects (name TEXT PRIMARY KEY NOT NULL)");
        String[] initialSubjects = {"القرآن الكريم","التربية الإسلامية","اللغة العربية","الرياضيات","العلوم","الاجتماعيات","اللغة الإنجليزية"};
        for (String subject : initialSubjects) db.execSQL("INSERT OR IGNORE INTO subjects(name) VALUES('" + subject.replace("'","''") + "')");
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS students (" +
            "id TEXT PRIMARY KEY NOT NULL," +
            "name TEXT NOT NULL," +
            "grade TEXT," +
            "class TEXT," +
            "parent TEXT," +
            "phone TEXT)"
        );

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS scores (" +
            "student_id TEXT NOT NULL," +
            "subject TEXT NOT NULL," +
            "semester INTEGER NOT NULL," +
            "mahsala REAL NOT NULL DEFAULT 0," +
            "exam REAL NOT NULL DEFAULT 0," +
            "PRIMARY KEY(student_id, subject, semester))"
        );

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS behavior (" +
            "student_id TEXT NOT NULL," +
            "semester INTEGER NOT NULL," +
            "score REAL NOT NULL DEFAULT 0," +
            "PRIMARY KEY(student_id, semester))"
        );

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS users (" +
            "name TEXT," +
            "username TEXT PRIMARY KEY," +
            "pass TEXT," +
            "role TEXT," +
            "grade TEXT," +
            "class TEXT)"
        );

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS teachers (" +
            "id TEXT PRIMARY KEY," +
            "name TEXT NOT NULL," +
            "phone TEXT," +
            "subject TEXT," +
            "grade TEXT," +
            "class TEXT)"
        );

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS attendance (" +
            "student_id TEXT NOT NULL," +
            "date TEXT NOT NULL," +
            "status TEXT NOT NULL," +
            "PRIMARY KEY(student_id, date))"
        );

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS announcements (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT," +
            "body TEXT," +
            "date TEXT)"
        );

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS timetable (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "grade TEXT," +
            "class TEXT," +
            "day TEXT," +
            "period INTEGER," +
            "subject TEXT," +
            "teacher TEXT)"
        );
    }

    private void createDefaultUser(SQLiteDatabase db) {
        Cursor c = db.rawQuery(
            "SELECT username FROM users WHERE username=?",
            new String[]{"admin"}
        );

        boolean exists = c.moveToFirst();
        c.close();

        if (!exists) {
            ContentValues v = new ContentValues();
            v.put("name", "مدير النظام الأساسي");
            v.put("username", "admin");
            v.put("pass", hashPassword("1234"));
            v.put("role", "القائم بأعمال المدير");
            v.put("grade", "");
            v.put("class", "");

            db.insert("users", null, v);
        }
    }

    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    // PBKDF2 password hashing with per-password salt; legacy plaintext passwords
    // are upgraded transparently after a successful login.
    private String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120000, 256);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).getEncoded();
            spec.clearPassword();
            return "PBKDF2$120000$" + Base64.encodeToString(salt, Base64.NO_WRAP)
                + "$" + Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("تعذر حماية كلمة المرور", e);
        }
    }

    public String passwordForStorage(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("يجب أن تتكون كلمة المرور من 6 أحرف على الأقل");
        }
        return hashPassword(password);
    }

    private boolean verifyPassword(String password, String stored) {
        if (stored == null || !stored.startsWith("PBKDF2$")) return password.equals(stored);
        try {
            String[] parts = stored.split("\\$");
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.decode(parts[2], Base64.NO_WRAP);
            byte[] expected = Base64.decode(parts[3], Base64.NO_WRAP);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, expected.length * 8);
            byte[] actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).getEncoded();
            spec.clearPassword();
            if (actual.length != expected.length) return false;
            int diff = 0;
            for (int i = 0; i < actual.length; i++) diff |= actual[i] ^ expected[i];
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || oldPassword == null || newPassword == null || newPassword.length() < 6) return false;
        Cursor c = getReadableDatabase().rawQuery("SELECT pass FROM users WHERE username=?", new String[]{username});
        if (!c.moveToFirst()) { c.close(); return false; }
        String stored = c.isNull(0) ? "" : c.getString(0);
        c.close();
        if (!verifyPassword(oldPassword, stored)) return false;
        ContentValues values = new ContentValues();
        values.put("pass", hashPassword(newPassword));
        return getWritableDatabase().update("users", values, "username=?", new String[]{username}) > 0;
    }

    public String[] authenticate(String username, String password) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT name, role, grade, class, username, pass FROM users WHERE username=?",
            new String[]{username}
        );
        if (!c.moveToFirst()) { c.close(); return null; }
        String stored = c.isNull(5) ? "" : c.getString(5);
        if (!verifyPassword(password, stored)) { c.close(); return null; }
        String[] result = new String[5];
        for (int i = 0; i < 5; i++) result[i] = c.isNull(i) ? "" : c.getString(i);
        c.close();
        // Upgrade legacy plaintext on successful authentication.
        if (stored == null || !stored.startsWith("PBKDF2$")) {
            ContentValues v = new ContentValues();
            v.put("pass", hashPassword(password));
            getWritableDatabase().update("users", v, "username=?", new String[]{username});
        }
        return result;
    }

    public int count(String table) {
        String safeTable;
        switch (table) {
            case "students":
            case "teachers":
            case "users":
            case "scores":
            case "attendance":
            case "announcements":
            case "timetable":
            case "behavior":
                safeTable = table;
                break;
            default:
                return 0;
        }

        Cursor c = getReadableDatabase().rawQuery(
            "SELECT COUNT(*) FROM " + safeTable,
            null
        );

        int result = 0;
        if (c.moveToFirst()) {
            result = c.getInt(0);
        }

        c.close();
        return result;
    }

    public int todayPresent() {
        return countAttendance("حاضر");
    }

    public int todayAbsent() {
        return countAttendance("غائب");
    }

    private int countAttendance(String status) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT COUNT(*) FROM attendance WHERE date=? AND status=?",
            new String[]{today(), status}
        );

        int result = 0;
        if (c.moveToFirst()) {
            result = c.getInt(0);
        }

        c.close();
        return result;
    }

    public boolean setAttendanceForDate(String studentId, String date, String status) {
        if (studentId == null || studentId.trim().isEmpty() || date == null || date.trim().isEmpty()) return false;
        if (!"حاضر".equals(status) && !"غائب".equals(status) && !"متأخر".equals(status) && !"غائب بعذر".equals(status)) return false;
        ContentValues values = new ContentValues();
        values.put("student_id", studentId);
        values.put("date", date);
        values.put("status", status);
        return getWritableDatabase().insertWithOnConflict("attendance", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1;
    }

    public String attendanceStatus(String studentId, String date) {
        Cursor c = getReadableDatabase().rawQuery("SELECT status FROM attendance WHERE student_id=? AND date=?", new String[]{studentId, date});
        String status = "";
        if (c.moveToFirst() && !c.isNull(0)) status = c.getString(0);
        c.close();
        return status;
    }

    public boolean setAttendance(String studentId, String status) {
        if (studentId == null || studentId.trim().isEmpty()) return false;

        if (!"حاضر".equals(status) && !"غائب".equals(status)
                && !"متأخر".equals(status) && !"غائب بعذر".equals(status)) {
            return false;
        }

        ContentValues v = new ContentValues();
        v.put("student_id", studentId);
        v.put("date", today());
        v.put("status", status);

        long result = getWritableDatabase().insertWithOnConflict(
            "attendance", null, v, SQLiteDatabase.CONFLICT_REPLACE
        );

        return result != -1;
    }

    public ArrayList<String[]> attendanceRangeForClass(String grade, String classroom, String start, String end) {
        ArrayList<String[]> result = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT s.name,s.id,a.status,COUNT(*) FROM attendance a INNER JOIN students s ON s.id=a.student_id " +
            "WHERE s.grade=? AND s.class=? AND a.date>=? AND a.date<=? GROUP BY s.id,a.status ORDER BY s.name",
            new String[]{grade,classroom,start,end});
        while(c.moveToNext()) result.add(new String[]{c.getString(0),c.getString(1),c.getString(2),String.valueOf(c.getInt(3))});
        c.close();
        return result;
    }

    public ArrayList<String[]> studentAttendanceHistory(String studentId, int limit) {
        ArrayList<String[]> result = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT date,status FROM attendance WHERE student_id=? ORDER BY date DESC LIMIT ?",
            new String[]{studentId,String.valueOf(Math.max(1,limit))});
        while(c.moveToNext()) result.add(new String[]{c.getString(0),c.getString(1)});
        c.close();
        return result;
    }

    public String studentAttendanceSummary(String studentId) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT status, COUNT(*) FROM attendance " +
            "WHERE student_id=? GROUP BY status",
            new String[]{studentId}
        );

        int present = 0, absent = 0, late = 0, excused = 0;

        while (c.moveToNext()) {
            String status = c.getString(0);
            int number = c.getInt(1);

            if ("حاضر".equals(status)) present = number;
            else if ("غائب".equals(status)) absent = number;
            else if ("متأخر".equals(status)) late = number;
            else if ("غائب بعذر".equals(status)) excused = number;
        }

        c.close();

        return "حاضر: " + present
            + "\nغائب: " + absent
            + "\nمتأخر: " + late
            + "\nغائب بعذر: " + excused;
    }

    public String latestAnnouncements() {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT title, body FROM announcements ORDER BY id DESC LIMIT 1",
            null
        );

        if (!c.moveToFirst()) {
            c.close();
            return "لا توجد إعلانات";
        }

        String title = c.isNull(0) ? "" : c.getString(0);
        String body = c.isNull(1) ? "" : c.getString(1);

        c.close();

        if (body.isEmpty()) return title;
        if (title.isEmpty()) return body;

        return title + "\n" + body;
    }

    public boolean saveAnnouncement(String title, String body) {
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("body", body);
        v.put("date", today());

        return getWritableDatabase().insert("announcements", null, v) != -1;
    }

    public String[] student(String id) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT name, id, grade, class, parent, phone " +
            "FROM students WHERE id=?",
            new String[]{id}
        );

        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        String[] result = new String[6];
        for (int i = 0; i < 6; i++) {
            result[i] = c.isNull(i) ? "" : c.getString(i);
        }

        c.close();
        return result;
    }

    public ArrayList<String[]> students(String query) {
        ArrayList<String[]> result = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT name, id, grade, class FROM students " +
            "WHERE name LIKE ? OR id LIKE ? ORDER BY name",
            new String[]{"%" + query + "%", "%" + query + "%"}
        );

        while (c.moveToNext()) {
            result.add(new String[]{
                c.getString(0),
                c.getString(1),
                c.getString(2) == null ? "" : c.getString(2),
                c.getString(3) == null ? "" : c.getString(3)
            });
        }

        c.close();
        return result;
    }

    public ArrayList<String[]> studentsInClass(String grade, String classroom) {
        ArrayList<String[]> result = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT name, id FROM students WHERE grade=? AND class=? ORDER BY name",
            new String[]{grade, classroom}
        );

        while (c.moveToNext()) {
            result.add(new String[]{c.getString(0), c.getString(1)});
        }

        c.close();
        return result;
    }

    public ArrayList<String[]> classes() {
        ArrayList<String[]> result = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT DISTINCT grade, class FROM students " +
            "WHERE grade IS NOT NULL AND class IS NOT NULL ORDER BY grade, class",
            null
        );

        while (c.moveToNext()) {
            result.add(new String[]{c.getString(0), c.getString(1)});
        }

        c.close();
        return result;
    }

    public boolean saveStudent(
            String name, String id, String grade,
            String classroom, String parent, String phone) {

        if (name == null || name.trim().isEmpty() || id == null || id.trim().isEmpty()) {
            return false;
        }

        ContentValues v = new ContentValues();
        v.put("id", id.trim());
        v.put("name", name.trim());
        v.put("grade", grade);
        v.put("class", classroom);
        v.put("parent", parent);
        v.put("phone", phone);

        return getWritableDatabase().insertWithOnConflict(
            "students", null, v, SQLiteDatabase.CONFLICT_REPLACE
        ) != -1;
    }

    public boolean deleteStudent(String id) {
        if (id == null || id.trim().isEmpty()) return false;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("scores", "student_id=?", new String[]{id});
            db.delete("behavior", "student_id=?", new String[]{id});
            db.delete("attendance", "student_id=?", new String[]{id});
            db.delete("students", "id=?", new String[]{id});

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public boolean saveBulkScores(String subject, int semester, ArrayList<String[]> rows) {
        if (subject == null || subject.trim().isEmpty() || semester < 1 || semester > 2 || rows == null) return false;
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            for (String[] row : rows) {
                if (row == null || row.length < 3 || row[0] == null) return false;
                double mahsala = Double.parseDouble(row[1]);
                double exam = Double.parseDouble(row[2]);
                if (mahsala < 0 || mahsala > 20 || exam < 0 || exam > 30) return false;
                ContentValues values = new ContentValues();
                values.put("student_id", row[0]);
                values.put("subject", subject);
                values.put("semester", semester);
                values.put("mahsala", mahsala);
                values.put("exam", exam);
                database.insertWithOnConflict("scores", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            database.endTransaction();
        }
    }

    public void setMahsala(String studentId, String subject, int semester, double value) {
        saveScorePart(studentId, subject, semester, value, null);
    }

    public void setExam(String studentId, String subject, int semester, double value) {
        saveScorePart(studentId, subject, semester, null, value);
    }

    private void saveScorePart(
            String studentId, String subject, int semester,
            Double mahsala, Double exam) {

        if (studentId == null || subject == null || semester < 1 || semester > 2) return;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            double oldMahsala = 0;
            double oldExam = 0;

            Cursor c = db.rawQuery(
                "SELECT mahsala, exam FROM scores WHERE student_id=? AND subject=? AND semester=?",
                new String[]{studentId, subject, String.valueOf(semester)}
            );

            if (c.moveToFirst()) {
                oldMahsala = c.getDouble(0);
                oldExam = c.getDouble(1);
            }
            c.close();

            ContentValues v = new ContentValues();
            v.put("student_id", studentId);
            v.put("subject", subject);
            v.put("semester", semester);
            v.put("mahsala", mahsala == null ? oldMahsala : clamp(mahsala, 0, 20));
            v.put("exam", exam == null ? oldExam : clamp(exam, 0, 30));

            db.insertWithOnConflict("scores", null, v, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public double getMahsala(String studentId, String subject, int semester) {
        return getScorePart(studentId, subject, semester, "mahsala");
    }

    public double getExam(String studentId, String subject, int semester) {
        return getScorePart(studentId, subject, semester, "exam");
    }

    private double getScorePart(String studentId, String subject, int semester, String column) {
        if (!"mahsala".equals(column) && !"exam".equals(column)) return 0;

        Cursor c = getReadableDatabase().rawQuery(
            "SELECT " + column + " FROM scores WHERE student_id=? AND subject=? AND semester=?",
            new String[]{studentId, subject, String.valueOf(semester)}
        );

        double value = 0;
        if (c.moveToFirst() && !c.isNull(0)) {
            value = c.getDouble(0);
        }

        c.close();
        return value;
    }

    public boolean hasScoreRecord(String studentId, String subject, int semester) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT 1 FROM scores WHERE student_id=? AND subject=? AND semester=? LIMIT 1",
            new String[]{studentId, subject, String.valueOf(semester)}
        );
        boolean found = c.moveToFirst();
        c.close();
        return found;
    }

    public double annualTotal(String studentId, String subject) {
        return getMahsala(studentId, subject, 1) + getExam(studentId, subject, 1)
            + getMahsala(studentId, subject, 2) + getExam(studentId, subject, 2);
    }

    public void setBehavior(String studentId, int semester, double score) {
        if (studentId == null || semester < 1 || semester > 2) return;

        ContentValues v = new ContentValues();
        v.put("student_id", studentId);
        v.put("semester", semester);
        v.put("score", clamp(score, 0, 100));

        getWritableDatabase().insertWithOnConflict("behavior", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public double getBehavior(String studentId, int semester) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT score FROM behavior WHERE student_id=? AND semester=?",
            new String[]{studentId, String.valueOf(semester)}
        );

        double value = 0;
        if (c.moveToFirst()) value = c.getDouble(0);

        c.close();
        return value;
    }

    public double annualBehavior(String studentId) {
        return (getBehavior(studentId, 1) + getBehavior(studentId, 2)) / 2.0;
    }

    public static String gradeFor(double total) {
        if (total >= 90) return "ممتاز";
        if (total >= 80) return "جيد جداً";
        if (total >= 70) return "جيد";
        if (total >= 60) return "مقبول";
        if (total >= 50) return "ضعيف";
        return "راسب";
    }

    public String resultLine(String studentId, int semester) {
        StringBuilder result = new StringBuilder();
        for (String subject : subjects()) {
            double total = getMahsala(studentId, subject, semester) + getExam(studentId, subject, semester);
            if (result.length() > 0) result.append("\n");
            result.append(subject).append(": ").append(total).append(" /50");
        }
        return result.toString();
    }

    public String[] subjects() {
        ArrayList<String> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT name FROM subjects ORDER BY rowid", null);
        while (c.moveToNext()) list.add(c.getString(0));
        c.close();
        return list.toArray(new String[0]);
    }

    public boolean addSubject(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        ContentValues v = new ContentValues(); v.put("name", name.trim());
        return getWritableDatabase().insert("subjects", null, v) != -1;
    }
    public boolean renameSubject(String oldName, String newName) {
        if (oldName == null || newName == null || newName.trim().isEmpty()) return false;
        SQLiteDatabase database=getWritableDatabase(); database.beginTransaction();
        try {
            ContentValues v=new ContentValues();v.put("subject",newName.trim());
            database.update("scores",v,"subject=?",new String[]{oldName});
            ContentValues s=new ContentValues();s.put("name",newName.trim());
            database.update("subjects",s,"name=?",new String[]{oldName});
            database.setTransactionSuccessful(); return true;
        } finally {database.endTransaction();}
    }
    public boolean removeSubject(String name) {
        if(name==null)return false; SQLiteDatabase database=getWritableDatabase();database.beginTransaction();
        try { database.delete("scores","subject=?",new String[]{name});database.delete("subjects","name=?",new String[]{name});database.setTransactionSuccessful();return true;}
        finally {database.endTransaction();}
    }

    public double[] monthlyValues(String studentId, String subject, int semester, int month) {
        return new double[]{getMahsala(studentId, subject, semester), 0, 0, 0};
    }

    public double exam(String studentId, String subject, int semester) {
        return getExam(studentId, subject, semester);
    }

    public void setMonthlyScore(
            String studentId, String subject, int semester, int month,
            double attendance, double oral, double homework, double written) {
        setMahsala(studentId, subject, semester, attendance + oral + homework + written);
    }
}
