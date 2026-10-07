package com.jafar.school;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;
import androidx.core.content.FileProvider;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    static final int REQ_BACKUP = 7101;

    LinearLayout root, content;

    String role = "القائم بأعمال المدير";
    String currentUser = "";
    String currentUsername = "";
    String assignedGrade = "";
    String assignedClass = "";

    DB db;

    final int BLUE = Color.rgb(17, 96, 177);
    final int BG = Color.rgb(244, 247, 251);
    final int WHITE = Color.WHITE;
    final int TEXT = Color.rgb(25, 45, 68);

    final String[] DAYS = {
            "السبت",
            "الأحد",
            "الاثنين",
            "الثلاثاء",
            "الأربعاء"
    };

    /*
     * سجل التنقل:
     *
     * الرئيسية
     *   ↓
     * الطلاب
     *   ↓
     * ملف الطالب
     *
     * زر الهاتف:
     * ملف الطالب ← الطلاب ← الرئيسية
     */
    private final Stack<Runnable> navHistory = new Stack<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        /*
         * استخدام مساحة الشاشة بشكل طبيعي وكامل.
         */
        getWindow().setStatusBarColor(BLUE);
        getWindow().setNavigationBarColor(Color.BLACK);

        getWindow().getDecorView().setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        db = new DB(this);

        showLogin();
    }

    /*
     * زر الرجوع الموجود في الهاتف.
     *
     * لا يوجد اعتماد على زر رجوع داخل الشاشة.
     */
    @Override
    public void onBackPressed() {

        if (navHistory.size() > 1) {

            navHistory.pop();

            Runnable previous =
                    navHistory.peek();

            if (previous != null) {
                previous.run();
            }

            return;
        }

        /*
         * إذا كان المستخدم في الرئيسية:
         * لا نرجع إلى شاشة داخلية.
         */
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

    /*
     * نص عام.
     */
    TextView tv(String s, int sp, boolean bold) {

        TextView t =
                new TextView(this);

        t.setText(
                s == null ? "" : s
        );

        t.setTextSize(sp);
        t.setTextColor(TEXT);

        t.setTypeface(
                null,
                bold
                        ? Typeface.BOLD
                        : Typeface.NORMAL
        );

        /*
         * مهم جدًا للعربية:
         * السماح للنص بالظهور على أكثر من سطر.
         */
        t.setSingleLine(false);
        t.setMaxLines(10);
        t.setEllipsize(null);
        t.setIncludeFontPadding(true);

        t.setGravity(
                Gravity.CENTER_VERTICAL |
                Gravity.RIGHT
        );

        t.setPadding(
                14,
                10,
                14,
                10
        );

        t.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        return t;
    }

    /*
     * زر عام.
     *
     * تم تعديل الزر لمنع قص الكتابة.
     */
    Button btn(String s) {

        Button b =
                new Button(this);

        b.setText(
                s == null ? "" : s
        );

        b.setTextSize(14);
        b.setTextColor(WHITE);
        b.setAllCaps(false);

        b.setGravity(
                Gravity.CENTER
        );

        b.setSingleLine(false);
        b.setMaxLines(3);
        b.setEllipsize(null);

        b.setPadding(
                12,
                8,
                12,
                8
        );

        b.setMinHeight(60);
        b.setMinimumHeight(60);

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(BLUE);
        g.setCornerRadius(18);

        b.setBackground(g);

        b.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        return b;
    }

    /*
     * إنشاء أساس الصفحة.
     */
    void base() {

        root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

        root.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        root.setGravity(
                Gravity.FILL
        );

        setContentView(root);
    }

    /*
     * فتح صفحة جديدة.
     */
    void navigateTo(Runnable screen) {

        if (screen == null)
            return;

        navHistory.push(screen);

        screen.run();
    }

    /*
     * صفحة داخلية.
     *
     * لا يوجد زر رجوع هنا.
     * الرجوع يتم من زر الهاتف.
     */
    void page(String name) {

        base();

        /*
         * شريط العنوان فقط.
         */
        LinearLayout top =
                new LinearLayout(this);

        top.setOrientation(
                LinearLayout.HORIZONTAL
        );

        top.setGravity(
                Gravity.CENTER_VERTICAL
        );

        top.setPadding(
                12,
                10,
                12,
                8
        );

        top.setBackgroundColor(
                WHITE
        );

        TextView title =
                tv(name, 19, true);

        title.setGravity(
                Gravity.CENTER
        );

        title.setTextAlignment(
                View.TEXT_ALIGNMENT_CENTER
        );

        top.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        root.addView(
                top,
                new LinearLayout.LayoutParams(
                        -1,
                        80
                )
        );

        /*
         * محتوى الصفحة.
         */
        content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                12,
                8,
                12,
                35
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

    /*
     * بطاقة.
     */
    void addCard(String a, String b) {

        LinearLayout c =
                new LinearLayout(this);

        c.setOrientation(
                LinearLayout.VERTICAL
        );

        c.setPadding(
                16,
                14,
                16,
                14
        );

        c.setBackgroundColor(
                WHITE
        );

        TextView title =
                tv(a, 16, true);

        TextView body =
                tv(b, 14, false);

        body.setLineSpacing(
                3,
                1.0f
        );

        c.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        c.addView(
                body,
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
                3,
                7,
                3,
                7
        );

        content.addView(c, p);
    }

    /*
     * تسجيل الدخول.
     */
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
                28,
                35,
                28,
                35
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

        logo.setGravity(
                Gravity.CENTER
        );

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

        h.setGravity(
                Gravity.CENTER
        );

        box.addView(h);

        TextView sub =
                tv(
                        "الجمهورية اليمنية - إب - مذيخرة - الأشعوب\n" +
                        "نظام الإدارة الذكي - يعمل بدون إنترنت",
                        14,
                        false
                );

        sub.setGravity(
                Gravity.CENTER
        );

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

        loginTitle.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams lpTitle =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        lpTitle.setMargins(
                0,
                20,
                0,
                8
        );

        box.addView(
                loginTitle,
                lpTitle
        );

        EditText user =
                new EditText(this);

        user.setHint(
                "اسم المستخدم"
        );

        user.setTextSize(16);
        user.setSingleLine(true);

        box.addView(
                user,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        EditText pass =
                new EditText(this);

        pass.setHint(
                "كلمة المرور"
        );

        pass.setTextSize(16);

        pass.setSingleLine(true);

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

        LinearLayout.LayoutParams goP =
                new LinearLayout.LayoutParams(
                        -1,
                        62
                );

        goP.setMargins(
                0,
                14,
                0,
                5
        );

        box.addView(go, goP);

        TextView demo =
                tv(
                        "admin / 1234",
                        12,
                        false
                );

        demo.setGravity(
                Gravity.CENTER
        );

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
                        "بيانات خاطئة",
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

    /*
     * بوابة الطالب / ولي الأمر.
     */
    void showPortal() {

        base();

        LinearLayout head =
                new LinearLayout(this);

        head.setGravity(
                Gravity.CENTER_VERTICAL
        );

        head.setPadding(
                10,
                10,
                10,
                5
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
                12,
                8,
                12,
                35
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
                    db.studentAttendanceSummary(
                            st[1]
                    )
            );

            Button pdf =
                    btn("📄 كشف درجات PDF");

            content.addView(
                    pdf,
                    new LinearLayout.LayoutParams(
                            -1,
                            62
                    )
            );

            String id = st[1];

            pdf.setOnClickListener(
                    v -> generateStudentPdf(id)
            );
        }
    }

    boolean admin() {

        return role.equals(
                "القائم بأعمال المدير"
        );
    }

    /*
     * الرئيسية.
     */
    void showHome() {

        base();

        LinearLayout head =
                new LinearLayout(this);

        head.setGravity(
                Gravity.CENTER_VERTICAL
        );

        head.setPadding(
                10,
                10,
                10,
                5
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
                12,
                8,
                12,
                35
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

            t.setGravity(
                    Gravity.CENTER
            );

            t.setTextAlignment(
                    View.TEXT_ALIGNMENT_CENTER
            );

            t.setBackgroundColor(
                    WHITE
            );

            t.setMinHeight(82);

            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(
                            0,
                            82,
                            1
                    );

            p.setMargins(
                    3,
                    3,
                    3,
                    8
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
                    4,
                    4,
                    4,
                    4
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

    /*
     * الطلاب.
     */
    void students() {

        page("👨‍🎓 الطلاب");

        EditText s =
                new EditText(this);

        s.setHint("بحث عن طالب...");
        s.setTextSize(16);
        s.setSingleLine(true);

        content.addView(
                s,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        if (admin()) {

            Button a =
                    btn("＋ إضافة طالب");

            content.addView(
                    a,
                    new LinearLayout.LayoutParams(
                            -1,
                            64
                    )
            );

            a.setOnClickListener(
                    v -> studentDialog(null)
            );
        }

        LinearLayout list =
                new LinearLayout(this);

        list.setOrientation(
                LinearLayout.VERTICAL
        );

        content.addView(list);

        Runnable ref = () -> {

            list.removeAllViews();

            for (String[] r :
                    db.students(
                            s.getText()
                                    .toString()
                    )) {

                Button b =
                        btn(
                                "👨‍🎓 " +
                                r[0] +
                                "\nالرقم: " +
                                r[1] +
                                "   الصف: " +
                                r[2] +
                                "/" +
                                r[3]
                        );

                LinearLayout.LayoutParams bp =
                        new LinearLayout.LayoutParams(
                                -1,
                                78
                        );

                bp.setMargins(
                        0,
                        4,
                        0,
                        4
                );

                list.addView(b, bp);

                b.setOnClickListener(
                        v -> navigateTo(
                                () ->
                                        studentDetails(
                                                r[1]
                                        )
                        )
                );
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
                                80
                        )
                );
            }
        };

        s.addTextChangedListener(
                new TextWatcher() {

                    public void beforeTextChanged(
                            CharSequence a,
                            int b,
                            int c,
                            int d
                    ) {}

                    public void onTextChanged(
                            CharSequence a,
                            int b,
                            int c,
                            int d
                    ) {
                        ref.run();
                    }

                    public void afterTextChanged(
                            Editable e
                    ) {}
                }
        );

        ref.run();
    }

    void studentDialog(String id) {

        boolean edit =
                id != null;

        String[] old =
                edit
                        ? db.student(id)
                        : null;

        EditText n = new EditText(this);
        EditText sid = new EditText(this);
        EditText g = new EditText(this);
        EditText cl = new EditText(this);
        EditText pa = new EditText(this);
        EditText ph = new EditText(this);

        n.setHint("الاسم الرباعي");
        sid.setHint("الرقم");
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
                8,
                5,
                8,
                5
        );

        for (EditText e :
                new EditText[]{
                        n,
                        sid,
                        g,
                        cl,
                        pa,
                        ph
                }) {

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
                                            .toString(),
                                    sid.getText()
                                            .toString(),
                                    g.getText()
                                            .toString(),
                                    cl.getText()
                                            .toString(),
                                    pa.getText()
                                            .toString(),
                                    ph.getText()
                                            .toString()
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

    /*
     * ملف الطالب.
     */
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
                "📊 النتائج",
                db.resultLine(id, 0) +
                "\n" +
                db.resultLine(id, 1) +
                "\n" +
                db.resultLine(id, 2)
        );

        addCard(
                "📅 الحضور",
                db.studentAttendanceSummary(id)
        );

        Button pdf =
                btn("📄 طباعة PDF");

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

    /*
     * المعلمون.
     */
    void teachers() {

        page("👨‍🏫 المعلمون");

        if (admin()) {

            Button a =
                    btn("＋ إضافة معلم");

            content.addView(
                    a,
                    new LinearLayout.LayoutParams(
                            -1,
                            64
                    )
            );

            a.setOnClickListener(v -> {

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
            });
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

    /*
     * الصفوف.
     */
    void classes() {

        page("🏫 الصفوف والشعب");

        if (admin()) {

            Button a =
                    btn("＋ إضافة صف / شعبة");

            content.addView(
                    a,
                    new LinearLayout.LayoutParams(
                            -1,
                            64
                    )
            );

            a.setOnClickListener(v -> {

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
                                                    .toString(),
                                            c.getText()
                                                    .toString()
                                    );

                                    classes();
                                }
                        )
                        .setNegativeButton(
                                "إلغاء",
                                null
                        )
                        .show();
            });
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

    /*
     * الحضور.
     */
    void attendance() {

        page("✅ الحضور");

        ArrayList<String[]> cs =
                db.classes();

        if (cs.isEmpty()) {

            addCard(
                    "لا توجد صفوف",
                    "أضف صفًا أولًا من قسم الصفوف"
            );

            return;
        }

        Button choose =
                btn("اختر الصف لتسجيل الحضور");

        content.addView(
                choose,
                new LinearLayout.LayoutParams(
                        -1,
                        64
                )
        );

        choose.setOnClickListener(v -> {

            String[] items =
                    new String[cs.size()];

            for (int i = 0;
                 i < cs.size();
                 i++) {

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
        });
    }

    void attendanceClass(
            String g,
            String c
    ) {

        page(
                "حضور " +
                g +
                " / " +
                c
        );

        ArrayList<String[]> students =
                db.studentsInClass(
                        g,
                        c
                );

        if (students.isEmpty()) {

            addCard(
                    "لا يوجد طلاب",
                    "لا يوجد طلاب مسجلون في هذا الصف"
            );

            return;
        }

        for (String[] r :
                students) {

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            row.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            TextView name =
                    tv(r[0], 14, true);

            name.setGravity(
                    Gravity.CENTER_VERTICAL |
                    Gravity.RIGHT
            );

            row.addView(
                    name,
                    new LinearLayout.LayoutParams(
                            0,
                            65,
                            1
                    )
            );

            Spinner sp =
                    new Spinner(this);

            String[] o = {
                    "لم يسجل",
                    "حاضر",
                    "غائب",
                    "متأخر",
                    "بعذر"
            };

            sp.setAdapter(
                    new ArrayAdapter<>(
                            this,
                            android.R.layout
                                    .simple_spinner_dropdown_item,
                            o
                    )
            );

            String old =
                    db.attendanceStatus(
                            r[1]
                    );

            for (int i = 0;
                 i < o.length;
                 i++) {

                if (o[i].equals(old)) {
                    sp.setSelection(i);
                }
            }

            sp.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {

                        public void onNothingSelected(
                                AdapterView<?> p
                        ) {}

                        public void onItemSelected(
                                AdapterView<?> p,
                                View v,
                                int pos,
                                long id
                        ) {

                            if (pos > 0) {

                                db.setAttendance(
                                        r[1],
                                        o[pos]
                                );
                            }
                        }
                    }
            );

            row.addView(
                    sp,
                    new LinearLayout.LayoutParams(
                            145,
                            65
                    )
            );

            LinearLayout.LayoutParams rp =
                    new LinearLayout.LayoutParams(
                            -1,
                            70
                    );

            rp.setMargins(
                    0,
                    2,
                    0,
                    2
            );

            content.addView(row, rp);
        }
    }

    /*
     * الدرجات.
     */
    void grades() {

        page("📊 الدرجات");

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

    void chooseClassForGrades() {

        ArrayList<String[]> cs =
                db.classes();

        if (cs.isEmpty()) {

            addCard(
                    "لا توجد صفوف",
                    "أضف الصفوف أولًا"
            );

            return;
        }

        String[] it =
                new String[cs.size()];

        for (int i = 0;
             i < cs.size();
             i++) {

            it[i] =
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

    void classGrades(
            String grade,
            String classroom
    ) {

        page(
                "درجات " +
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

        Spinner sub =
                new Spinner(this);

        sub.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        db.subjects()
                )
        );

        content.addView(
                sem,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        content.addView(
                sub,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        LinearLayout list =
                new LinearLayout(this);

        list.setOrientation(
                LinearLayout.VERTICAL
        );

        content.addView(list);

        Runnable render = () -> {

            list.removeAllViews();

            for (String[] st :
                    db.studentsInClass(
                            grade,
                            classroom
                    )) {

                LinearLayout row =
                        new LinearLayout(this);

                row.setOrientation(
                        LinearLayout.HORIZONTAL
                );

                row.setGravity(
                        Gravity.CENTER_VERTICAL
                );

                TextView studentName =
                        tv(
                                st[0],
                                12,
                                true
                        );

                studentName.setGravity(
                        Gravity.CENTER_VERTICAL |
                        Gravity.RIGHT
                );

                row.addView(
                        studentName,
                        new LinearLayout.LayoutParams(
                                135,
                                62
                        )
                );

                EditText a =
                        box("م/20");

                EditText o =
                        box("ش/20");

                EditText h =
                        box("و/20");

                EditText w =
                        box("ت/40");

                EditText ex =
                        box("اختبار");

                int semester =
                        sem.getSelectedItemPosition()
                        + 1;

                String subject =
                        sub.getSelectedItem()
                                .toString();

                double[] old =
                        db.monthlyValues(
                                st[1],
                                subject,
                                semester,
                                1
                        );

                if (old != null) {

                    a.setText(
                            fmt(old[0])
                    );

                    o.setText(
                            fmt(old[1])
                    );

                    h.setText(
                            fmt(old[2])
                    );

                    w.setText(
                            fmt(old[3])
                    );
                }

                double exam =
                        db.exam(
                                st[1],
                                subject,
                                semester
                        );

                if (exam != 0) {
                    ex.setText(
                            fmt(exam)
                    );
                }

                addScoreBox(row, a, 58);
                addScoreBox(row, o, 58);
                addScoreBox(row, h, 58);
                addScoreBox(row, w, 58);
                addScoreBox(row, ex, 70);

                row.setTag(
                        new EditText[]{
                                a,
                                o,
                                h,
                                w,
                                ex
                        }
                );

                LinearLayout.LayoutParams rp =
                        new LinearLayout.LayoutParams(
                                -1,
                                68
                        );

                rp.setMargins(
                        0,
                        2,
                        0,
                        2
                );

                list.addView(row, rp);
            }
        };

        sem.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    public void onItemSelected(
                            AdapterView<?> p,
                            View v,
                            int po,
                            long id
                    ) {
                        render.run();
                    }

                    public void onNothingSelected(
                            AdapterView<?> p
                    ) {}
                }
        );

        sub.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    public void onItemSelected(
                            AdapterView<?> p,
                            View v,
                            int po,
                            long id
                    ) {
                        render.run();
                    }

                    public void onNothingSelected(
                            AdapterView<?> p
                    ) {}
                }
        );

        render.run();

        Button save =
                btn("💾 حفظ الدرجات");

        content.addView(
                save,
                new LinearLayout.LayoutParams(
                        -1,
                        68
                )
        );

        save.setOnClickListener(v -> {

            android.database.sqlite.SQLiteDatabase writable =
                    db.getWritableDatabase();

            writable.beginTransaction();

            try {

                ArrayList<String[]> students =
                        db.studentsInClass(
                                grade,
                                classroom
                        );

                for (
                        int i = 0;
                        i < list.getChildCount()
                                && i < students.size();
                        i++
                ) {

                    String sid =
                            students.get(i)[1];

                    EditText[] z =
                            (EditText[])
                                    list.getChildAt(i)
                                            .getTag();

                    if (z == null)
                        continue;

                    db.setMonthlyScore(
                            sid,
                            sub.getSelectedItem()
                                    .toString(),
                            sem.getSelectedItemPosition()
                                    + 1,
                            1,
                            par(z[0]),
                            par(z[1]),
                            par(z[2]),
                            par(z[3])
                    );

                    db.setExam(
                            sid,
                            sub.getSelectedItem()
                                    .toString(),
                            sem.getSelectedItemPosition()
                                    + 1,
                            par(z[4])
                    );
                }

                writable.setTransactionSuccessful();

                Toast.makeText(
                        this,
                        "تم حفظ الدرجات بنجاح",
                        Toast.LENGTH_SHORT
                ).show();

            } finally {

                writable.endTransaction();
            }
        });
    }

    void addScoreBox(
            LinearLayout row,
            EditText e,
            int width
    ) {

        row.addView(
                e,
                new LinearLayout.LayoutParams(
                        width,
                        58
                )
        );
    }

    EditText box(String h) {

        EditText e =
                new EditText(this);

        e.setHint(h);
        e.setTextSize(10);

        e.setSingleLine(true);

        e.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        e.setGravity(
                Gravity.CENTER
        );

        e.setPadding(
                2,
                2,
                2,
                2
        );

        return e;
    }

    double par(EditText e) {

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

    String fmt(double x) {

        if (x == 0)
            return "";

        return x == Math.round(x)
                ? String.valueOf((int) x)
                : String.format(
                        Locale.US,
                        "%.1f",
                        x
                );
    }

    /*
     * الجدول.
     */
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

    /*
     * التقارير.
     */
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
                db.todayAbsent()
        );
    }

    /*
     * الإعلانات.
     */
    void announcements() {

        page("🔔 الإعلانات");

        if (admin()) {

            Button a =
                    btn("＋ إضافة إعلان");

            content.addView(
                    a,
                    new LinearLayout.LayoutParams(
                            -1,
                            64
                    )
            );

            a.setOnClickListener(v -> {

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
            });
        }

        for (String[] r :
                db.announcements()) {

            addCard(
                    "📢 " + r[1],
                    "التاريخ: " + r[0]
            );
        }
    }

    /*
     * المستخدمون.
     */
    void users() {

        page("🔐 المستخدمون");

        if (!admin()) {

            addCard(
                    "غير مسموح",
                    "ليس لديك صلاحية إدارة المستخدمين"
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

        Button a =
                btn("＋ إضافة مستخدم جديد");

        content.addView(
                a,
                new LinearLayout.LayoutParams(
                        -1,
                        64
                )
        );

        a.setOnClickListener(v -> {

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
                                    "القائم بأعمال المدير"
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
                                                .toString(),
                                        ro.getSelectedItem()
                                                .toString(),
                                        user.getText()
                                                .toString(),
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
        });
    }

    /*
     * النسخ الاحتياطي.
     */
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

        b.setOnClickListener(v -> {

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
                            "yyyyMMdd",
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
        });
    }

    /*
     * PDF الطالب.
     */
    void generateStudentPdf(String id) {

        try {

            String[] st =
                    db.student(id);

            if (st == null)
                return;

            File f =
                    new File(
                            getCacheDir(),
                            "طالب_" +
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

            p.setTextSize(16);

            p.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            p.setTextAlign(
                    Paint.Align.RIGHT
            );

            c.drawText(
                    "مدرسة جعفر بن أبي طالب - إب - الأشعوب",
                    550,
                    40,
                    p
            );

            p.setTextSize(12);

            p.setTypeface(
                    Typeface.DEFAULT
            );

            c.drawText(
                    "الطالب: " +
                    st[0] +
                    " | الرقم: " +
                    st[1] +
                    " | الصف: " +
                    st[2] +
                    "-" +
                    st[3],
                    550,
                    70,
                    p
            );

            c.drawText(
                    db.resultLine(id, 0),
                    550,
                    100,
                    p
            );

            c.drawText(
                    db.resultLine(id, 1),
                    550,
                    120,
                    p
            );

            c.drawText(
                    db.resultLine(id, 2),
                    550,
                    140,
                    p
            );

            doc.finishPage(pg);

            FileOutputStream out =
                    new FileOutputStream(f);

            doc.writeTo(out);

            out.close();

            doc.close();

            android.net.Uri uri =
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
                            "مشاركة"
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "خطأ PDF: " +
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int req,
            int res,
            Intent data
    ) {

        super.onActivityResult(
                req,
                res,
                data
        );

        if (
                req == REQ_BACKUP &&
                res == RESULT_OK &&
                data != null &&
                data.getData() != null
        ) {

            try {

                OutputStream out =
                        getContentResolver()
                                .openOutputStream(
                                        data.getData()
                                );

                if (out == null)
                    throw new IOException(
                            "تعذر فتح ملف النسخ الاحتياطي"
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
