package com.jafar.school;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import androidx.core.content.FileProvider;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;

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

    final String[] DAYS = {
            "السبت",
            "الأحد",
            "الاثنين",
            "الثلاثاء",
            "الأربعاء"
    };

    /*
     * سجل الصفحات.
     *
     * مثال:
     * الرئيسية
     *    ↓
     * الطلاب
     *    ↓
     * ملف الطالب
     *
     * عند الضغط على رجوع من ملف الطالب:
     * ملف الطالب ← الطلاب
     *
     * ثم:
     * الطلاب ← الرئيسية
     */
    private final Stack<Runnable> navHistory = new Stack<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().getDecorView().setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        db = new DB(this);

        showLogin();
    }

    @Override
    public void onBackPressed() {

        /*
         * إذا توجد صفحة سابقة:
         * احذف الصفحة الحالية ثم اعرض الصفحة السابقة.
         */
        if (navHistory.size() > 1) {
            navHistory.pop();

            Runnable previous = navHistory.peek();

            if (previous != null) {
                previous.run();
            }

            return;
        }

        /*
         * إذا كانت هذه الصفحة الرئيسية فلا توجد صفحة
         * داخلية للرجوع إليها.
         */
        super.onBackPressed();
    }

    TextView tv(String s, int sp, boolean bold) {
        TextView t = new TextView(this);

        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(Color.rgb(25, 45, 68));

        t.setTypeface(
                null,
                bold ? Typeface.BOLD : Typeface.NORMAL
        );

        t.setPadding(14, 9, 14, 9);

        return t;
    }

    Button btn(String s) {

        Button b = new Button(this);

        b.setText(s);
        b.setTextSize(14);
        b.setTextColor(WHITE);
        b.setAllCaps(false);

        b.setGravity(Gravity.CENTER);

        GradientDrawable g = new GradientDrawable();
        g.setColor(BLUE);
        g.setCornerRadius(18);

        b.setBackground(g);

        /*
         * يجعل منطقة اللمس مريحة.
         */
        b.setMinHeight(54);
        b.setMinimumHeight(54);

        return b;
    }

    void base() {

        root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        setContentView(root);
    }

    /*
     * فتح صفحة جديدة وإضافتها إلى سجل التنقل.
     */
    void navigateTo(Runnable screen) {

        navHistory.push(screen);

        screen.run();
    }

    /*
     * إنشاء صفحة داخلية.
     *
     * تم إصلاح مشكلة زر الرجوع هنا:
     *
     * 1. إضافة مسافة علوية واضحة.
     * 2. زر أكبر.
     * 3. عدم وضعه ملاصقاً لشريط حالة الهاتف.
     * 4. منطقة لمس واسعة.
     */
    void page(String name) {

        base();

        LinearLayout top = new LinearLayout(this);

        top.setOrientation(LinearLayout.HORIZONTAL);

        /*
         * مسافة علوية حتى لا يكون الزر ملاصقاً
         * لشريط حالة الهاتف.
         */
        top.setPadding(
                8,
                18,
                8,
                8
        );

        top.setGravity(Gravity.CENTER_VERTICAL);

        /*
         * زر الرجوع.
         */
        Button back = btn("رجوع");

        back.setTextSize(15);

        back.setGravity(Gravity.CENTER);

        /*
         * مساحة الزر أكبر من السابق.
         */
        LinearLayout.LayoutParams backParams =
                new LinearLayout.LayoutParams(
                        125,
                        58
                );

        backParams.setMargins(
                5,
                2,
                8,
                2
        );

        top.addView(back, backParams);

        /*
         * الرجوع إلى الصفحة السابقة.
         */
        back.setOnClickListener(v -> onBackPressed());

        /*
         * عنوان الصفحة.
         */
        TextView title = tv(name, 18, true);

        title.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        58,
                        1
                );

        top.addView(title, titleParams);

        root.addView(
                top,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        /*
         * محتوى الصفحة.
         */
        content = new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                9,
                6,
                9,
                28
        );

        ScrollView s = new ScrollView(this);

        s.setFillViewport(true);

        s.addView(content);

        root.addView(
                s,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );
    }

    void addCard(String a, String b) {

        LinearLayout c = new LinearLayout(this);

        c.setOrientation(
                LinearLayout.VERTICAL
        );

        c.setPadding(
                14,
                12,
                14,
                12
        );

        c.setBackgroundColor(WHITE);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        p.setMargins(
                3,
                8,
                3,
                8
        );

        c.addView(
                tv(a, 16, true)
        );

        c.addView(
                tv(b, 13, false)
        );

        content.addView(c, p);
    }

    /*
     * تسجيل الدخول.
     */
    void showLogin() {

        navHistory.clear();

        base();

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(Gravity.CENTER);

        box.setPadding(
                26,
                30,
                26,
                25
        );

        root.addView(
                box,
                new LinearLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        box.addView(
                tv("🏫", 64, true)
        );

        TextView h =
                tv(
                        "مدرسة جعفر بن أبي طالب",
                        22,
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

        box.addView(sub);

        box.addView(
                tv("تسجيل الدخول", 18, true)
        );

        EditText user =
                new EditText(this);

        user.setHint("اسم المستخدم");

        box.addView(user);

        EditText pass =
                new EditText(this);

        pass.setHint("كلمة المرور");

        pass.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        box.addView(pass);

        Button go = btn("دخول");

        box.addView(
                go,
                new LinearLayout.LayoutParams(
                        -1,
                        58
                )
        );

        box.addView(
                tv("admin / 1234", 12, false)
        );

        go.setOnClickListener(v -> {

            String[] a =
                    db.authenticate(
                            user.getText().toString().trim(),
                            pass.getText().toString()
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

        head.setPadding(
                8,
                8,
                8,
                3
        );

        head.addView(
                tv(
                        "بوابة ولي الأمر\n" + currentUser,
                        16,
                        true
                ),
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        Button out = btn("خروج");

        out.setOnClickListener(
                v -> showLogin()
        );

        head.addView(
                out,
                new LinearLayout.LayoutParams(
                        84,
                        52
                )
        );

        root.addView(head);

        ScrollView sv =
                new ScrollView(this);

        content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                9,
                5,
                9,
                28
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
                db.linkedStudents(currentUsername)) {

            addCard(
                    "👨‍🎓 " + st[0],
                    "الرقم: " + st[1] +
                    "\nالصف: " + st[2] +
                    "-" + st[3] +
                    "\n" + db.resultLine(st[1], 2) +
                    "\n" + db.studentAttendanceSummary(st[1])
            );

            Button pdf =
                    btn("📄 كشف درجات PDF");

            content.addView(pdf);

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

        head.setPadding(
                8,
                8,
                8,
                3
        );

        head.addView(
                tv(
                        "🏫 مدرسة جعفر\n" +
                        role + " : " + currentUser,
                        16,
                        true
                ),
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        Button out = btn("خروج");

        out.setOnClickListener(
                v -> showLogin()
        );

        head.addView(
                out,
                new LinearLayout.LayoutParams(
                        84,
                        52
                )
        );

        root.addView(head);

        ScrollView sv =
                new ScrollView(this);

        content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                9,
                5,
                9,
                28
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

            t.setBackgroundColor(
                    WHITE
            );

            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(
                            0,
                            -2,
                            1
                    );

            p.setMargins(
                    3,
                    3,
                    3,
                    3
            );

            r.addView(t, p);
        }

        content.addView(r);
    }

    void addGrid() {

        ArrayList<String[]> list =
                new ArrayList<>();

        list.add(
                new String[]{"👨‍🎓 الطلاب", "students"}
        );

        list.add(
                new String[]{"📊 الدرجات", "grades"}
        );

        list.add(
                new String[]{"✅ الحضور", "attendance"}
        );

        list.add(
                new String[]{"📅 الجدول", "timetable"}
        );

        if (admin()) {

            list.add(
                    new String[]{"👨‍🏫 المعلمون", "teachers"}
            );

            list.add(
                    new String[]{"🏫 الصفوف", "classes"}
            );

            list.add(
                    new String[]{"📈 التقارير", "reports"}
            );

            list.add(
                    new String[]{"🔔 الإعلانات", "announcements"}
            );

            list.add(
                    new String[]{"🔐 المستخدمون", "users"}
            );

            list.add(
                    new String[]{"📁 نسخ احتياطي", "files"}
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

                content.addView(row);
            }

            Button b =
                    btn(item[0]);

            row.addView(
                    b,
                    new LinearLayout.LayoutParams(
                            0,
                            70,
                            1
                    )
            );

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

        s.setHint("بحث...");

        content.addView(s);

        if (admin()) {

            Button a =
                    btn("＋ إضافة طالب");

            content.addView(a);

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
                            s.getText().toString()
                    )) {

                Button b =
                        btn(
                                r[0] +
                                " | " +
                                r[1] +
                                " | " +
                                r[2] +
                                "/" +
                                r[3]
                        );

                list.addView(b);

                b.setOnClickListener(
                        v -> navigateTo(
                                () -> studentDetails(r[1])
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

    /*
     * إضافة / تعديل طالب.
     *
     * مهم:
     * بعد الحفظ نعيد عرض الطلاب فقط،
     * ولا نضيف صفحة جديدة إلى سجل الرجوع.
     */
    void studentDialog(String id) {

        boolean edit = id != null;

        String[] old =
                edit ? db.student(id) : null;

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

        for (EditText e :
                new EditText[]{
                        n, sid, g, cl, pa, ph
                }) {

            l.addView(e);
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        edit ? "تعديل" : "إضافة طالب"
                )
                .setView(l)
                .setPositiveButton(
                        "حفظ",
                        (d, w) -> {

                            db.saveStudent(
                                    n.getText().toString(),
                                    sid.getText().toString(),
                                    g.getText().toString(),
                                    cl.getText().toString(),
                                    pa.getText().toString(),
                                    ph.getText().toString()
                            );

                            /*
                             * إعادة بناء صفحة الطلاب الحالية
                             * بدون إضافة سجل جديد.
                             */
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

        page("ملف الطالب");

        String[] r =
                db.student(id);

        if (r == null)
            return;

        addCard(
                "👨‍🎓 " + r[0],
                "الرقم: " + r[1] +
                "\nالصف: " + r[2] +
                "-" + r[3] +
                "\nولي الأمر: " + r[4]
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

        content.addView(pdf);

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
                    btn("＋ إضافة");

            content.addView(a);

            a.setOnClickListener(v -> {

                EditText n =
                        new EditText(this);

                EditText s =
                        new EditText(this);

                n.setHint("الاسم");
                s.setHint("المادة");

                LinearLayout l =
                        new LinearLayout(this);

                l.setOrientation(
                        LinearLayout.VERTICAL
                );

                l.addView(n);
                l.addView(s);

                new AlertDialog.Builder(this)
                        .setView(l)
                        .setPositiveButton(
                                "حفظ",
                                (d, w) -> {

                                    db.addTeacher(
                                            n.getText().toString(),
                                            s.getText().toString(),
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
                    "المادة: " + r[1]
            );
        }
    }

    /*
     * الصفوف.
     */
    void classes() {

        page("🏫 الصفوف");

        if (admin()) {

            Button a =
                    btn("＋ إضافة صف");

            content.addView(a);

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
                        .setView(l)
                        .setPositiveButton(
                                "حفظ",
                                (d, w) -> {

                                    db.addClass(
                                            g.getText().toString(),
                                            c.getText().toString()
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
                    "الصف " +
                    r[0] +
                    " شعبة " +
                    r[1],
                    "طلاب: " + r[2]
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

        String[] items =
                new String[cs.size()];

        for (int i = 0; i < cs.size(); i++) {

            items[i] =
                    "الصف " +
                    cs.get(i)[0] +
                    " - " +
                    cs.get(i)[1];
        }

        new AlertDialog.Builder(this)
                .setTitle("اختر الصف")
                .setItems(
                        items,
                        (d, w) ->
                                navigateTo(
                                        () -> attendanceClass(
                                                cs.get(w)[0],
                                                cs.get(w)[1]
                                        )
                                )
                )
                .show();
    }

    void attendanceClass(
            String g,
            String c
    ) {

        page(
                "حضور " +
                g +
                "/" +
                c
        );

        for (String[] r :
                db.studentsInClass(g, c)) {

            LinearLayout row =
                    new LinearLayout(this);

            row.addView(
                    tv(r[0], 14, true),
                    new LinearLayout.LayoutParams(
                            0,
                            -2,
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
                            android.R.layout.simple_spinner_dropdown_item,
                            o
                    )
            );

            String old =
                    db.attendanceStatus(r[1]);

            for (int i = 0; i < o.length; i++) {

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
                            130,
                            60
                    )
            );

            content.addView(row);
        }
    }

    /*
     * الدرجات.
     */
    void grades() {

        page("📊 الدرجات");

        Button e =
                btn("✏️ إدخال درجات");

        content.addView(e);

        e.setOnClickListener(
                v -> chooseClassForGrades()
        );
    }

    void chooseClassForGrades() {

        ArrayList<String[]> cs =
                db.classes();

        String[] it =
                new String[cs.size()];

        for (int i = 0; i < cs.size(); i++) {

            it[i] =
                    cs.get(i)[0] +
                    "-" +
                    cs.get(i)[1];
        }

        new AlertDialog.Builder(this)
                .setTitle("اختر الصف")
                .setItems(
                        it,
                        (d, w) ->
                                navigateTo(
                                        () -> classGrades(
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
                "/" +
                classroom
        );

        Spinner sem =
                new Spinner(this);

        sem.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
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
                        android.R.layout.simple_spinner_dropdown_item,
                        db.subjects()
                )
        );

        content.addView(sem);
        content.addView(sub);

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

                row.addView(
                        tv(st[0], 12, true),
                        new LinearLayout.LayoutParams(
                                140,
                                -2
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
                        box("اختبار30");

                double[] old =
                        db.monthlyValues(
                                st[1],
                                sub.getSelectedItem().toString(),
                                sem.getSelectedItemPosition() + 1,
                                1
                        );

                if (old != null) {

                    a.setText(fmt(old[0]));
                    o.setText(fmt(old[1]));
                    h.setText(fmt(old[2]));
                    w.setText(fmt(old[3]));
                }

                double exam =
                        db.exam(
                                st[1],
                                sub.getSelectedItem().toString(),
                                sem.getSelectedItemPosition() + 1
                        );

                ex.setText(
                        exam == 0
                                ? ""
                                : fmt(exam)
                );

                row.addView(
                        a,
                        new LinearLayout.LayoutParams(
                                60,
                                50
                        )
                );

                row.addView(
                        o,
                        new LinearLayout.LayoutParams(
                                60,
                                50
                        )
                );

                row.addView(
                        h,
                        new LinearLayout.LayoutParams(
                                60,
                                50
                        )
                );

                row.addView(
                        w,
                        new LinearLayout.LayoutParams(
                                60,
                                50
                        )
                );

                row.addView(
                        ex,
                        new LinearLayout.LayoutParams(
                                70,
                                50
                        )
                );

                row.setTag(
                        new EditText[]{
                                a,
                                o,
                                h,
                                w,
                                ex
                        }
                );

                list.addView(row);
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
                btn("💾 حفظ الكل");

        content.addView(save);

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

                for (int i = 0;
                     i < list.getChildCount() &&
                     i < students.size();
                     i++) {

                    String sid =
                            students.get(i)[1];

                    EditText[] z =
                            (EditText[])
                                    list.getChildAt(i)
                                            .getTag();

                    db.setMonthlyScore(
                            sid,
                            sub.getSelectedItem().toString(),
                            sem.getSelectedItemPosition() + 1,
                            1,
                            par(z[0]),
                            par(z[1]),
                            par(z[2]),
                            par(z[3])
                    );

                    db.setExam(
                            sid,
                            sub.getSelectedItem().toString(),
                            sem.getSelectedItemPosition() + 1,
                            par(z[4])
                    );
                }

                writable.setTransactionSuccessful();

                Toast.makeText(
                        this,
                        "تم الحفظ",
                        Toast.LENGTH_SHORT
                ).show();

            } finally {

                writable.endTransaction();
            }
        });
    }

    EditText box(String h) {

        EditText e =
                new EditText(this);

        e.setHint(h);
        e.setTextSize(10);

        e.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        e.setGravity(
                Gravity.CENTER
        );

        return e;
    }

    double par(EditText e) {

        try {

            return Double.parseDouble(
                    e.getText().toString()
            );

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

        page("📅 الجدول");

        for (String d : DAYS) {

            addCard(
                    d,
                    db.daySchedule(d)
            );
        }
    }

    /*
     * التقارير.
     */
    void reports() {

        page("📈 التقارير");

        addCard(
                "إجمالي الطلاب",
                db.count("students") + ""
        );

        addCard(
                "إجمالي المعلمين",
                db.count("teachers") + ""
        );

        addCard(
                "حضور اليوم",
                "حاضر: " +
                db.todayPresent() +
                " - غائب: " +
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
                    btn("＋ إعلان");

            content.addView(a);

            a.setOnClickListener(v -> {

                EditText e =
                        new EditText(this);

                e.setHint("نص الإعلان");

                new AlertDialog.Builder(this)
                        .setView(e)
                        .setPositiveButton(
                                "نشر",
                                (d, w) -> {

                                    db.addAnnouncement(
                                            e.getText().toString()
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
                    r[1],
                    r[0]
            );
        }
    }

    /*
     * المستخدمون.
     */
    void users() {

        page("🔐 المستخدمون");

        /*
         * حماية الصفحة نفسها.
         */
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
                    u[3] +
                    " - " +
                    u[0],
                    "الدور: " + u[1]
            );
        }

        Button a =
                btn("＋ مستخدم جديد");

        content.addView(a);

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
                            android.R.layout.simple_spinner_dropdown_item,
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
                    .setView(l)
                    .setPositiveButton(
                            "حفظ",
                            (d, w) -> {

                                db.addUser(
                                        name.getText().toString(),
                                        ro.getSelectedItem().toString(),
                                        user.getText().toString(),
                                        pass.getText().toString()
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

        Button b =
                btn("💾 نسخ احتياطي");

        content.addView(b);

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
     * إنشاء ملف PDF للطالب.
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

            i.setType("application/pdf");

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
                        "تم النسخ",
                        Toast.LENGTH_SHORT
                ).show();

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}
