package com.jafar.school;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class DB extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 10;

    DB(Context c) {
        super(c, "jafar_school.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase d) {

        d.execSQL("CREATE TABLE students(" +
                "name TEXT,id TEXT PRIMARY KEY,grade TEXT," +
                "classroom TEXT,parent TEXT,phone TEXT)");

        d.execSQL("CREATE TABLE teachers(" +
                "name TEXT,subject TEXT,role TEXT)");

        d.execSQL("CREATE TABLE classes(" +
                "grade TEXT,classroom TEXT," +
                "PRIMARY KEY(grade,classroom))");

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

        d.execSQL("CREATE TABLE behavior(" +
                "student_id TEXT,semester INTEGER," +
                "rating TEXT,note TEXT," +
                "PRIMARY KEY(student_id,semester))");

        d.execSQL("CREATE TABLE announcements(" +
                "date TEXT,text TEXT)");

        d.execSQL("CREATE TABLE users(" +
                "name TEXT,role TEXT,permissions TEXT," +
                "username TEXT PRIMARY KEY,password_hash TEXT," +
                "grade TEXT,classroom TEXT)");

        d.execSQL("CREATE TABLE user_students(" +
                "username TEXT,student_id TEXT," +
                "PRIMARY KEY(username,student_id))");

        d.execSQL(
                "INSERT INTO users VALUES(?,?,?,?,?,?,?)",
                new Object[]{
                        "القائم بأعمال المدير",
                        "القائم بأعمال المدير",
                        "إدارة كاملة",
                        "admin",
                        sha256("1234"),
                        "",
                        ""
                }
        );

        d.execSQL(
                "INSERT INTO classes VALUES(?,?)",
                new Object[]{"الأول", "أ"}
        );

        d.execSQL(
                "INSERT INTO classes VALUES(?,?)",
                new Object[]{"الثاني", "أ"}
        );

        d.execSQL(
                "INSERT INTO announcements VALUES(?,?)",
                new Object[]{
                        todayDateStatic(),
                        "مرحبا بكم في نظام مدرسة جعفر الذكي"
                }
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase d,
            int oldVersion,
            int newVersion) {

        /*
         * ترقية آمنة:
         * لا نحذف جداول الطلاب أو الدرجات أو الحضور.
         */

        if (oldVersion < 10) {

            d.execSQL(
                    "CREATE TABLE IF NOT EXISTS behavior(" +
                    "student_id TEXT,semester INTEGER," +
                    "rating TEXT,note TEXT," +
                    "PRIMARY KEY(student_id,semester))"
            );

            /*
             * تغيير اسم المادة القديمة مع الاحتفاظ بالدرجات.
             */
            d.execSQL(
                    "UPDATE monthly_scores " +
                    "SET subject='التربية الوطنية' " +
                    "WHERE subject='الاجتماعيات'"
            );

            d.execSQL(
                    "UPDATE exams " +
                    "SET subject='التربية الوطنية' " +
                    "WHERE subject='الاجتماعيات'"
            );
        }
    }

    // =========================================================
    // التاريخ
    // =========================================================

    static String todayDateStatic() {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(new Date());
    }

    String todayDate() {
        return todayDateStatic();
    }

    String formatDate(Calendar cal) {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(cal.getTime());
    }

    // =========================================================
    // تسجيل الدخول
    // =========================================================

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

    // =========================================================
    // الإحصائيات
    // =========================================================

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

    // =========================================================
    // الطلاب
    // =========================================================

    void saveStudent(
            String n,
            String id,
            String g,
            String cl,
            String p,
            String ph) {

        SQLiteDatabase d = getWritableDatabase();

        d.execSQL(
                "INSERT OR REPLACE INTO students " +
                "(name,id,grade,classroom,parent,phone) " +
                "VALUES(?,?,?,?,?,?)",
                new Object[]{n, id, g, cl, p, ph}
        );

        if (g != null && !g.trim().isEmpty()
                && cl != null && !cl.trim().isEmpty()) {

            d.execSQL(
                    "INSERT OR IGNORE INTO classes " +
                    "(grade,classroom) VALUES(?,?)",
                    new Object[]{g.trim(), cl.trim()}
            );
        }
    }

    ArrayList<String[]> students(String q) {

        ArrayList<String[]> a = new ArrayList<>();

        if (q == null) q = "";
        q = q.trim();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT name,id,grade,classroom FROM students " +
                "WHERE name LIKE ? OR id LIKE ? ORDER BY name",
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
                "SELECT name,id,grade,classroom,parent,phone " +
                "FROM students WHERE id=?",
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

    ArrayList<String[]> studentsInClass(
            String g,
            String cl) {

        ArrayList<String[]> a = new ArrayList<>();

        if (g == null) g = "";
        if (cl == null) cl = "";

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT name,id FROM students " +
                "WHERE TRIM(grade)=? AND TRIM(classroom)=? " +
                "ORDER BY name",
                new String[]{g.trim(), cl.trim()}
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

    // =========================================================
    // المعلمون
    // =========================================================

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

    // =========================================================
    // الصفوف والشعب
    // =========================================================

    void addClass(String g, String c) {

        getWritableDatabase().execSQL(
                "INSERT OR IGNORE INTO classes VALUES(?,?)",
                new Object[]{g, c}
        );
    }

    // =========================================================
    // الحضور
    // =========================================================

    String attendanceStatus(String id) {
        return attendanceStatus(id, todayDate());
    }

    String attendanceStatus(String id, String date) {

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
        setAttendance(id, todayDate(), st);
    }

    void setAttendance(String id, String date, String st) {

        if (id == null || id.trim().isEmpty()
                || date == null || date.trim().isEmpty()
                || st == null || st.trim().isEmpty()) {
            return;
        }

        getWritableDatabase().execSQL(
                "INSERT OR REPLACE INTO attendance " +
                "(student_id,date,status) VALUES(?,?,?)",
                new Object[]{id, date, st}
        );
    }

    void clearAttendance(String id, String date) {

        getWritableDatabase().execSQL(
                "DELETE FROM attendance WHERE student_id=? AND date=?",
                new Object[]{id, date}
        );
    }

    int todayPresent() {
        return todayCount("حاضر");
    }

    int todayAbsent() {
        return todayCount("غائب");
    }

    int todayLate() {
        return todayCount("متأخر");
    }

    int todayExcused() {
        return todayCount("بعذر");
    }

    int todayCount(String st) {
        return dateCount(todayDate(), st);
    }

    int dateCount(String date, String st) {

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

    int monthlyAttendanceCount(
            String id,
            int year,
            int month,
            String status) {

        String prefix = String.format(
                Locale.US,
                "%04d-%02d-",
                year,
                month
        );

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM attendance " +
                "WHERE student_id=? AND date LIKE ? AND status=?",
                new String[]{id, prefix + "%", status}
        );

        c.moveToFirst();
        int n = c.getInt(0);
        c.close();

        return n;
    }

    double monthlyAttendancePercentage(
            String id,
            int year,
            int month) {

        int present = monthlyAttendanceCount(
                id, year, month, "حاضر"
        );

        int absent = monthlyAttendanceCount(
                id, year, month, "غائب"
        );

        int late = monthlyAttendanceCount(
                id, year, month, "متأخر"
        );

        int excused = monthlyAttendanceCount(
                id, year, month, "بعذر"
        );

        int total = present + absent + late + excused;

        if (total == 0) return 0;

        return present * 100.0 / total;
    }

    int countAttendanceStatus(String id, String status) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM attendance " +
                "WHERE student_id=? AND status=?",
                new String[]{id, status}
        );

        c.moveToFirst();
        int n = c.getInt(0);
        c.close();

        return n;
    }

    String studentAttendanceSummary(String id) {

        int h = countAttendanceStatus(id, "حاضر");
        int g = countAttendanceStatus(id, "غائب");
        int l = countAttendanceStatus(id, "متأخر");
        int b = countAttendanceStatus(id, "بعذر");

        return "حاضر: " + h +
                " | غائب: " + g +
                " | متأخر: " + l +
                " | بعذر: " + b;
    }

    // =========================================================
    // الدرجات الشهرية
    // =========================================================

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
                "(student_id,subject,semester,month," +
                "regular,oral,homework,written) " +
                "VALUES(?,?,?,?,?,?,?,?)",
                new Object[]{
                        id, sub, sem, mon,
                        r1, r2, r3, r4
                }
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
                        id, sub,
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

    double monthlyTotal(
            String id,
            String sub,
            int sem,
            int mon) {

        double[] v = monthlyValues(id, sub, sem, mon);

        if (v == null) return 0;

        return v[0] + v[1] + v[2] + v[3];
    }

    // =========================================================
    // امتحانات الفصل
    // =========================================================

    void setExam(String id, String sub, int sem, double sc) {

        getWritableDatabase().execSQL(
                "INSERT OR REPLACE INTO exams " +
                "(student_id,subject,semester,score) VALUES(?,?,?,?)",
                new Object[]{id, sub, sem, sc}
        );
    }

    double exam(String id, String sub, int sem) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT score FROM exams " +
                "WHERE student_id=? AND subject=? AND semester=?",
                new String[]{id, sub, String.valueOf(sem)}
        );

        if (!c.moveToFirst()) {
            c.close();
            return 0;
        }

        double x = c.getDouble(0);
        c.close();

        return x;
    }

    // =========================================================
    // السلوك - سجل مستقل لكل فصل
    // =========================================================

    void setBehavior(
            String id,
            int semester,
            String rating,
            String note) {

        getWritableDatabase().execSQL(
                "INSERT OR REPLACE INTO behavior " +
                "(student_id,semester,rating,note) VALUES(?,?,?,?)",
                new Object[]{
                        id,
                        semester,
                        rating == null ? "" : rating,
                        note == null ? "" : note
                }
        );
    }

    String[] behavior(String id, int semester) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT rating,note FROM behavior " +
                "WHERE student_id=? AND semester=?",
                new String[]{id, String.valueOf(semester)}
        );

        if (!c.moveToFirst()) {
            c.close();
            return new String[]{"", ""};
        }

        String[] r = {
                c.getString(0) == null ? "" : c.getString(0),
                c.getString(1) == null ? "" : c.getString(1)
        };

        c.close();
        return r;
    }

    // =========================================================
    // المواد الدراسية - الترتيب المعتمد للشهادة
    // =========================================================

    String[] subjects() {

        return new String[]{
                "القرآن الكريم",
                "التربية الإسلامية",
                "اللغة العربية",
                "الرياضيات",
                "العلوم",
                "التربية الوطنية"
        };
    }

    // =========================================================
    // متوسط الأشهر الثلاثة للمادة
    // =========================================================

    double semesterMonthlyAverage(
            String id,
            String sub,
            int semester) {

        double sum = 0;
        int count = 0;

        for (int m = 1; m <= 3; m++) {

            double[] v = monthlyValues(id, sub, semester, m);

            if (v != null) {
                sum += v[0] + v[1] + v[2] + v[3];
                count++;
            }
        }

        if (count == 0) return 0;

        return sum / count;
    }

    // =========================================================
    // نتيجة المادة في الفصل من 50
    //
    // متوسط الأشهر الثلاثة من 100 يتحول إلى 20
    // امتحان الفصل من 30
    // مجموع المادة في الفصل من 50
    // =========================================================

    double semesterSubjectResult(
            String id,
            String sub,
            int semester) {

        double monthly = semesterMonthlyAverage(
                id, sub, semester
        );

        double monthly20 = monthly * 20.0 / 100.0;
        double exam30 = exam(id, sub, semester);

        return monthly20 + exam30;
    }

    // =========================================================
    // النتيجة العامة
    //
    // mode 0 = الفصل الأول من 300
    // mode 1 = الفصل الثاني من 300
    // mode 2 = العام الدراسي من 600
    //
    // السلوك يظهر مستقلاً ولا يدخل في مجموع المواد.
    // =========================================================

    String resultLine(String id, int mode) {

        double total = 0;
        String[] subs = subjects();

        for (String sub : subs) {

            if (mode == 0) {

                total += semesterSubjectResult(id, sub, 1);

            } else if (mode == 1) {

                total += semesterSubjectResult(id, sub, 2);

            } else if (mode == 2) {

                total += semesterSubjectResult(id, sub, 1);
                total += semesterSubjectResult(id, sub, 2);
            }
        }

        double max = mode == 2
                ? subs.length * 100.0
                : subs.length * 50.0;

        double pct = max > 0
                ? total * 100.0 / max
                : 0;

        String taq = pct >= 90
                ? "ممتاز"
                : pct >= 80
                ? "جيد جدا"
                : pct >= 65
                ? "جيد"
                : pct >= 50
                ? "مقبول"
                : "ضعيف";

        String result = pct >= 50 ? "ناجح" : "راسب";

        return String.format(
                Locale.US,
                "المجموع: %.1f / %.0f - %.1f%% - %s - %s",
                total, max, pct, taq, result
        );
    }

    // =========================================================
    // نتيجة مادة واحدة
    // =========================================================

    double subjectResult(String id, String sub, int semester) {
        return semesterSubjectResult(id, sub, semester);
    }

    // =========================================================
    // جدول المدرسة
    // =========================================================

    String daySchedule(String day) {
        return "جدول " + day + " محفوظ محلياً";
    }

    // =========================================================
    // الإعلانات
    // =========================================================

    void addAnnouncement(String t) {

        getWritableDatabase().execSQL(
                "INSERT INTO announcements(date,text) VALUES(?,?)",
                new Object[]{todayDate(), t}
        );
    }

    ArrayList<String[]> announcements() {

        ArrayList<String[]> a = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT date,text FROM announcements ORDER BY date DESC",
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

    // =========================================================
    // المستخدمون
    // =========================================================

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
                "(name,role,permissions,username,password_hash,grade,classroom) " +
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
                "FROM students s INNER JOIN user_students u " +
                "ON u.student_id=s.id WHERE u.username=?",
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

    // =========================================================
    // التشفير
    // =========================================================

    static String sha256(String x) {

        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] b = md.digest(
                    x.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder s = new StringBuilder();

            for (byte v : b) {
                s.append(String.format(Locale.US, "%02x", v));
            }

            return s.toString();

        } catch (Exception e) {
            return x;
        }
    }
}
