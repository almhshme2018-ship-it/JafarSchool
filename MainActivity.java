package com.jafar.school;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.text.*;
import android.text.InputType;

import androidx.core.content.FileProvider;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    static final int REQ_BACKUP = 7101;

    LinearLayout root;
    LinearLayout content;

    DB db;

    String role = "القائم بأعمال المدير";
    String currentUser = "";
    String currentUsername = "";
    String assignedGrade = "";
    String assignedClass = "";

    final int BLUE = Color.rgb(17, 96, 177);
    final int BG = Color.rgb(244, 247, 251);
    final int WHITE = Color.WHITE;
    final int TEXT = Color.rgb(25, 45, 68);
    final int GREEN = Color.rgb(30, 130, 76);
    final int RED = Color.rgb(190, 50, 50);
    final int ORANGE = Color.rgb(220, 130, 30);
    final int GRAY = Color.rgb(100, 105, 112);

    final String[] DAYS = {
            "السبت",
            "الأحد",
            "الاثنين",
            "الثلاثاء",
            "الأربعاء"
    };

    final Stack<Runnable> navHistory = new Stack<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(BLUE);
        getWindow().setNavigationBarColor(Color.BLACK);

        getWindow().getDecorView().setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        db = new DB(this);

        showLogin();

        try {
            getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // الرجوع
    // =========================================================

    @Override
    public void onBackPressed() {

        if (navHistory.size() > 1) {

            navHistory.pop();

            Runnable previous = navHistory.peek();

            if (previous != null) {
                previous.run();
            }

            return;
        }

        if (navHistory.size() == 1) {

            Toast.makeText(
                    this,
                    "أنت في الصفحة الرئيسية",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        super.onBackPressed();
    }

    void goBack() {
        onBackPressed();
    }

    // =========================================================
    // عناصر الواجهة
    // =========================================================

    TextView tv(
            String s,
            int sp,
            boolean bold) {

        TextView t = new TextView(this);

        t.setText(s == null ? "" : s);
        t.setTextSize(sp);
        t.setTextColor(TEXT);

        t.setTypeface(
                null,
                bold ? Typeface.BOLD : Typeface.NORMAL
        );

        t.setGravity(
                Gravity.CENTER_VERTICAL |
                Gravity.RIGHT
        );

        t.setIncludeFontPadding(true);
        t.setPadding(14, 10, 14, 10);

        t.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        return t;
    }

    Button btn(String s) {

        Button b = new Button(this);

        b.setText(s == null ? "" : s);
        b.setTextSize(14);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setSingleLine(false);
        b.setMaxLines(3);
        b.setPadding(12, 8, 12, 8);

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(BLUE);
        g.setCornerRadius(18);

        b.setBackground(g);

        b.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        b.setClickable(true);
        b.setEnabled(true);

        return b;
    }

    Button smallBtn(
            String text,
            int color) {

        Button b = new Button(this);

        b.setText(text);
        b.setTextSize(12);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setSingleLine(false);
        b.setGravity(Gravity.CENTER);
        b.setPadding(4, 2, 4, 2);

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(color);
        g.setCornerRadius(14);

        b.setBackground(g);

        b.setClickable(true);
        b.setEnabled(true);
        b.setFocusable(false);

        return b;
    }

    void base() {

        root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

        root.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        root.setGravity(Gravity.FILL);

        setContentView(root);

        try {
            getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        } catch (Exception ignored) {
        }
    }

    void navigateTo(Runnable screen) {

        if (screen == null)
            return;

        navHistory.push(screen);

        screen.run();
    }

    // =========================================================
    // الصفحة الداخلية
    // =========================================================

    void page(String name) {

        base();

        LinearLayout top =
                new LinearLayout(this);

        top.setOrientation(
                LinearLayout.HORIZONTAL
        );

        top.setGravity(
                Gravity.CENTER_VERTICAL
        );

        top.setPadding(
                8, 6, 8, 6
        );

        top.setBackgroundColor(WHITE);

        // زر الرجوع
        Button back =
                smallBtn(
                        "‹ رجوع",
                        BLUE
                );

        back.setTextSize(13);
        back.setGravity(Gravity.CENTER);
        back.setPadding(2, 2, 2, 2);
        back.setMinHeight(58);

        LinearLayout.LayoutParams backParams =
                new LinearLayout.LayoutParams(
                        88,
                        60
                );

        backParams.setMargins(
                4, 0, 8, 0
        );

        top.addView(
                back,
                backParams
        );

        back.setOnClickListener(
                v -> goBack()
        );

        TextView title =
                tv(name, 18, true);

        title.setGravity(Gravity.CENTER);
        title.setTextAlignment(
                View.TEXT_ALIGNMENT_CENTER
        );

        top.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        64,
                        1
                )
        );

        root.addView(
                top,
                new LinearLayout.LayoutParams(
                        -1,
                        76
                )
        );

        content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                12, 8, 12, 35
        );

        content.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);

        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );
    }

    void addCard(
            String title,
            String body) {

        LinearLayout c =
                new LinearLayout(this);

        c.setOrientation(
                LinearLayout.VERTICAL
        );

        c.setPadding(
                16, 14, 16, 14
        );

        c.setBackgroundColor(WHITE);

        TextView a =
                tv(title, 16, true);

        TextView b =
                tv(body, 14, false);

        b.setLineSpacing(
                3,
                1.0f
        );

        c.addView(
                a,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        c.addView(
                b,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        p.setMargins(
                3, 7, 3, 7
        );

        content.addView(c, p);
    }

    // =========================================================
    // تسجيل الدخول
    // =========================================================

    void showLogin() {

        navHistory.clear();

        base();

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(
                Gravity.CENTER
        );

        box.setPadding(
                28, 35, 28, 35
        );

        scroll.addView(box);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        TextView logo =
                tv("🏫", 60, true);

        logo.setGravity(Gravity.CENTER);

        box.addView(
                logo,
                new LinearLayout.LayoutParams(
                        -1,
                        90
                )
        );

        TextView h =
                tv(
                        "مدرسة جعفر بن أبي طالب",
                        23,
                        true
                );

        h.setGravity(Gravity.CENTER);

        box.addView(h);

        TextView sub =
                tv(
                        "الجمهورية اليمنية - إب - مذيخرة - الأشعوب\n" +
                        "نظام الإدارة الذكي - يعمل بدون إنترنت",
                        14,
                        false
                );

        sub.setGravity(Gravity.CENTER);

        box.addView(
                sub,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        TextView loginTitle =
                tv(
                        "تسجيل الدخول",
                        19,
                        true
                );

        loginTitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams lt =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        lt.setMargins(
                0, 20, 0, 8
        );

        box.addView(
                loginTitle,
                lt
        );

        EditText user =
                new EditText(this);

        user.setHint("اسم المستخدم");
        user.setTextSize(16);
        user.setSingleLine(true);
        user.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        box.addView(
                user,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        EditText pass =
                new EditText(this);

        pass.setHint("كلمة المرور");
        pass.setTextSize(16);
        pass.setSingleLine(true);
        pass.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        pass.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        box.addView(
                pass,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        Button go =
                btn("دخول");

        LinearLayout.LayoutParams gp =
                new LinearLayout.LayoutParams(
                        -1,
                        62
                );

        gp.setMargins(
                0, 14, 0, 5
        );

        box.addView(go, gp);

        TextView demo =
                tv(
                        "المستخدم الافتراضي: admin / 1234",
                        12,
                        false
                );

        demo.setGravity(Gravity.CENTER);

        box.addView(demo);

        go.setOnClickListener(v -> {

            String[] a =
                    db.authenticate(
                            user.getText()
                                    .toString()
                                    .trim(),
                            pass.getText()
                                    .toString()
                    );

            if (a == null) {

                Toast.makeText(
                        this,
                        "بيانات الدخول غير صحيحة",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            currentUser = a[0];
            role = a[1];
            assignedGrade = a[2];
            assignedClass = a[3];
            currentUsername = a[4];

            navigateTo(
                    role.equals("طالب") ||
                    role.equals("ولي أمر")
                            ? this::showPortal
                            : this::showHome
            );
        });
    }

    // =========================================================
    // الصلاحيات
    // =========================================================

    boolean admin() {

        return role.equals("القائم بأعمال المدير") ||
                role.equals("مدير المدرسة") ||
                role.equals("مدير النظام");
    }

    boolean assignedTeacher() {

        return role.equals("معلم") &&
                !assignedGrade.isEmpty() &&
                !assignedClass.isEmpty();
    }

    // =========================================================
    // الرئيسية
    // =========================================================

    void showHome() {

        base();

        LinearLayout head =
                new LinearLayout(this);

        head.setGravity(
                Gravity.CENTER_VERTICAL
        );

        head.setPadding(
                10, 10, 10, 5
        );

        TextView title =
                tv(
                        "🏫 مدرسة جعفر\n" +
                        role +
                        " : " +
                        currentUser,
                        16,
                        true
                );

        head.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        Button out =
                btn("خروج");

        head.addView(
                out,
                new LinearLayout.LayoutParams(
                        95,
                        58
                )
        );

        out.setOnClickListener(
                v -> showLogin()
        );

        root.addView(head);

        ScrollView sv =
                new ScrollView(this);

        sv.setFillViewport(true);

        content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                12, 8, 12, 35
        );

        sv.addView(content);

        root.addView(
                sv,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        addStats();
        addGrid();

        addCard(
                "📢 آخر الإعلانات",
                db.latestAnnouncements()
        );
    }

    void addStats() {

        LinearLayout r =
                new LinearLayout(this);

        r.setOrientation(
                LinearLayout.HORIZONTAL
        );

        String[] a = {
                "👨‍🎓\nالطلاب\n" +
                db.count("students"),

                "👨‍🏫\nالمعلمون\n" +
                db.count("teachers"),

                "🟢\nحاضر\n" +
                db.todayPresent(),

                "🔴\nغائب\n" +
                db.todayAbsent()
        };

        for (String x : a) {

            TextView t =
                    tv(x, 13, true);

            t.setGravity(Gravity.CENTER);

            t.setTextAlignment(
                    View.TEXT_ALIGNMENT_CENTER
            );

            t.setBackgroundColor(WHITE);

            t.setMinHeight(82);

            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(
                            0,
                            82,
                            1
                    );

            p.setMargins(
                    3, 3, 3, 8
            );

            r.addView(t, p);
        }

        content.addView(
                r,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );
    }

    void addGrid() {

        ArrayList<String[]> list =
                new ArrayList<>();

        list.add(
                new String[]{
                        "👨‍🎓 الطلاب",
                        "students"
                }
        );

        list.add(
                new String[]{
                        "📊 الدرجات",
                        "grades"
                }
        );

        list.add(
                new String[]{
                        "✅ الحضور",
                        "attendance"
                }
        );

        list.add(
                new String[]{
                        "📅 الجدول",
                        "timetable"
                }
        );

        if (admin()) {

            list.add(
                    new String[]{
                            "👨‍🏫 المعلمون",
                            "teachers"
                    }
            );

            list.add(
                    new String[]{
                            "🏫 الصفوف",
                            "classes"
                    }
            );

            list.add(
                    new String[]{
                            "📈 التقارير",
                            "reports"
                    }
            );

            list.add(
                    new String[]{
                            "🔔 الإعلانات",
                            "announcements"
                    }
            );

            list.add(
                    new String[]{
                            "🔐 المستخدمون",
                            "users"
                    }
            );

            list.add(
                    new String[]{
                            "📁 نسخ احتياطي",
                            "files"
                    }
            );
        }

        LinearLayout row = null;

        int i = 0;

        for (String[] item : list) {

            if (i % 2 == 0) {

                row =
                        new LinearLayout(this);

                row.setOrientation(
                        LinearLayout.HORIZONTAL
                );

                row.setGravity(
                        Gravity.CENTER
                );

                content.addView(
                        row,
                        new LinearLayout.LayoutParams(
                                -1,
                                88
                        )
                );
            }

            Button b =
                    btn(item[0]);

            LinearLayout.LayoutParams bp =
                    new LinearLayout.LayoutParams(
                            0,
                            76,
                            1
                    );

            bp.setMargins(
                    4, 4, 4, 4
            );

            row.addView(b, bp);

            String key = item[1];

            b.setOnClickListener(
                    v -> open(key)
            );

            i++;
        }
    }

    void open(String k) {

        if (k.equals("students"))
            navigateTo(this::students);

        else if (k.equals("grades"))
            navigateTo(this::grades);

        else if (k.equals("attendance"))
            navigateTo(this::attendance);

        else if (k.equals("timetable"))
            navigateTo(this::timetable);

        else if (k.equals("teachers"))
            navigateTo(this::teachers);

        else if (k.equals("classes"))
            navigateTo(this::classes);

        else if (k.equals("reports"))
            navigateTo(this::reports);

        else if (k.equals("announcements"))
            navigateTo(this::announcements);

        else if (k.equals("users"))
            navigateTo(this::users);

        else if (k.equals("files"))
            navigateTo(this::files);
    }

    // =========================================================
    // الطلاب
    // =========================================================

    void students() {

        page("👨‍🎓 الطلاب والطالبات");

        EditText search =
                new EditText(this);

        search.setHint(
                "بحث باسم الطالب أو الرقم..."
        );

        search.setTextSize(16);
        search.setSingleLine(true);

        content.addView(
                search,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        if (admin()) {

            Button add =
                    btn("＋ إضافة طالب / طالبة");

            content.addView(
                    add,
                    new LinearLayout.LayoutParams(
                            -1,
                            64
                    )
            );

            add.setOnClickListener(
                    v -> studentDialog(null)
            );
        }

        LinearLayout list =
                new LinearLayout(this);

        list.setOrientation(
                LinearLayout.VERTICAL
        );

        content.addView(list);

        Runnable refresh = () -> {

            list.removeAllViews();

            String q =
                    search.getText()
                            .toString()
                            .trim();

            if (admin()) {

                for (String[] r :
                        db.students(q)) {

                    addStudentRow(
                            list,
                            r
                    );
                }

            } else if (assignedTeacher()) {

                for (String[] r :
                        db.studentsInClass(
                                assignedGrade,
                                assignedClass
                        )) {

                    if (q.isEmpty() ||
                            r[0].contains(q) ||
                            r[1].contains(q)) {

                        addStudentRow(
                                list,
                                new String[]{
                                        r[0],
                                        r[1],
                                        assignedGrade,
                                        assignedClass
                                }
                        );
                    }
                }
            }

            if (list.getChildCount() == 0) {

                TextView empty =
                        tv(
                                "لا توجد بيانات للعرض",
                                16,
                                false
                        );

                empty.setGravity(
                        Gravity.CENTER
                );

                list.addView(
                        empty,
                        new LinearLayout.LayoutParams(
                                -1,
                                90
                        )
                );
            }
        };

        search.addTextChangedListener(
                new TextWatcher() {

                    public void beforeTextChanged(
                            CharSequence s,
                            int st,
                            int c,
                            int a) {
                    }

                    public void onTextChanged(
                            CharSequence s,
                            int st,
                            int before,
                            int count) {

                        refresh.run();
                    }

                    public void afterTextChanged(
                            Editable e) {
                    }
                }
        );

        refresh.run();
    }

    void addStudentRow(
            LinearLayout list,
            String[] r) {

        Button b =
                btn(
                        "👨‍🎓 " + r[0] +
                        "\nالرقم: " + r[1] +
                        "    الصف: " +
                        r[2] + " / " + r[3]
                );

        b.setGravity(
                Gravity.CENTER_VERTICAL |
                Gravity.RIGHT
        );

        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(
                        -1,
                        82
                );

        bp.setMargins(
                0, 4, 0, 4
        );

        list.addView(b, bp);

        b.setOnClickListener(
                v -> navigateTo(
                        () -> studentDetails(r[1])
                )
        );
    }

    // =========================================================
    // إضافة / تعديل طالب
    // =========================================================

    void studentDialog(String id) {

        boolean edit =
                id != null;

        String[] old =
                edit
                        ? db.student(id)
                        : null;

        EditText n =
                new EditText(this);

        EditText sid =
                new EditText(this);

        EditText g =
                new EditText(this);

        EditText cl =
                new EditText(this);

        EditText pa =
                new EditText(this);

        EditText ph =
                new EditText(this);

        n.setHint("اسم الطالب الرباعي");
        sid.setHint("رقم الطالب");
        g.setHint("الصف");
        cl.setHint("الشعبة");
        pa.setHint("ولي الأمر");
        ph.setHint("الهاتف");

        if (old != null) {

            n.setText(old[0]);
            sid.setText(old[1]);
            sid.setEnabled(false);
            g.setText(old[2]);
            cl.setText(old[3]);
            pa.setText(old[4]);
            ph.setText(old[5]);
        }

        LinearLayout l =
                new LinearLayout(this);

        l.setOrientation(
                LinearLayout.VERTICAL
        );

        l.setPadding(
                8, 5, 8, 5
        );

        EditText[] fields = {
                n, sid, g, cl, pa, ph
        };

        for (EditText e : fields) {

            l.addView(
                    e,
                    new LinearLayout.LayoutParams(
                            -1,
                            58
                    )
            );
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        edit
                                ? "تعديل الطالب"
                                : "إضافة طالب"
                )
                .setView(l)
                .setPositiveButton(
                        "حفظ",
                        (d, w) -> {

                            db.saveStudent(
                                    n.getText()
                                            .toString()
                                            .trim(),
                                    sid.getText()
                                            .toString()
                                            .trim(),
                                    g.getText()
                                            .toString()
                                            .trim(),
                                    cl.getText()
                                            .toString()
                                            .trim(),
                                    pa.getText()
                                            .toString()
                                            .trim(),
                                    ph.getText()
                                            .toString()
                                            .trim()
                            );

                            students();
                        }
                )
                .setNegativeButton(
                        "إلغاء",
                        null
                )
                .show();
    }

    // =========================================================
    // ملف الطالب
    // =========================================================

    void studentDetails(String id) {

        page("👨‍🎓 ملف الطالب");

        String[] r =
                db.student(id);

        if (r == null) {

            addCard(
                    "خطأ",
                    "لم يتم العثور على الطالب"
            );

            return;
        }

        addCard(
                "👨‍🎓 " + r[0],
                "الرقم: " + r[1] +
                "\nالصف: " + r[2] +
                " - " + r[3] +
                "\nولي الأمر: " + r[4] +
                "\nالهاتف: " + r[5]
        );

        addCard(
                "📊 الفصل الأول",
                db.resultLine(id, 0)
        );

        addCard(
                "📊 الفصل الثاني",
                db.resultLine(id, 1)
        );

        addCard(
                "🏆 النتيجة السنوية",
                db.resultLine(id, 2)
        );

        addCard(
                "📅 الحضور",
                db.studentAttendanceSummary(id)
        );

        Button pdf =
                btn("📄 طباعة ملف الطالب PDF");

        content.addView(
                pdf,
                new LinearLayout.LayoutParams(
                        -1,
                        64
                )
        );

        pdf.setOnClickListener(
                v -> generateStudentPdf(id)
        );
    }

    // =========================================================
    // الدرجات
    // =========================================================

    void grades() {

        page("📊 الدرجات");

        addCard(
                "طريقة إدخال الدرجات",
                "اختر الصف والشعبة، ثم الشهر. " +
                "ستظهر جميع الطلاب وجميع المواد.\n" +
                "اضغط على خانة المادة لإدخال:\n" +
                "مواظبة 20 + شفهي 20 + واجب 20 + تحريري 40."
        );

        Button e =
                btn("✏️ إدخال درجات الطلاب");

        content.addView(
                e,
                new LinearLayout.LayoutParams(
                        -1,
                        68
                )
        );

        e.setOnClickListener(
                v -> chooseClassForGrades()
        );
    }

    ArrayList<String[]> allowedClasses() {

        ArrayList<String[]> all =
                db.classes();

        if (admin())
            return all;

        ArrayList<String[]> result =
                new ArrayList<>();

        if (assignedTeacher()) {

            for (String[] c : all) {

                if (c[0].equals(assignedGrade) &&
                        c[1].equals(assignedClass)) {

                    result.add(c);
                }
            }
        }

        return result;
    }

    void chooseClassForGrades() {

        ArrayList<String[]> cs =
                allowedClasses();

        if (cs.isEmpty()) {

            addCard(
                    "لا توجد صفوف",
                    "لا يوجد صف أو شعبة مسموح لك بإدارتها."
            );

            return;
        }

        String[] it =
                new String[cs.size()];

        for (int i = 0; i < cs.size(); i++) {

            it[i] =
                    "الصف " +
                    cs.get(i)[0] +
                    " - الشعبة " +
                    cs.get(i)[1];
        }

        new AlertDialog.Builder(this)
                .setTitle("اختر الصف والشعبة")
                .setItems(
                        it,
                        (d, w) ->
                                navigateTo(
                                        () ->
                                                classGrades(
                                                        cs.get(w)[0],
                                                        cs.get(w)[1]
                                                )
                                )
                )
                .show();
    }

    // =========================================================
    // شاشة درجات الصف
    // =========================================================

    void classGrades(
            String grade,
            String classroom) {

        page(
                "📊 درجات " +
                grade +
                " / " +
                classroom
        );

        Spinner sem =
                new Spinner(this);

        sem.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        new String[]{
                                "الفصل الأول",
                                "الفصل الثاني"
                        }
                )
        );

        Spinner month =
                new Spinner(this);

        month.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        new String[]{
                                "الشهر الأول",
                                "الشهر الثاني",
                                "الشهر الثالث"
                        }
                )
        );

        content.addView(
                sem,
                new LinearLayout.LayoutParams(
                        -1,
                        58
                )
        );

        content.addView(
                month,
                new LinearLayout.LayoutParams(
                        -1,
                        58
                )
        );

        TextView info =
                tv(
                        "⬅ اسحب الجدول أفقيًا — واضغط على خانة المادة لإدخال الدرجة",
                        13,
                        true
                );

        info.setGravity(Gravity.CENTER);

        content.addView(
                info,
                new LinearLayout.LayoutParams(
                        -1,
                        52
                )
        );

        // =====================================================
        // مهم جدًا:
        // لا نستخدم weight=1 هنا.
        // لأن HorizontalScrollView موجود داخل ScrollView.
        // =====================================================

        HorizontalScrollView hsv =
                new HorizontalScrollView(this);

        hsv.setFillViewport(false);
        hsv.setClipToPadding(false);
        hsv.setHorizontalScrollBarEnabled(true);
        hsv.setNestedScrollingEnabled(false);

        LinearLayout table =
                new LinearLayout(this);

        table.setOrientation(
                LinearLayout.VERTICAL
        );

        table.setClickable(false);
        table.setFocusable(false);

        hsv.addView(
                table,
                new HorizontalScrollView.LayoutParams(
                        -2,
                        -2
                )
        );

        // ارتفاع Wrap Content بدل weight=1
        LinearLayout.LayoutParams hsvParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        hsvParams.setMargins(
                0, 5, 0, 10
        );

        content.addView(
                hsv,
                hsvParams
        );

        Runnable render =
                () -> {

                    table.removeAllViews();

                    addGradeHeader(table);

                    ArrayList<String[]> students =
                            db.studentsInClass(
                                    grade,
                                    classroom
                            );

                    int semester =
                            sem.getSelectedItemPosition()
                                    + 1;

                    int mon =
                            month.getSelectedItemPosition()
                                    + 1;

                    for (String[] st :
                            students) {

                        addGradeStudentRow(
                                table,
                                st,
                                semester,
                                mon
                        );
                    }

                    if (students.isEmpty()) {

                        TextView empty =
                                tv(
                                        "لا يوجد طلاب في هذا الصف",
                                        16,
                                        false
                                );

                        empty.setGravity(
                                Gravity.CENTER
                        );

                        table.addView(
                                empty,
                                new LinearLayout.LayoutParams(
                                        900,
                                        80
                                )
                        );
                    }
                };

        sem.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    public void onItemSelected(
                            AdapterView<?> p,
                            View v,
                            int po,
                            long id) {

                        render.run();
                    }

                    public void onNothingSelected(
                            AdapterView<?> p) {
                    }
                }
        );

        month.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    public void onItemSelected(
                            AdapterView<?> p,
                            View v,
                            int po,
                            long id) {

                        render.run();
                    }

                    public void onNothingSelected(
                            AdapterView<?> p) {
                    }
                }
        );

        render.run();

        Button exam =
                btn("📝 إدخال اختبارات الفصل");

        content.addView(
                exam,
                new LinearLayout.LayoutParams(
                        -1,
                        64
                )
        );

        exam.setOnClickListener(
                v ->
                        examEntry(
                                grade,
                                classroom,
                                sem.getSelectedItemPosition()
                                        + 1
                        )
        );
    }

    // =========================================================
    // رأس جدول الدرجات
    // =========================================================

    void addGradeHeader(
            LinearLayout table) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView name =
                tv(
                        "الطالب",
                        13,
                        true
                );

        name.setGravity(Gravity.CENTER);

        row.addView(
                name,
                new LinearLayout.LayoutParams(
                        180,
                        62
                )
        );

        for (String sub :
                db.subjects()) {

            TextView s =
                    tv(
                            sub,
                            11,
                            true
                    );

            s.setGravity(Gravity.CENTER);

            row.addView(
                    s,
                    new LinearLayout.LayoutParams(
                            125,
                            62
                    )
            );
        }

        table.addView(
                row,
                new LinearLayout.LayoutParams(
                        -2,
                        66
                )
        );
    }

    // =========================================================
    // صف طالب في جدول الدرجات
    // =========================================================

    void addGradeStudentRow(
            LinearLayout table,
            String[] st,
            int semester,
            int month) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setClickable(false);
        row.setFocusable(false);

        TextView name =
                tv(
                        st[0] +
                        "\n" +
                        st[1],
                        11,
                        true
                );

        name.setGravity(
                Gravity.CENTER_VERTICAL |
                Gravity.RIGHT
        );

        name.setClickable(false);
        name.setFocusable(false);

        row.addView(
                name,
                new LinearLayout.LayoutParams(
                        180,
                        72
                )
        );

       
