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

        for (String sub :
                db.subjects()) {

            final String sid =
                    st[1];

            final String studentName =
                    st[0];

            final String subject =
                    sub;

            final int semValue =
                    semester;

            final int monthValue =
                    month;

            // =================================================
            // الإصلاح:
            // لا نعتمد على db.monthlyTotal().
            // نحسب المجموع مباشرة من القيم المحفوظة.
            // =================================================

            double[] vals =
                    db.monthlyValues(
                            sid,
                            subject,
                            semValue,
                            monthValue
                    );

            double total = 0;

            if (vals != null &&
                    vals.length >= 4) {

                total =
                        vals[0] +
                        vals[1] +
                        vals[2] +
                        vals[3];
            }

            String label;

            int color;

            if (total == 0) {

                label = "✏ إدخال";
                color = ORANGE;

            } else {

                label =
                        formatNumber(total) +
                        "\n/100";

                color = GREEN;
            }

            Button cell =
                    smallBtn(
                            label,
                            color
                    );

            cell.setTextSize(11);
            cell.setGravity(Gravity.CENTER);
            cell.setClickable(true);
            cell.setEnabled(true);
            cell.setFocusable(false);

            LinearLayout.LayoutParams cp =
                    new LinearLayout.LayoutParams(
                            125,
                            68
                    );

            cp.setMargins(
                    3, 3, 3, 3
            );

            row.addView(
                    cell,
                    cp
            );

            cell.setOnClickListener(
                    v -> {

                        scoreDialog(
                                sid,
                                studentName,
                                subject,
                                semValue,
                                monthValue,
                                cell
                        );
                    }
            );
        }

        table.addView(
                row,
                new LinearLayout.LayoutParams(
                        -2,
                        76
                )
        );
    }

    String formatNumber(
            double x) {

        if (x == Math.round(x))
            return String.valueOf(
                    (int)x
            );

        return String.format(
                Locale.US,
                "%.1f",
                x
        );
    }

    // =========================================================
    // نافذة إدخال عناصر الدرجة
    // =========================================================

    void scoreDialog(
            String studentId,
            String studentName,
            String subject,
            int semester,
            int month,
            Button cell) {

        double[] old =
                db.monthlyValues(
                        studentId,
                        subject,
                        semester,
                        month
                );

        EditText regular =
                scoreEdit("مواظبة /20");

        EditText oral =
                scoreEdit("شفهي /20");

        EditText homework =
                scoreEdit("واجب /20");

        EditText written =
                scoreEdit("تحريري /40");

        if (old != null &&
                old.length >= 4) {

            regular.setText(
                    formatNumber(old[0])
            );

            oral.setText(
                    formatNumber(old[1])
            );

            homework.setText(
                    formatNumber(old[2])
            );

            written.setText(
                    formatNumber(old[3])
            );
        }

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                10, 5, 10, 5
        );

        box.addView(
                regular,
                new LinearLayout.LayoutParams(
                        -1,
                        58
                )
        );

        box.addView(
                oral,
                new LinearLayout.LayoutParams(
                        -1,
                        58
                )
        );

        box.addView(
                homework,
                new LinearLayout.LayoutParams(
                        -1,
                        58
                )
        );

        box.addView(
                written,
                new LinearLayout.LayoutParams(
                        -1,
                        58
                )
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                studentName +
                                "\n" +
                                subject +
                                " - " +
                                (month == 1
                                        ? "الشهر الأول"
                                        : month == 2
                                        ? "الشهر الثاني"
                                        : "الشهر الثالث")
                        )
                        .setView(box)
                        .setPositiveButton(
                                "حفظ",
                                null
                        )
                        .setNegativeButton(
                                "إلغاء",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    Button save =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    save.setOnClickListener(
                            v -> {

                                double a =
                                        limit(
                                                parse(regular),
                                                0,
                                                20
                                        );

                                double o =
                                        limit(
                                                parse(oral),
                                                0,
                                                20
                                        );

                                double h =
                                        limit(
                                                parse(homework),
                                                0,
                                                20
                                        );

                                double wr =
                                        limit(
                                                parse(written),
                                                0,
                                                40
                                        );

                                db.setMonthlyScore(
                                        studentId,
                                        subject,
                                        semester,
                                        month,
                                        a,
                                        o,
                                        h,
                                        wr
                                );

                                double total =
                                        a + o + h + wr;

                                if (total == 0) {

                                    cell.setText(
                                            "✏ إدخال"
                                    );

                                    cell.setBackground(
                                            background(ORANGE)
                                    );

                                } else {

                                    cell.setText(
                                            formatNumber(total) +
                                            "\n/100"
                                    );

                                    cell.setBackground(
                                            background(GREEN)
                                    );
                                }

                                Toast.makeText(
                                        this,
                                        "تم حفظ درجة " +
                                        subject +
                                        " = " +
                                        formatNumber(total) +
                                        " /100",
                                        Toast.LENGTH_SHORT
                                ).show();

                                dialog.dismiss();
                            }
                    );

                    regular.requestFocus();

                    if (dialog.getWindow() != null) {

                        dialog.getWindow()
                                .setSoftInputMode(
                                        WindowManager.LayoutParams
                                                .SOFT_INPUT_STATE_ALWAYS_VISIBLE
                                );
                    }
                }
        );

        dialog.show();
    }

    EditText scoreEdit(
            String hint) {

        EditText e =
                new EditText(this);

        e.setHint(hint);
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setGravity(Gravity.CENTER);
        e.setSelectAllOnFocus(false);

        e.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        e.setClickable(true);
        e.setFocusable(true);
        e.setFocusableInTouchMode(true);
        e.setEnabled(true);

        return e;
    }

    double parse(
            EditText e) {

        try {

            String s =
                    e.getText()
                            .toString()
                            .trim();

            if (s.isEmpty())
                return 0;

            return Double.parseDouble(s);

        } catch (Exception ex) {

            return 0;
        }
    }

    double limit(
            double x,
            double min,
            double max) {

        if (x < min) return min;
        if (x > max) return max;

        return x;
    }

    // =========================================================
    // اختبارات الفصل
    // =========================================================

    void examEntry(
            String grade,
            String classroom,
            int semester) {

        page(
                "📝 اختبارات " +
                grade +
                " / " +
                classroom
        );

        ArrayList<String[]> students =
                db.studentsInClass(
                        grade,
                        classroom
                );

        for (String[] st :
                students) {

            addExamStudent(
                    st,
                    semester
            );
        }

        if (students.isEmpty()) {

            addCard(
                    "لا يوجد طلاب",
                    "لا توجد بيانات في هذا الصف."
            );
        }
    }

    void addExamStudent(
            String[] st,
            int semester) {

        addCard(
                "👨‍🎓 " +
                st[0],
                "الرقم: " +
                st[1]
        );

        for (String subject :
                db.subjects()) {

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            TextView name =
                    tv(
                            subject,
                            12,
                            true
                    );

            name.setGravity(
                    Gravity.CENTER_VERTICAL |
                    Gravity.RIGHT
            );

            row.addView(
                    name,
                    new LinearLayout.LayoutParams(
                            0,
                            55,
                            1
                    )
            );

            EditText e =
                    scoreEdit("الاختبار /30");

            double old =
                    db.exam(
                            st[1],
                            subject,
                            semester
                    );

            if (old != 0)
                e.setText(
                        formatNumber(old)
                );

            row.addView(
                    e,
                    new LinearLayout.LayoutParams(
                            120,
                            55
                    )
            );

            Button save =
                    smallBtn(
                            "حفظ",
                            GREEN
                    );

            row.addView(
                    save,
                    new LinearLayout.LayoutParams(
                            70,
                            52
                    )
            );

            content.addView(
                    row,
                    new LinearLayout.LayoutParams(
                            -1,
                            58
                    )
            );

            save.setOnClickListener(
                    v -> {

                        double x =
                                limit(
                                        parse(e),
                                        0,
                                        30
                                );

                        db.setExam(
                                st[1],
                                subject,
                                semester,
                                x
                        );

                        Toast.makeText(
                                this,
                                "تم حفظ " +
                                subject,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );
        }
    }

    // =========================================================
    // الحضور
    // =========================================================

    void attendance() {

        page("✅ الحضور والغياب");

        addCard(
                "نظام الحضور",
                "اختر الصف ثم الشهر. " +
                "سيظهر جميع الطلاب وجميع أيام الدوام " +
                "من السبت إلى الأربعاء. " +
                "الخميس والجمعة عطلة رسمية."
        );

        Button choose =
                btn("اختر الصف والشعبة");

        content.addView(
                choose,
                new LinearLayout.LayoutParams(
                        -1,
                        64
                )
        );

        choose.setOnClickListener(
                v -> chooseAttendanceClass()
        );
    }

    void chooseAttendanceClass() {

        ArrayList<String[]> cs =
                allowedClasses();

        if (cs.isEmpty()) {

            Toast.makeText(
                    this,
                    "لا توجد صفوف مسموحة",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String[] items =
                new String[cs.size()];

        for (int i = 0; i < cs.size(); i++) {

            items[i] =
                    "الصف " +
                    cs.get(i)[0] +
                    " - الشعبة " +
                    cs.get(i)[1];
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "اختر الصف"
                )
                .setItems(
                        items,
                        (d, w) ->
                                navigateTo(
                                        () ->
                                                attendanceClass(
                                                        cs.get(w)[0],
                                                        cs.get(w)[1]
                                                )
                                )
                )
                .show();
    }

    void attendanceClass(
            String grade,
            String classroom) {

        page(
                "✅ حضور " +
                grade +
                " / " +
                classroom
        );

        Spinner month =
                new Spinner(this);

        month.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        new String[]{
                                "يناير",
                                "فبراير",
                                "مارس",
                                "أبريل",
                                "مايو",
                                "يونيو",
                                "يوليو",
                                "أغسطس",
                                "سبتمبر",
                                "أكتوبر",
                                "نوفمبر",
                                "ديسمبر"
                        }
                )
        );

        content.addView(
                month,
                new LinearLayout.LayoutParams(
                        -1,
                        58
                )
        );

        Spinner year =
                new Spinner(this);

        int currentYear =
                Calendar.getInstance()
                        .get(Calendar.YEAR);

        String[] years = {
                String.valueOf(currentYear - 1),
                String.valueOf(currentYear),
                String.valueOf(currentYear + 1)
        };

        year.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        years
                )
        );

        year.setSelection(1);

        content.addView(
                year,
                new LinearLayout.LayoutParams(
                        -1,
                        58
                )
        );

        TextView info =
                tv(
                        "اضغط على خانة الطالب/اليوم لتحديد الحالة",
                        13,
                        true
                );

        info.setGravity(Gravity.CENTER);

        content.addView(
                info,
                new LinearLayout.LayoutParams(
                        -1,
                        45
                )
        );

        HorizontalScrollView horizontal =
                new HorizontalScrollView(this);

        horizontal.setFillViewport(false);
        horizontal.setNestedScrollingEnabled(false);

        LinearLayout table =
                new LinearLayout(this);

        table.setOrientation(
                LinearLayout.VERTICAL
        );

        horizontal.addView(
                table,
                new HorizontalScrollView.LayoutParams(
                        -2,
                        -2
                )
        );

        content.addView(
                horizontal,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        Runnable render =
                () -> {

                    int m =
                            month.getSelectedItemPosition()
                                    + 1;

                    int y =
                            Integer.parseInt(
                                    year.getSelectedItem()
                                            .toString()
                            );

                    renderAttendanceTable(
                            table,
                            grade,
                            classroom,
                            y,
                            m
                    );
                };

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

        year.setOnItemSelectedListener(
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

        Button pdf =
                btn(
                        "📄 كشف الحضور الشهري PDF"
                );

        content.addView(
                pdf,
                new LinearLayout.LayoutParams(
                        -1,
                        64
                )
        );

        pdf.setOnClickListener(
                v -> {

                    int m =
                            month.getSelectedItemPosition()
                                    + 1;

                    int y =
                            Integer.parseInt(
                                    year.getSelectedItem()
                                            .toString()
                            );

                    generateAttendancePdf(
                            grade,
                            classroom,
                            y,
                            m
                    );
                }
        );
    }

    ArrayList<Calendar> schoolDays(
            int year,
            int month) {

        ArrayList<Calendar> days =
                new ArrayList<>();

        Calendar c =
                Calendar.getInstance();

        c.set(
                year,
                month - 1,
                1,
                0,
                0,
                0
        );

        c.set(
                Calendar.MILLISECOND,
                0
        );

        int max =
                c.getActualMaximum(
                        Calendar.DAY_OF_MONTH
                );

        for (int day = 1;
             day <= max;
             day++) {

            c.set(
                    Calendar.DAY_OF_MONTH,
                    day
            );

            int dow =
                    c.get(
                            Calendar.DAY_OF_WEEK
                    );

            if (dow == Calendar.SATURDAY ||
                    dow == Calendar.SUNDAY ||
                    dow == Calendar.MONDAY ||
                    dow == Calendar.TUESDAY ||
                    dow == Calendar.WEDNESDAY) {

                Calendar copy =
                        (Calendar)c.clone();

                days.add(copy);
            }
        }

        return days;
    }

    void renderAttendanceTable(
            LinearLayout table,
            String grade,
            String classroom,
            int year,
            int month) {

        table.removeAllViews();

        ArrayList<Calendar> days =
                schoolDays(
                        year,
                        month
                );

        ArrayList<String[]> students =
                db.studentsInClass(
                        grade,
                        classroom
                );

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        TextView stHead =
                tv(
                        "الطالب",
                        12,
                        true
                );

        stHead.setGravity(Gravity.CENTER);

        header.addView(
                stHead,
                new LinearLayout.LayoutParams(
                        180,
                        65
                )
        );

        for (Calendar day : days) {

            String label =
                    new SimpleDateFormat(
                            "dd/MM",
                            Locale.US
                    ).format(
                            day.getTime()
                    );

            TextView d =
                    tv(
                            label,
                            10,
                            true
                    );

            d.setGravity(Gravity.CENTER);

            header.addView(
                    d,
                    new LinearLayout.LayoutParams(
                            85,
                            65
                    )
            );
        }

        table.addView(
                header,
                new LinearLayout.LayoutParams(
                        -2,
                        68
                )
        );

        for (String[] st :
                students) {

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

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

            row.addView(
                    name,
                    new LinearLayout.LayoutParams(
                            180,
                            72
                    )
            );

            for (Calendar day :
                    days) {

                String date =
                        new SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.US
                        ).format(
                                day.getTime()
                        );

                String status =
                        db.attendanceStatus(
                                st[1],
                                date
                        );

                Button cell =
                        attendanceButton(
                                status
                        );

                row.addView(
                        cell,
                        new LinearLayout.LayoutParams(
                                85,
                                68
                        )
                );

                String sid = st[1];

                cell.setOnClickListener(
                        v ->
                                attendanceDialog(
                                        sid,
                                        st[0],
                                        date,
                                        cell
                                )
                );
            }

            table.addView(
                    row,
                    new LinearLayout.LayoutParams(
                            -2,
                            74
                    )
            );
        }

        if (students.isEmpty()) {

            TextView empty =
                    tv(
                            "لا يوجد طلاب في هذا الصف",
                            16,
                            false
                    );

            table.addView(
                    empty,
                    new LinearLayout.LayoutParams(
                            500,
                            80
                    )
            );
        }
    }

    Button attendanceButton(
            String status) {

        String text;
        int color;

        if ("حاضر".equals(status)) {

            text = "حاضر";
            color = GREEN;

        } else if ("غائب".equals(status)) {

            text = "غائب";
            color = RED;

        } else if ("متأخر".equals(status)) {

            text = "متأخر";
            color = ORANGE;

        } else if ("بعذر".equals(status)) {

            text = "بعذر";
            color = BLUE;

        } else {

            text = "—";
            color = GRAY;
        }

        return smallBtn(
                text,
                color
        );
    }

    void attendanceDialog(
            String studentId,
            String studentName,
            String date,
            Button cell) {

        String[] options = {
                "لم يسجل",
                "حاضر",
                "غائب",
                "متأخر",
                "بعذر"
        };

        new AlertDialog.Builder(this)
                .setTitle(
                        studentName +
                        "\n" +
                        date
                )
                .setItems(
                        options,
                        (d, which) -> {

                            if (which == 0) {

                                db.clearAttendance(
                                        studentId,
                                        date
                                );

                            } else {

                                db.setAttendance(
                                        studentId,
                                        date,
                                        options[which]
                                );
                            }

                            cell.setText(
                                    options[which]
                            );

                            if (which == 1)
                                cell.setBackground(
                                        background(GREEN)
                                );

                            else if (which == 2)
                                cell.setBackground(
                                        background(RED)
                                );

                            else if (which == 3)
                                cell.setBackground(
                                        background(ORANGE)
                                );

                            else if (which == 4)
                                cell.setBackground(
                                        background(BLUE)
                                );

                            else
                                cell.setBackground(
                                        background(GRAY)
                                );
                        }
                )
                .show();
    }

    GradientDrawable background(
            int color) {

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(color);
        g.setCornerRadius(14);

        return g;
    }

    // =========================================================
    // المعلمون
    // =========================================================

    void teachers() {

        page("👨‍🏫 المعلمون");

        if (admin()) {

            Button add =
                    btn("＋ إضافة معلم");

            content.addView(
                    add,
                    new LinearLayout.LayoutParams(
                            -1,
                            64
                    )
            );

            add.setOnClickListener(
                    v -> {

                        EditText n =
                                new EditText(this);

                        EditText s =
                                new EditText(this);

                        n.setHint("اسم المعلم");
                        s.setHint("المادة");

                        LinearLayout l =
                                new LinearLayout(this);

                        l.setOrientation(
                                LinearLayout.VERTICAL
                        );

                        l.addView(n);
                        l.addView(s);

                        new AlertDialog.Builder(this)
                                .setTitle(
                                        "إضافة معلم"
                                )
                                .setView(l)
                                .setPositiveButton(
                                        "حفظ",
                                        (d, w) -> {

                                            db.addTeacher(
                                                    n.getText()
                                                            .toString(),
                                                    s.getText()
                                                            .toString(),
                                                    "معلم"
                                            );

                                            teachers();
                                        }
                                )
                                .setNegativeButton(
                                        "إلغاء",
                                        null
                                )
                                .show();
                    }
            );
        }

        for (String[] r :
                db.teachers()) {

            addCard(
                    "👨‍🏫 " + r[0],
                    "المادة: " + r[1] +
                    "\nالدور: " + r[2]
            );
        }
    }

    // =========================================================
    // الصفوف
    // =========================================================

    void classes() {

        page("🏫 الصفوف والشعب");

        if (admin()) {

            Button add =
                    btn("＋ إضافة صف / شعبة");

            content.addView(
                    add,
                    new LinearLayout.LayoutParams(
                            -1,
                            64
                    )
            );

            add.setOnClickListener(
                    v -> {

                        EditText g =
                                new EditText(this);

                        EditText c =
                                new EditText(this);

                        g.setHint("الصف");
                        c.setHint("الشعبة");

                        LinearLayout l =
                                new LinearLayout(this);

                        l.setOrientation(
                                LinearLayout.VERTICAL
                        );

                        l.addView(g);
                        l.addView(c);

                        new AlertDialog.Builder(this)
                                .setTitle(
                                        "إضافة صف"
                                )
                                .setView(l)
                                .setPositiveButton(
                                        "حفظ",
                                        (d, w) -> {

                                            db.addClass(
                                                    g.getText()
                                                            .toString()
                                                            .trim(),
                                                    c.getText()
                                                            .toString()
                                                            .trim()
                                            );

                                            classes();
                                        }
                                )
                                .setNegativeButton(
                                        "إلغاء",
                                        null
                                )
                                .show();
                    }
            );
        }

        for (String[] r :
                db.classes()) {

            addCard(
                    "🏫 الصف " +
                    r[0] +
                    " - الشعبة " +
                    r[1],
                    "عدد الطلاب: " +
                    r[2]
            );
        }
    }

    // =========================================================
    // الجدول
    // =========================================================

    void timetable() {

        page("📅 الجدول المدرسي");

        addCard(
                "أيام الدوام",
                "السبت - الأحد - الاثنين - الثلاثاء - الأربعاء"
        );

        addCard(
                "العطلة الرسمية",
                "الخميس والجمعة"
        );

        for (String d : DAYS) {

            addCard(
                    "📅 " + d,
                    db.daySchedule(d)
            );
        }
    }

    // =========================================================
    // التقارير
    // =========================================================

    void reports() {

        page("📈 التقارير والإحصائيات");

        addCard(
                "👨‍🎓 إجمالي الطلاب",
                String.valueOf(
                        db.count("students")
                )
        );

        addCard(
                "👨‍🏫 إجمالي المعلمين",
                String.valueOf(
                        db.count("teachers")
                )
        );

        addCard(
                "🏫 إجمالي الصفوف والشعب",
                String.valueOf(
                        db.count("classes")
                )
        );

        addCard(
                "📅 حضور اليوم",
                "حاضر: " +
                db.todayPresent() +
                "\nغائب: " +
                db.todayAbsent() +
                "\nمتأخر: " +
                db.todayLate() +
                "\nبعذر: " +
                db.todayExcused()
        );
    }

    // =========================================================
    // الإعلانات
    // =========================================================

    void announcements() {

        page("🔔 الإعلانات");

        if (admin()) {

            Button add =
                    btn("＋ إضافة إعلان");

            content.addView(
                    add,
                    new LinearLayout.LayoutParams(
                            -1,
                            64
                    )
            );

            add.setOnClickListener(
                    v -> {

                        EditText e =
                                new EditText(this);

                        e.setHint(
                                "نص الإعلان"
                        );

                        e.setGravity(
                                Gravity.TOP |
                                Gravity.RIGHT
                        );

                        new AlertDialog.Builder(this)
                                .setTitle(
                                        "إضافة إعلان"
                                )
                                .setView(e)
                                .setPositiveButton(
                                        "نشر",
                                        (d, w) -> {

                                            db.addAnnouncement(
                                                    e.getText()
                                                            .toString()
                                            );

                                            announcements();
                                        }
                                )
                                .setNegativeButton(
                                        "إلغاء",
                                        null
                                )
                                .show();
                    }
            );
        }

        for (String[] r :
                db.announcements()) {

            addCard(
                    "📢 " + r[1],
                    "التاريخ: " + r[0]
            );
        }
    }

    // =========================================================
    // المستخدمون
    // =========================================================

    void users() {

        page("🔐 المستخدمون والصلاحيات");

        if (!admin()) {

            addCard(
                    "غير مسموح",
                    "ليس لديك صلاحية إدارة المستخدمين."
            );

            return;
        }

        for (String[] u :
                db.users()) {

            addCard(
                    "👤 " +
                    u[3] +
                    " - " +
                    u[0],
                    "الدور: " +
                    u[1] +
                    "\nالصلاحيات: " +
                    u[2]
            );
        }

        Button add =
                btn("＋ إضافة مستخدم جديد");

        content.addView(
                add,
                new LinearLayout.LayoutParams(
                        -1,
                        64
                )
        );

        add.setOnClickListener(
                v -> {

                    EditText name =
                            new EditText(this);

                    EditText user =
                            new EditText(this);

                    EditText pass =
                            new EditText(this);

                    name.setHint("الاسم");
                    user.setHint("اسم المستخدم");
                    pass.setHint("كلمة المرور");

                    Spinner ro =
                            new Spinner(this);

                    ro.setAdapter(
                            new ArrayAdapter<>(
                                    this,
                                    android.R.layout
                                            .simple_spinner_dropdown_item,
                                    new String[]{
                                            "معلم",
                                            "طالب",
                                            "ولي أمر",
                                            "القائم بأعمال المدير",
                                            "مدير المدرسة",
                                            "مدير النظام"
                                    }
                            )
                    );

                    LinearLayout l =
                            new LinearLayout(this);

                    l.setOrientation(
                            LinearLayout.VERTICAL
                    );

                    l.addView(name);
                    l.addView(user);
                    l.addView(pass);
                    l.addView(ro);

                    new AlertDialog.Builder(this)
                            .setTitle(
                                    "مستخدم جديد"
                            )
                            .setView(l)
                            .setPositiveButton(
                                    "حفظ",
                                    (d, w) -> {

                                        db.addUser(
                                                name.getText()
                                                        .toString()
                                                        .trim(),
                                                ro.getSelectedItem()
                                                        .toString(),
                                                user.getText()
                                                        .toString()
                                                        .trim(),
                                                pass.getText()
                                                        .toString()
                                        );

                                        users();
                                    }
                            )
                            .setNegativeButton(
                                    "إلغاء",
                                    null
                            )
                            .show();
                }
        );
    }

    // =========================================================
    // النسخ الاحتياطي
    // =========================================================

    void files() {

        page("📁 النسخ الاحتياطي");

        addCard(
                "النسخ الاحتياطي المحلي",
                "يمكن حفظ نسخة من قاعدة بيانات المدرسة على الهاتف."
        );

        Button b =
                btn("💾 إنشاء نسخة احتياطية");

        content.addView(
                b,
                new LinearLayout.LayoutParams(
                        -1,
                        68
                )
        );

        b.setOnClickListener(
                v -> {

                    Intent i =
                            new Intent(
                                    Intent.ACTION_CREATE_DOCUMENT
                            );

                    i.setType(
                            "application/octet-stream"
                    );

                    i.putExtra(
                            Intent.EXTRA_TITLE,
                            "Jafar_" +
                            new SimpleDateFormat(
                                    "yyyyMMdd_HHmm",
                                    Locale.US
                            ).format(
                                    new Date()
                            ) +
                            ".db"
                    );

                    startActivityForResult(
                            i,
                            REQ_BACKUP
                    );
                }
        );
    }

    // =========================================================
    // بوابة ولي الأمر
    // =========================================================

    void showPortal() {

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
                        "بوابة ولي الأمر\n" +
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

        addCard(
                "📴 بدون إنترنت",
                "البيانات محفوظة محلياً"
        );

        addCard(
                "🔔 الإعلانات",
                db.latestAnnouncements()
        );

        for (String[] st :
                db.linkedStudents(
                        currentUsername
                )) {

            addCard(
                    "👨‍🎓 " + st[0],
                    "الرقم: " + st[1] +
                    "\nالصف: " + st[2] +
                    " - " + st[3] +
                    "\n" +
                    db.resultLine(st[1], 2) +
                    "\n" +
                    db.studentAttendanceSummary(st[1])
            );

            Button pdf =
                    btn(
                            "📄 كشف درجات PDF"
                    );

            content.addView(
                    pdf,
                    new LinearLayout.LayoutParams(
                            -1,
                            62
                    )
            );

            String id = st[1];

            pdf.setOnClickListener(
                    v ->
                            generateStudentPdf(id)
            );
        }
    }

    // =========================================================
    // PDF الطالب
    // =========================================================

    void generateStudentPdf(
            String id) {

        try {

            String[] st =
                    db.student(id);

            if (st == null)
                return;

            File f =
                    new File(
                            getCacheDir(),
                            "ملف_الطالب_" +
                            id +
                            ".pdf"
                    );

            PdfDocument doc =
                    new PdfDocument();

            PdfDocument.Page pg =
                    doc.startPage(
                            new PdfDocument.PageInfo
                                    .Builder(
                                            595,
                                            842,
                                            1
                                    )
                                    .create()
                    );

            Canvas c =
                    pg.getCanvas();

            Paint p =
                    new Paint();

            p.setTextAlign(
                    Paint.Align.RIGHT
            );

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            p.setTextSize(18);

            c.drawText(
                    "مدرسة جعفر بن أبي طالب",
                    550,
                    45,
                    p
            );

            p.setTypeface(
                    Typeface.DEFAULT
            );

            p.setTextSize(13);

            c.drawText(
                    "الجمهورية اليمنية - إب - مذيخرة - الأشعوب",
                    550,
                    68,
                    p
            );

            c.drawText(
                    "ملف الطالب",
                    550,
                    105,
                    p
            );

            c.drawText(
                    "الاسم: " + st[0],
                    550,
                    135,
                    p
            );

            c.drawText(
                    "الرقم: " + st[1],
                    550,
                    158,
                    p
            );

            c.drawText(
                    "الصف: " +
                    st[2] +
                    " - " +
                    st[3],
                    550,
                    181,
                    p
            );

            p.setTextSize(12);

            c.drawText(
                    db.resultLine(id, 0),
                    550,
                    220,
                    p
            );

            c.drawText(
                    db.resultLine(id, 1),
                    550,
                    245,
                    p
            );

            c.drawText(
                    db.resultLine(id, 2),
                    550,
                    270,
                    p
            );

            c.drawText(
                    db.studentAttendanceSummary(id),
                    550,
                    305,
                    p
            );

            doc.finishPage(pg);

            FileOutputStream out =
                    new FileOutputStream(f);

            doc.writeTo(out);

            out.close();
            doc.close();

            sharePdf(f);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "خطأ PDF: " +
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================================
    // PDF الحضور الشهري
    // =========================================================

    void generateAttendancePdf(
            String grade,
            String classroom,
            int year,
            int month) {

        try {

            ArrayList<Calendar> days =
                    schoolDays(
                            year,
                            month
                    );

            ArrayList<String[]> students =
                    db.studentsInClass(
                            grade,
                            classroom
                    );

            File f =
                    new File(
                            getCacheDir(),
                            "حضور_" +
                            grade +
                            "_" +
                            classroom +
                            "_" +
                            year +
                            "_" +
                            month +
                            ".pdf"
                    );

            PdfDocument doc =
                    new PdfDocument();

            PdfDocument.Page pg =
                    doc.startPage(
                            new PdfDocument.PageInfo
                                    .Builder(
                                            842,
                                            595,
                                            1
                                    )
                                    .create()
                    );

            Canvas c =
                    pg.getCanvas();

            Paint p =
                    new Paint();

            p.setTextAlign(
                    Paint.Align.RIGHT
            );

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            p.setTextSize(18);

            c.drawText(
                    "مدرسة جعفر بن أبي طالب",
                    800,
                    30,
                    p
            );

            p.setTextSize(13);

            p.setTypeface(
                    Typeface.DEFAULT
            );

            c.drawText(
                    "كشف الحضور والغياب الشهري",
                    800,
                    52,
                    p
            );

            c.drawText(
                    "الصف: " +
                    grade +
                    "   الشعبة: " +
                    classroom,
                    800,
                    73,
                    p
            );

            c.drawText(
                    "الشهر: " +
                    month +
                    " / " +
                    year,
                    800,
                    94,
                    p
            );

            float startX = 800;
            float startY = 120;
            float nameW = 145;
            float dayW = 30;

            p.setStyle(
                    Paint.Style.STROKE
            );

            p.setTextAlign(
                    Paint.Align.CENTER
            );

            c.drawRect(
                    startX - nameW,
                    startY,
                    startX,
                    startY + 30,
                    p
            );

            p.setStyle(
                    Paint.Style.FILL
            );

            c.drawText(
                    "الطالب",
                    startX - nameW / 2,
                    startY + 20,
                    p
            );

            for (int i = 0;
                 i < days.size();
                 i++) {

                float right =
                        startX -
                        nameW -
                        i * dayW;

                float left =
                        right - dayW;

                p.setStyle(
                        Paint.Style.STROKE
                );

                c.drawRect(
                        left,
                        startY,
                        right,
                        startY + 30,
                        p
                );

                p.setStyle(
                        Paint.Style.FILL
                );

                String d =
                        new SimpleDateFormat(
                                "dd",
                                Locale.US
                        ).format(
                                days.get(i)
                                        .getTime()
                        );

                c.drawText(
                        d,
                        (left + right) / 2,
                        startY + 20,
                        p
                );
            }

            float y =
                    startY + 30;

            p.setTextSize(7);

            for (String[] st :
                    students) {

                if (y > 555)
                    break;

                p.setStyle(
                        Paint.Style.STROKE
                );

                c.drawRect(
                        startX - nameW,
                        y,
                        startX,
                        y + 30,
                        p
                );

                p.setStyle(
                        Paint.Style.FILL
                );

                p.setTextAlign(
                        Paint.Align.CENTER
                );

                c.drawText(
                        st[0],
                        startX - nameW / 2,
                        y + 19,
                        p
                );

                for (int i = 0;
                     i < days.size();
                     i++) {

                    float right =
                            startX -
                            nameW -
                            i * dayW;

                    float left =
                            right - dayW;

                    p.setStyle(
                            Paint.Style.STROKE
                    );

                    c.drawRect(
                            left,
                            y,
                            right,
                            y + 30,
                            p
                    );

                    p.setStyle(
                            Paint.Style.FILL
                    );

                    String date =
                            new SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.US
                            ).format(
                                    days.get(i)
                                            .getTime()
                            );

                    String status =
                            db.attendanceStatus(
                                    st[1],
                                    date
                            );

                    String mark =
                            attendanceShort(
                                    status
                            );

                    c.drawText(
                            mark,
                            (left + right) / 2,
                            y + 19,
                            p
                    );
                }

                y += 30;
            }

            p.setTextAlign(
                    Paint.Align.RIGHT
            );

            p.setTextSize(8);

            c.drawText(
                    "ح = حاضر    غ = غائب    ت = متأخر    ع = بعذر    ـ = لم يسجل",
                    800,
                    580,
                    p
            );

            doc.finishPage(pg);

            FileOutputStream out =
                    new FileOutputStream(f);

            doc.writeTo(out);

            out.close();
            doc.close();

            sharePdf(f);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "خطأ في كشف الحضور: " +
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    String attendanceShort(
            String status) {

        if ("حاضر".equals(status))
            return "ح";

        if ("غائب".equals(status))
            return "غ";

        if ("متأخر".equals(status))
            return "ت";

        if ("بعذر".equals(status))
            return "ع";

        return "ـ";
    }

    void sharePdf(File f) {

        try {

            Uri uri =
                    FileProvider.getUriForFile(
                            this,
                            getPackageName() +
                            ".fileprovider",
                            f
                    );

            Intent i =
                    new Intent(
                            Intent.ACTION_SEND
                    );

            i.setType(
                    "application/pdf"
            );

            i.putExtra(
                    Intent.EXTRA_STREAM,
                    uri
            );

            i.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            startActivity(
                    Intent.createChooser(
                            i,
                            "مشاركة ملف PDF"
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "تعذر مشاركة الملف: " +
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================================
    // النسخ الاحتياطي
    // =========================================================

    @Override
    protected void onActivityResult(
            int req,
            int res,
            Intent data) {

        super.onActivityResult(
                req,
                res,
                data
        );

        if (req == REQ_BACKUP &&
                res == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            try {

                OutputStream out =
                        getContentResolver()
                                .openOutputStream(
                                        data.getData()
                                );

                if (out == null)
                    throw new IOException(
                            "تعذر فتح ملف النسخة"
                    );

                InputStream in =
                        new FileInputStream(
                                getDatabasePath(
                                        "jafar_school.db"
                                )
                        );

                byte[] buf =
                        new byte[8192];

                int len;

                while (
                        (len = in.read(buf)) > 0
                ) {

                    out.write(
                            buf,
                            0,
                            len
                    );
                }

                in.close();
                out.close();

                Toast.makeText(
                        this,
                        "تم إنشاء النسخة الاحتياطية بنجاح",
                        Toast.LENGTH_SHORT
                ).show();

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "خطأ: " +
                        e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
