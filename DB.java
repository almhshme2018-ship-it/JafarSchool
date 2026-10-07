package com.jafar.school;

import android.content.Context;
import android.database.Cursor;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class DB extends android.database.sqlite.SQLiteOpenHelper {

    DB(Context c) {
        super(c, "jafar_school.db", null, 9);
    }

    public void onCreate(android.database.sqlite.SQLiteDatabase d) {

        d.execSQL("CREATE TABLE students(" +
                "name TEXT,id TEXT PRIMARY KEY,grade TEXT,classroom TEXT,parent TEXT,phone TEXT)");

        d.execSQL("CREATE TABLE teachers(" +
                "name TEXT,subject TEXT,role TEXT)");

        d.execSQL("CREATE TABLE classes(" +
                "grade TEXT,classroom TEXT,PRIMARY KEY(grade,classroom))");

        d.execSQL("CREATE TABLE attendance(" +
                "student_id TEXT,date TEXT,status TEXT," +
                "PRIMARY KEY(student_id,date))");

        d.execSQL("CREATE TABLE monthly_scores(" +
                "student_id TEXT,subject TEXT,semester INTEGER,month INTEGER," +
                "regular REAL,oral REAL,homework REAL,written REAL," +
                "PRIMARY KEY(student_id,subject,semester,month))");

        d.execSQL("CREATE TABLE exams(" +
                "student_id TEXT,subject TEXT,semester INTEGER,score REAL," +
                "PRIMARY KEY(student_id,subject,semester))");

        d.execSQL("CREATE TABLE announcements(" +
                "date TEXT,text TEXT)");

        d.execSQL("CREATE TABLE users(" +
                "name TEXT,role TEXT,permissions TEXT,username TEXT PRIMARY KEY," +
                "password_hash TEXT,grade TEXT,classroom TEXT)");

        d.execSQL("CREATE TABLE user_students(" +
                "username TEXT,student_id TEXT," +
                "PRIMARY KEY(username,student_id))");

        d.execSQL(
                "INSERT INTO users VALUES(" +
                "'القائم بأعمال المدير'," +
                "'القائم بأعمال المدير'," +
                "'إدارة كاملة'," +
                "'admin','" +
                sha256("1234") +
                "','','')"
        );

        d.execSQL("INSERT INTO classes VALUES('الأول','أ')");
        d.execSQL("INSERT INTO classes VALUES('الثاني','أ')");

        d.execSQL(
                "INSERT INTO announcements VALUES('" +
                new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        .format(new Date()) +
                "','مرحبا بكم في نظام مدرسة جعفر الذكي')"
        );
    }

    public void onUpgrade(
            android.database.sqlite.SQLiteDatabase d,
            int oldVersion,
            int newVersion) {
    }

    String[] authenticate(String u, String p) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT name,role,grade,classroom,username " +
                "FROM users WHERE username=? AND password_hash=?",
                new String[]{u, sha256(p)}
        );

        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        String[] r = {
                c.getString(0),
                c.getString(1),
                c.getString(2) == null ? "" : c.getString(2),
                c.getString(3) == null ? "" : c.getString(3),
                c.getString(4)
        };

        c.close();
        return r;
    }

    int count(String t) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + t,
                null
        );

        c.moveToFirst();

        int n = c.getInt(0);

        c.close();

        return n;
    }

    void saveStudent(
            String n,
            String id,
            String g,
            String cl,
            String p,
            String ph) {

        getWritableDatabase().execSQL(
                "INSERT OR REPLACE INTO students VALUES(?,?,?,?,?,?)",
                new Object[]{n, id, g, cl, p, ph}
        );
    }

    ArrayList<String[]> students(String q) {

        ArrayList<String[]> a = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT name,id,grade,classroom " +
                "FROM students " +
                "WHERE name LIKE? OR id LIKE? " +
                "ORDER BY name",
                new String[]{"%" + q + "%", "%" + q + "%"}
        );

        while (c.moveToNext()) {
            a.add(new String[]{
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3)
            });
        }

        c.close();

        return a;
    }

    String[] student(String id) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM students WHERE id=?",
                new String[]{id}
        );

        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        String[] r = {
                c.getString(0),
                c.getString(1),
                c.getString(2),
                c.getString(3),
                c.getString(4),
                c.getString(5)
        };

        c.close();

        return r;
    }

    ArrayList<String[]> studentsInClass(String g, String cl) {

        ArrayList<String[]> a = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT name,id FROM students " +
                "WHERE grade=? AND classroom=? " +
                "ORDER BY name",
                new String[]{g, cl}
        );

        while (c.moveToNext()) {
            a.add(new String[]{
                    c.getString(0),
                    c.getString(1)
            });
        }

        c.close();

        return a;
    }

    void addTeacher(String n, String s, String r) {

        getWritableDatabase().execSQL(
                "INSERT INTO teachers VALUES(?,?,?)",
                new Object[]{n, s, r}
        );
    }

    ArrayList<String[]> teachers() {

        ArrayList<String[]> a = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM teachers",
                null
        );

        while (c.moveToNext()) {
            a.add(new String[]{
                    c.getString(0),
                    c.getString(1),
                    c.getString(2)
            });
        }

        c.close();

        return a;
    }

    void addClass(String g, String c) {

        getWritableDatabase().execSQL(
                "INSERT OR IGNORE INTO classes VALUES(?,?)",
                new Object[]{g, c}
        );
    }

    ArrayList<String[]> classes() {

        ArrayList<String[]> a = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT c.grade,c.classroom," +
                "(SELECT COUNT(*) FROM students s " +
                "WHERE s.grade=c.grade AND s.classroom=c.classroom) " +
                "FROM classes c",
                null
        );

        while (c.moveToNext()) {
            a.add(new String[]{
                    c.getString(0),
                    c.getString(1),
                    c.getString(2)
            });
        }

        c.close();

        return a;
    }

    String attendanceStatus(String id) {

        String date =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        .format(new Date());

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT status FROM attendance " +
                "WHERE student_id=? AND date=?",
                new String[]{id, date}
        );

        String s = "";

        if (c.moveToFirst()) {
            s = c.getString(0);
        }

        c.close();

        return s;
    }

    void setAttendance(String id, String st) {

        String date =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        .format(new Date());

        getWritableDatabase().execSQL(
                "INSERT OR REPLACE INTO attendance VALUES(?,?,?)",
                new Object[]{id, date, st}
        );
    }

    int todayPresent() {
        return todayCount("حاضر");
    }

    int todayAbsent() {
        return todayCount("غائب");
    }

    int todayCount(String st) {

        String date =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        .format(new Date());

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM attendance " +
                "WHERE date=? AND status=?",
                new String[]{date, st}
        );

        c.moveToFirst();

        int n = c.getInt(0);

        c.close();

        return n;
    }

    void setMonthlyScore(
            String id,
            String sub,
            int sem,
            int mon,
            double r1,
            double r2,
            double r3,
            double r4) {

        getWritableDatabase().execSQL(
                "INSERT OR REPLACE INTO monthly_scores " +
                "VALUES(?,?,?,?,?,?,?,?)",
                new Object[]{
                        id, sub, sem, mon,
                        r1, r2, r3, r4
                }
        );
    }

    void setExam(
            String id,
            String sub,
            int sem,
            double sc) {

        getWritableDatabase().execSQL(
                "INSERT OR REPLACE INTO exams VALUES(?,?,?,?)",
                new Object[]{id, sub, sem, sc}
        );
    }

    double[] monthlyValues(
            String id,
            String sub,
            int sem,
            int mon) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT regular,oral,homework,written " +
                "FROM monthly_scores " +
                "WHERE student_id=? AND subject=? " +
                "AND semester=? AND month=?",
                new String[]{
                        id,
                        sub,
                        String.valueOf(sem),
                        String.valueOf(mon)
                }
        );

        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        double[] v = {
                c.getDouble(0),
                c.getDouble(1),
                c.getDouble(2),
                c.getDouble(3)
        };

        c.close();

        return v;
    }

    double exam(String id, String sub, int sem) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT score FROM exams " +
                "WHERE student_id=? AND subject=? AND semester=?",
                new String[]{
                        id,
                        sub,
                        String.valueOf(sem)
                }
        );

        if (!c.moveToFirst()) {
            c.close();
            return 0;
        }

        double x = c.getDouble(0);

        c.close();

        return x;
    }

    String[] subjects() {

        return new String[]{
                "اللغة العربية",
                "الرياضيات",
                "القرآن الكريم",
                "العلوم",
                "التربية الإسلامية",
                "الاجتماعيات"
        };
    }

    String resultLine(String id, int mode) {

        double total = 0;

        for (String sub : subjects()) {

            double sum = 0;

            if (mode == 0 || mode == 2) {

                for (int m = 1; m <= 3; m++) {

                    double[] v =
                            monthlyValues(id, sub, 1, m);

                    if (v != null) {
                        sum += v[0] + v[1] + v[2] + v[3];
                    }
                }

                sum += exam(id, sub, 1);
            }

            if (mode == 1 || mode == 2) {

                double s2 = 0;

                for (int m = 1; m <= 3; m++) {

                    double[] v =
                            monthlyValues(id, sub, 2, m);

                    if (v != null) {
                        s2 += v[0] + v[1] + v[2] + v[3];
                    }
                }

                s2 += exam(id, sub, 2);

                if (mode == 2) {
                    sum += s2;
                } else {
                    sum = s2;
                }
            }

            total += sum;
        }

        double max =
                subjects().length *
                (mode == 2 ? 200 : 100);

        double pct =
                max > 0 ? total * 100 / max : 0;

        String taq =
                pct >= 90 ? "ممتاز" :
                pct >= 80 ? "جيد جدا" :
                pct >= 65 ? "جيد" :
                pct >= 50 ? "مقبول" :
                "ضعيف";

        return String.format(
                Locale.US,
                "المجموع: %.0f / %.0f - %.1f%% - %s",
                total,
                max,
                pct,
                taq
        );
    }

    String studentAttendanceSummary(String id) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM attendance " +
                "WHERE student_id=? AND status='حاضر'",
                new String[]{id}
        );

        c.moveToFirst();

        int h = c.getInt(0);

        c.close();

        c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM attendance " +
                "WHERE student_id=? AND status='غائب'",
                new String[]{id}
        );

        c.moveToFirst();

        int g = c.getInt(0);

        c.close();

        return "حاضر: " + h + " | غائب: " + g;
    }

    String daySchedule(String day) {

        return "جدول " + day + " محفوظ محلياً";
    }

    void addAnnouncement(String t) {

        getWritableDatabase().execSQL(
                "INSERT INTO announcements VALUES(?,?)",
                new Object[]{
                        new SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.US
                        ).format(new Date()),
                        t
                }
        );
    }

    ArrayList<String[]> announcements() {

        ArrayList<String[]> a = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT date,text FROM announcements " +
                "ORDER BY date DESC",
                null
        );

        while (c.moveToNext()) {

            a.add(new String[]{
                    c.getString(0),
                    c.getString(1)
            });
        }

        c.close();

        return a;
    }

    String latestAnnouncements() {

        ArrayList<String[]> a = announcements();

        return a.isEmpty()
                ? "لا توجد إعلانات"
                : a.get(0)[1];
    }

    ArrayList<String[]> users() {

        ArrayList<String[]> a = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT name,role,permissions,username FROM users",
                null
        );

        while (c.moveToNext()) {

            a.add(new String[]{
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3)
            });
        }

        c.close();

        return a;
    }

    void addUser(
            String name,
            String role,
            String username,
            String pass) {

        getWritableDatabase().execSQL(
                "INSERT OR REPLACE INTO users " +
                "VALUES(?,?,?,?,?,?,?)",
                new Object[]{
                        name,
                        role,
                        "",
                        username,
                        sha256(pass),
                        "",
                        ""
                }
        );
    }

    ArrayList<String[]> linkedStudents(String username) {

        ArrayList<String[]> a = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT s.name,s.id,s.grade,s.classroom " +
                "FROM students s " +
                "INNER JOIN user_students u " +
                "ON u.student_id=s.id " +
                "WHERE u.username=?",
                new String[]{username}
        );

        while (c.moveToNext()) {

            a.add(new String[]{
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3)
            });
        }

        c.close();

        return a;
    }

    static String sha256(String x) {

        try {

            MessageDigest md =
                    MessageDigest.getInstance("SHA-256");

            byte[] b =
                    md.digest(
                            x.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder s = new StringBuilder();

            for (byte v : b) {

                s.append(
                        String.format(
                                Locale.US,
                                "%02x",
                                v
                        )
                );
            }

            return s.toString();

        } catch (Exception e) {

            return x;
        }
    }
}
