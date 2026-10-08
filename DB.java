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

    @Override
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

    @Override
    public void onUpgrade(
            android.database.sqlite.SQLiteDatabase d,
            int oldVersion,
            int newVersion) {

        /*
         * لا توجد تغييرات بنيوية مطلوبة في الجداول الحالية.
         * جدول الحضور يحتوي أصلاً على التاريخ،
         * لذلك يمكن تسجيل أي يوم من الشهر مباشرة.
         */
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

    android.database.sqlite.SQLiteDatabase d =
            getWritableDatabase();

    d.execSQL(
            "INSERT OR REPLACE INTO students " +
            "(name,id,grade,classroom,parent,phone) " +
            "VALUES(?,?,?,?,?,?)",
            new Object[]{
                    n,
                    id,
                    g,
                    cl,
                    p,
                    ph
            }
    );

    // إضافة الصف والشعبة تلقائياً إلى جدول classes
    if (g != null && !g.trim().isEmpty()
            && cl != null && !cl.trim().isEmpty()) {

        d.execSQL(
                "INSERT OR IGNORE INTO classes " +
                "(grade,classroom) VALUES(?,?)",
                new Object[]{
                        g.trim(),
                        cl.trim()
                }
        );
    }
}

   ArrayList<String[]> students(String q) {

    ArrayList<String[]> a = new ArrayList<>();

    if (q == null) q = "";
    q = q.trim();

    Cursor c = getReadableDatabase().rawQuery(
            "SELECT name,id,grade,classroom " +
            "FROM students " +
            "WHERE name LIKE ? OR id LIKE ? " +
            "ORDER BY name",
            new String[]{
                    "%" + q + "%",
                    "%" + q + "%"
            }
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
    // المعلمون
    // =========================================================

    void addTeacher(
            String n,
            String s,
            String r) {

        getWritableDatabase().execSQL(
                "INSERT INTO teachers VALUES(?,?,?)",
                new Object[]{
                        n,
                        s,
                        r
                }
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

    void addClass(
            String g,
            String c) {

        getWritableDatabase().execSQL(
                "INSERT OR IGNORE INTO classes " +
                "VALUES(?,?)",
                new Object[]{
                        g,
                        c
                }
        );
    }

    ArrayList<String[]> classes() {

        ArrayList<String[]> a = new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT c.grade,c.classroom," +
                "(SELECT COUNT(*) " +
                "FROM students s " +
                "WHERE s.grade=c.grade " +
                "AND s.classroom=c.classroom) " +
                "FROM classes c " +
                "ORDER BY c.grade,c.classroom",
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
    // التاريخ
    // =========================================================

    String todayDate() {

        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(new Date());
    }

    String formatDate(
            Calendar cal) {

        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(cal.getTime());
    }

    // =========================================================
    // الحضور - اليوم الحالي
    // =========================================================

    String attendanceStatus(String id) {

        return attendanceStatus(
                id,
                todayDate()
        );
    }

    // =========================================================
    // الحضور - تاريخ محدد
    // =========================================================

    String attendanceStatus(
            String id,
            String date) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT status FROM attendance " +
                "WHERE student_id=? AND date=?",
                new String[]{
                        id,
                        date
                }
        );

        String s = "";

        if (c.moveToFirst()) {
            s = c.getString(0);
        }

        c.close();

        return s;
    }

    // =========================================================
    // حفظ حضور اليوم
    // =========================================================

    void setAttendance(
            String id,
            String st) {

        setAttendance(
                id,
                todayDate(),
                st
        );
    }

    // =========================================================
    // حفظ حضور بتاريخ محدد
    // =========================================================

    void setAttendance(
            String id,
            String date,
            String st) {

        if (id == null || id.trim().isEmpty()) {
            return;
        }

        if (date == null || date.trim().isEmpty()) {
            return;
        }

        if (st == null || st.trim().isEmpty()) {
            return;
        }

        getWritableDatabase().execSQL(
                "INSERT OR REPLACE INTO attendance " +
                "(student_id,date,status) " +
                "VALUES(?,?,?)",
                new Object[]{
                        id,
                        date,
                        st
                }
        );
    }

    // =========================================================
    // حذف تسجيل حضور ليوم معين
    // =========================================================

    void clearAttendance(
            String id,
            String date) {

        getWritableDatabase().execSQL(
                "DELETE FROM attendance " +
                "WHERE student_id=? AND date=?",
                new Object[]{
                        id,
                        date
                }
        );
    }

    // =========================================================
    // إحصائيات حضور اليوم
    // =========================================================

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

        return dateCount(
                todayDate(),
                st
        );
    }

    int dateCount(
            String date,
            String st) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) " +
                "FROM attendance " +
                "WHERE date=? AND status=?",
                new String[]{
                        date,
                        st
                }
        );

        c.moveToFirst();

        int n = c.getInt(0);

        c.close();

        return n;
    }

    // =========================================================
    // الحضور الشهري لطالب
    // =========================================================

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
                "SELECT COUNT(*) " +
                "FROM attendance " +
                "WHERE student_id=? " +
                "AND date LIKE ? " +
                "AND status=?",
                new String[]{
                        id,
                        prefix + "%",
                        status
                }
        );

        c.moveToFirst();

        int n = c.getInt(0);

        c.close();

        return n;
    }

    // =========================================================
    // نسبة حضور الطالب في شهر
    // =========================================================

    double monthlyAttendancePercentage(
            String id,
            int year,
            int month) {

        int present =
                monthlyAttendanceCount(
                        id,
                        year,
                        month,
                        "حاضر"
                );

        int absent =
                monthlyAttendanceCount(
                        id,
                        year,
                        month,
                        "غائب"
                );

        int late =
                monthlyAttendanceCount(
                        id,
                        year,
                        month,
                        "متأخر"
                );

        int excused =
                monthlyAttendanceCount(
                        id,
                        year,
                        month,
                        "بعذر"
                );

        int total =
                present +
                absent +
                late +
                excused;

        if (total <= 0) {
            return 0;
        }

        return present * 100.0 / total;
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
                        id,
                        sub,
                        sem,
                        mon,
                        r1,
                        r2,
                        r3,
                        r4
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
                "WHERE student_id=? " +
                "AND subject=? " +
                "AND semester=? " +
                "AND month=?",
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

    // =========================================================
    // مجموع درجة الشهر من 100
    // =========================================================

    double monthlyTotal(
            String id,
            String sub,
            int sem,
            int mon) {

        double[] v =
                monthlyValues(
                        id,
                        sub,
                        sem,
                        mon
                );

        if (v == null) {
            return 0;
        }

        return v[0] +
                v[1] +
                v[2] +
                v[3];
    }

    // =========================================================
    // الاختبار
    // =========================================================

    void setExam(
            String id,
            String sub,
            int sem,
            double sc) {

        getWritableDatabase().execSQL(
                "INSERT OR REPLACE INTO exams " +
                "(student_id,subject,semester,score) " +
                "VALUES(?,?,?,?)",
                new Object[]{
                        id,
                        sub,
                        sem,
                        sc
                }
        );
    }

    double exam(
            String id,
            String sub,
            int sem) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT score FROM exams " +
                "WHERE student_id=? " +
                "AND subject=? " +
                "AND semester=?",
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

    // =========================================================
    // المواد
    // =========================================================

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

            double[] v =
                    monthlyValues(
                            id,
                            sub,
                            semester,
                            m
                    );

            if (v != null) {

                sum +=
                        v[0] +
                        v[1] +
                        v[2] +
                        v[3];

                count++;
            }
        }

        if (count == 0) {
            return 0;
        }

        return sum / count;
    }

    // =========================================================
    // المحصلة الفصلية للمادة من 50
    //
    // متوسط الأشهر الثلاثة /100
    // يحول إلى /20
    //
    // الاختبار /30
    //
    // المجموع = /50
    // =========================================================

    double semesterSubjectResult(
            String id,
            String sub,
            int semester) {

        double monthly =
                semesterMonthlyAverage(
                        id,
                        sub,
                        semester
                );

        double monthly20 =
                monthly * 20.0 / 100.0;

        double exam30 =
                exam(
                        id,
                        sub,
                        semester
                );

        return monthly20 + exam30;
    }

    // =========================================================
    // النتيجة العامة للطالب
    //
    // mode 0 = الفصل الأول /300
    // mode 1 = الفصل الثاني /300
    // mode 2 = السنوي /600
    //
    // لأن عدد المواد = 6
    // المادة في الفصل = 50
    // المادة سنوياً = 100
    // =========================================================

    String resultLine(
            String id,
            int mode) {

        double total = 0;

        String[] subs = subjects();

        for (String sub : subs) {

            if (mode == 0) {

                total +=
                        semesterSubjectResult(
                                id,
                                sub,
                                1
                        );

            } else if (mode == 1) {

                total +=
                        semesterSubjectResult(
                                id,
                                sub,
                                2
                        );

            } else if (mode == 2) {

                total +=
                        semesterSubjectResult(
                                id,
                                sub,
                                1
                        );

                total +=
                        semesterSubjectResult(
                                id,
                                sub,
                                2
                        );
            }
        }

        double max;

        if (mode == 2) {
            max = subs.length * 100.0;
        } else {
            max = subs.length * 50.0;
        }

        double pct =
                max > 0
                        ? total * 100.0 / max
                        : 0;

        String taq =
                pct >= 90
                        ? "ممتاز"
                        : pct >= 80
                        ? "جيد جدا"
                        : pct >= 65
                        ? "جيد"
                        : pct >= 50
                        ? "مقبول"
                        : "ضعيف";

        String result =
                pct >= 50
                        ? "ناجح"
                        : "راسب";

        return String.format(
                Locale.US,
                "المجموع: %.1f / %.0f - %.1f%% - %s - %s",
                total,
                max,
                pct,
                taq,
                result
        );
    }

    // =========================================================
    // نتيجة مادة واحدة
    // =========================================================

    double subjectResult(
            String id,
            String sub,
            int semester) {

        return semesterSubjectResult(
                id,
                sub,
                semester
        );
    }

    // =========================================================
    // الحضور العام للطالب
    // =========================================================

    String studentAttendanceSummary(
            String id) {

        int h = countAttendanceStatus(
                id,
                "حاضر"
        );

        int g = countAttendanceStatus(
                id,
                "غائب"
        );

        int l = countAttendanceStatus(
                id,
                "متأخر"
        );

        int b = countAttendanceStatus(
                id,
                "بعذر"
        );

        return "حاضر: " + h +
                " | غائب: " + g +
                " | متأخر: " + l +
                " | بعذر: " + b;
    }

    int countAttendanceStatus(
            String id,
            String status) {

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) " +
                "FROM attendance " +
                "WHERE student_id=? " +
                "AND status=?",
                new String[]{
                        id,
                        status
                }
        );

        c.moveToFirst();

        int n = c.getInt(0);

        c.close();

        return n;
    }

    // =========================================================
    // جدول المدرسة
    // =========================================================

    String daySchedule(String day) {

        return "جدول " +
                day +
                " محفوظ محلياً";
    }

    // =========================================================
    // الإعلانات
    // =========================================================

    void addAnnouncement(String t) {

        getWritableDatabase().execSQL(
                "INSERT INTO announcements " +
                "(date,text) VALUES(?,?)",
                new Object[]{
                        todayDate(),
                        t
                }
        );
    }

    ArrayList<String[]> announcements() {

        ArrayList<String[]> a =
                new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT date,text " +
                "FROM announcements " +
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

        ArrayList<String[]> a =
                announcements();

        return a.isEmpty()
                ? "لا توجد إعلانات"
                : a.get(0)[1];
    }

    // =========================================================
    // المستخدمون
    // =========================================================

    ArrayList<String[]> users() {

        ArrayList<String[]> a =
                new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT name,role,permissions,username " +
                "FROM users",
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
                "(name,role,permissions,username," +
                "password_hash,grade,classroom) " +
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

    ArrayList<String[]> linkedStudents(
            String username) {

        ArrayList<String[]> a =
                new ArrayList<>();

        Cursor c = getReadableDatabase().rawQuery(
                "SELECT s.name,s.id,s.grade,s.classroom " +
                "FROM students s " +
                "INNER JOIN user_students u " +
                "ON u.student_id=s.id " +
                "WHERE u.username=?",
                new String[]{
                        username
                }
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

            MessageDigest md =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] b =
                    md.digest(
                            x.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder s =
                    new StringBuilder();

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
