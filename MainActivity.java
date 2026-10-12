package com.jafar.school;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.*;
import android.text.InputType;

import androidx.core.content.FileProvider;

import java.io.*;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root, content;
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

    final String[] SUBJECTS_7 = {
        "القرآن الكريم",
        "التربية الإسلامية",
        "اللغة العربية",
        "الرياضيات",
        "العلوم",
        "الاجتماعيات",
        "اللغة الإنجليزية"
    };

    final Stack<Runnable> navHistory = new Stack<>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(BLUE);
        getWindow().getDecorView().setLayoutDirection(
            View.LAYOUT_DIRECTION_RTL
        );

        try {
            db = new DB(this);
            showLogin();
        } catch (Exception e) {
            showFatalError(e);
        }
    }

    void showFatalError(Exception e) {
        TextView t = new TextView(this);
        t.setPadding(dp(20), dp(20), dp(20), dp(20));
        t.setTextColor(RED);
        t.setTextSize(16);
        t.setText("تعذر تشغيل التطبيق:\n" + e.getMessage());
        setContentView(t);
    }

    @Override
    public void onBackPressed() {
        if (navHistory.size() > 1) {
            navHistory.pop();

            try {
                navHistory.peek().run();
            } catch (Exception e) {
                Toast.makeText(
                    this,
                    "تعذر الرجوع: " + e.getMessage(),
                    Toast.LENGTH_LONG
                ).show();
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

    int dp(int value) {
        return (int) (
            value * getResources().getDisplayMetrics().density
        );
    }

    GradientDrawable rounded(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    TextView tv(String text, int size, boolean bold) {
        TextView t = new TextView(this);

        t.setText(text == null ? "" : text);
        t.setTextSize(size);
        t.setTextColor(TEXT);
        t.setTypeface(
            null,
            bold ? Typeface.BOLD : Typeface.NORMAL
        );
        t.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        t.setPadding(dp(12), dp(8), dp(12), dp(8));

        return t;
    }

    Button btn(String text) {
        Button b = new Button(this);

        b.setText(text);
        b.setTextSize(14);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(8), dp(5), dp(8), dp(5));
        b.setBackground(rounded(BLUE, 14));

        return b;
    }

    Button smallBtn(String text, int color) {
        Button b = new Button(this);

        b.setText(text);
        b.setTextSize(12);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(3), dp(2), dp(3), dp(2));
        b.setBackground(rounded(color, 10));

        return b;
    }

    void base() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(root);
    }

    void navigateTo(Runnable action) {
        if (action == null) return;

        navHistory.push(action);

        try {
            action.run();
        } catch (Exception e) {
            Toast.makeText(
                this,
                "حدث خطأ: " + e.getMessage(),
                Toast.LENGTH_LONG
            ).show();
        }
    }

    void page(String title) {
        base();

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(5), dp(5), dp(5), dp(5));
        top.setBackgroundColor(WHITE);

        Button back = smallBtn("رجوع ‹", BLUE);
        top.addView(
            back,
            new LinearLayout.LayoutParams(dp(82), dp(46))
        );
        back.setOnClickListener(v -> goBack());

        TextView heading = tv(title, 17, true);
        heading.setGravity(Gravity.CENTER);

        top.addView(
            heading,
            new LinearLayout.LayoutParams(0, dp(48), 1)
        );

        root.addView(
            top,
            new LinearLayout.LayoutParams(-1, dp(58))
        );

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(10), dp(8), dp(10), dp(25));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setDescendantFocusability(
            ViewGroup.FOCUS_AFTER_DESCENDANTS
        );
        scroll.addView(content);

        root.addView(
            scroll,
            new LinearLayout.LayoutParams(-1, 0, 1)
        );
    }

    void addCard(String title, String body) {
        if (content == null) return;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackground(rounded(WHITE, 12));

        card.addView(tv(title, 16, true));
        card.addView(tv(body, 14, false));

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);

        p.setMargins(dp(3), dp(5), dp(3), dp(5));
        content.addView(card, p);
    }

    EditText edit(String hint) {
        EditText e = new EditText(this);

        e.setHint(hint);
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setPadding(dp(10), dp(5), dp(10), dp(5));
        e.setFocusable(true);
        e.setFocusableInTouchMode(true);
        e.setClickable(true);

        return e;
    }

    EditText scoreEdit(String hint) {
        EditText e = edit(hint);

        e.setGravity(Gravity.CENTER);
        e.setInputType(
            InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        return e;
    }

    double parse(EditText e) {
        try {
            String value = e.getText().toString().trim();

            if (value.isEmpty()) return 0;

            value = value.replace(',', '.')
                         .replace('٫', '.');

            return Double.parseDouble(value);
        } catch (Exception ex) {
            return 0;
        }
    }

    double limit(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    String formatNumber(double value) {
        if (value == Math.round(value)) {
            return String.valueOf((int) value);
        }

        return String.format(Locale.US, "%.1f", value);
    }

    boolean admin() {
        if (role == null) return false;

        String r = role.trim();

        return r.equals("القائم بأعمال المدير")
            || r.equals("مدير المدرسة")
            || r.equals("مدير النظام");
    }

    boolean isTeacher() {
        return role != null && role.trim().equals("معلم");
    }

    int safeCount(String table) {
        try {
            return db.count(table);
        } catch (Exception e) {
            return 0;
        }
    }

    // ---------------- تسجيل الدخول ----------------

    void showLogin() {
        navHistory.clear();

        base();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(22), dp(22), dp(22), dp(22));

        scroll.addView(box);

        root.addView(
            scroll,
            new LinearLayout.LayoutParams(-1, 0, 1)
        );

        TextView logo = tv("🏫", 48, true);
        logo.setGravity(Gravity.CENTER);
        box.addView(logo);

        TextView title = tv(
            "مدرسة جعفر بن أبي طالب الأساسية",
            21,
            true
        );
        title.setGravity(Gravity.CENTER);
        box.addView(title);

        TextView address = tv(
            "الجمهورية اليمنية - إب - مذيخرة - الأشعوب\n"
                + "نظام إدارة المدرسة",
            13,
            false
        );
        address.setGravity(Gravity.CENTER);
        box.addView(address);

        EditText username = edit("اسم المستخدم");
        EditText password = edit("كلمة المرور");

        password.setInputType(
            InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        box.addView(
            username,
            new LinearLayout.LayoutParams(-1, dp(55))
        );

        box.addView(
            password,
            new LinearLayout.LayoutParams(-1, dp(55))
        );

        Button login = btn("دخول");

        box.addView(
            login,
            new LinearLayout.LayoutParams(-1, dp(52))
        );

        TextView hint = tv("الحساب الافتراضي: admin / 1234", 12, false);
        hint.setGravity(Gravity.CENTER);
        box.addView(hint);

        login.setOnClickListener(v -> {
            String u = username.getText().toString().trim();
            String p = password.getText().toString();

            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(
                    this,
                    "أدخل اسم المستخدم وكلمة المرور",
                    Toast.LENGTH_SHORT
                ).show();
                return;
            }

            login.setEnabled(false);

            try {
                String[] a = db.authenticate(u, p);

                if (a == null || a.length < 5) {
                    Toast.makeText(
                        this,
                        "اسم المستخدم أو كلمة المرور غير صحيحة",
                        Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                currentUser = a[0] == null ? "" : a[0];
                role = a[1] == null ? "" : a[1];
                assignedGrade = a[2] == null ? "" : a[2];
                assignedClass = a[3] == null ? "" : a[3];
                currentUsername = a[4] == null ? "" : a[4];

                navHistory.clear();
                navigateTo(this::showHome);

            } catch (Exception ex) {
                Toast.makeText(
                    this,
                    "تعذر تسجيل الدخول: " + ex.getMessage(),
                    Toast.LENGTH_LONG
                ).show();
            } finally {
                login.setEnabled(true);
            }
        });
    }

    // ---------------- الرئيسية ----------------

    void showHome() {
        base();

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(8), dp(6), dp(8), dp(6));
        header.setBackgroundColor(WHITE);

        header.addView(
            tv(role + "\n" + currentUser, 13, true),
            new LinearLayout.LayoutParams(0, -2, 1)
        );

        Button logout = btn("خروج");
        header.addView(
            logout,
            new LinearLayout.LayoutParams(dp(82), dp(44))
        );

        logout.setOnClickListener(v -> showLogin());

        root.addView(
            header,
            new LinearLayout.LayoutParams(-1, -2)
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(8), dp(8), dp(8), dp(20));

        scroll.addView(content);

        root.addView(
            scroll,
            new LinearLayout.LayoutParams(-1, 0, 1)
        );

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);

        String[] values = {
            "الطلاب\n" + safeCount("students"),
            "المعلمون\n" + safeCount("teachers"),
            "حاضر اليوم\n" + db.todayPresent(),
            "غائب اليوم\n" + db.todayAbsent()
        };

        for (String value : values) {
            TextView item = tv(value, 12, true);
            item.setGravity(Gravity.CENTER);
            item.setBackground(rounded(WHITE, 10));

            LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(0, dp(70), 1);

            p.setMargins(dp(2), dp(3), dp(2), dp(6));
            stats.addView(item, p);
        }

        content.addView(stats);

        ArrayList<String[]> items = new ArrayList<>();

        items.add(new String[]{"👨‍🎓 الطلاب", "students"});
        items.add(new String[]{"📊 الدرجات", "grades"});
        items.add(new String[]{"✅ الحضور", "attendance"});
        items.add(new String[]{"📅 الجدول", "timetable"});
        items.add(new String[]{"👨‍🏫 المعلمون", "teachers"});
        items.add(new String[]{"🏫 الصفوف", "classes"});
        items.add(new String[]{"📈 التقارير", "reports"});
        items.add(new String[]{"🔔 الإعلانات", "announcements"});

        if (admin()) {
            items.add(new String[]{"🔐 المستخدمون", "users"});
            items.add(new String[]{"📁 النسخ الاحتياطي", "files"});
        }

        for (int i = 0; i < items.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            content.addView(
                row,
                new LinearLayout.LayoutParams(-1, dp(72))
            );

            for (int j = i; j < Math.min(i + 2, items.size()); j++) {
                String[] item = items.get(j);

                Button b = btn(item[0]);

                LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(0, dp(62), 1);

                p.setMargins(dp(3), dp(3), dp(3), dp(3));
                row.addView(b, p);

                b.setOnClickListener(v -> open(item[1]));
            }
        }

        String announcements;

        try {
            announcements = db.latestAnnouncements();
        } catch (Exception e) {
            announcements = "لا توجد إعلانات";
        }

        addCard("📢 آخر الإعلانات", announcements);
    }

    void open(String key) {
        switch (key) {
            case "students":
                navigateTo(this::students);
                break;
            case "grades":
                navigateTo(this::grades);
                break;
            case "attendance":
                navigateTo(this::attendance);
                break;
            case "timetable":
                navigateTo(this::timetable);
                break;
            case "teachers":
                navigateTo(this::teachers);
                break;
            case "classes":
                navigateTo(this::classes);
                break;
            case "reports":
                navigateTo(this::reports);
                break;
            case "announcements":
                navigateTo(this::announcements);
                break;
            case "users":
                if (admin()) navigateTo(this::users);
                break;
            case "files":
                if (admin()) navigateTo(this::files);
                break;
        }
    }

    // ---------------- الطلاب ----------------

    void students() {
        page("👨‍🎓 الطلاب");

        EditText search = edit("ابحث باسم الطالب أو رقمه");
        content.addView(
            search,
            new LinearLayout.LayoutParams(-1, dp(52))
        );

        if (admin()) {
            Button add = btn("＋ إضافة طالب");

            content.addView(
                add,
                new LinearLayout.LayoutParams(-1, dp(52))
            );

            add.setOnClickListener(v -> studentDialog(null));
        }

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list);

        Runnable refresh = () -> {
            list.removeAllViews();

            for (String[] s : db.students(
                search.getText().toString().trim()
            )) {
                if (s.length < 4) continue;

                Button b = btn(
                    s[0]
                        + "\nالرقم: " + s[1]
                        + " | الصف: " + s[2]
                        + " / " + s[3]
                );

                b.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

                list.addView(
                    b,
                    new LinearLayout.LayoutParams(-1, dp(68))
                );

                String id = s[1];

                b.setOnClickListener(
                    v -> navigateTo(() -> studentDetails(id))
                );
            }
        };

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(
                CharSequence s, int start, int count, int after) {}

            public void onTextChanged(
                CharSequence s, int start, int before, int count) {
                refresh.run();
            }

            public void afterTextChanged(Editable e) {}
        });

        refresh.run();
    }

    void studentDialog(String id) {
        boolean editing = id != null;
        String[] old = editing ? db.student(id) : null;

        EditText name = edit("اسم الطالب");
        EditText number = edit("رقم الطالب");
        EditText grade = edit("الصف");
        EditText classroom = edit("الشعبة");
        EditText parent = edit("اسم ولي الأمر");
        EditText phone = edit("رقم الهاتف");

        if (old != null) {
            name.setText(old[0]);
            number.setText(old[1]);
            number.setEnabled(false);
            grade.setText(old[2]);
            classroom.setText(old[3]);
            parent.setText(old[4]);
            phone.setText(old[5]);
        }

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(8), dp(4), dp(8), dp(4));

        for (EditText e : new EditText[]{
            name, number, grade, classroom, parent, phone
        }) {
            box.addView(
                e,
                new LinearLayout.LayoutParams(-1, dp(50))
            );
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(box);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(editing ? "تعديل بيانات الطالب" : "إضافة طالب")
            .setView(scroll)
            .setPositiveButton("حفظ", null)
            .setNegativeButton("إلغاء", null)
            .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String n = name.getText().toString().trim();
                    String sid = number.getText().toString().trim();

                    if (n.isEmpty() || sid.isEmpty()) {
                        Toast.makeText(
                            this,
                            "اسم الطالب ورقمه مطلوبان",
                            Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    boolean saved = db.saveStudent(
                        n,
                        sid,
                        grade.getText().toString().trim(),
                        classroom.getText().toString().trim(),
                        parent.getText().toString().trim(),
                        phone.getText().toString().trim()
                    );

                    if (saved) {
                        dialog.dismiss();
                        students();
                        Toast.makeText(
                            this,
                            "تم حفظ بيانات الطالب",
                            Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                            this,
                            "تعذر حفظ بيانات الطالب",
                            Toast.LENGTH_LONG
                        ).show();
                    }
                });
        });

        dialog.show();
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );
    }

    ArrayList<String[]> studentsForClass(
        String grade, String classroom) {

        ArrayList<String[]> result =
            db.studentsInClass(grade, classroom);

        if (!result.isEmpty()) return result;

        for (String[] s : db.students("")) {
            if (s.length >= 4
                && grade.equals(s[2])
                && classroom.equals(s[3])) {
                result.add(new String[]{s[0], s[1]});
            }
        }

        return result;
    }

    void studentDetails(String id) {
        page("👨‍🎓 ملف الطالب");

        String[] s = db.student(id);

        if (s == null) {
            addCard("تنبيه", "لم يتم العثور على بيانات الطالب.");
            return;
        }

        addCard(
            "بيانات الطالب",
            "الاسم: " + s[0]
                + "\nالرقم: " + s[1]
                + "\nالصف: " + s[2] + " / " + s[3]
                + "\nولي الأمر: " + s[4]
                + "\nالهاتف: " + s[5]
        );

        double grandTotal = 0;
        boolean allPass = true;

        for (String subject : SUBJECTS_7) {
            double first =
                db.getMahsala(id, subject, 1)
                    + db.getExam(id, subject, 1);

            double second =
                db.getMahsala(id, subject, 2)
                    + db.getExam(id, subject, 2);

            double annual = first + second;
            grandTotal += annual;

            if (annual < 50) allPass = false;

            addCard(
                subject,
                "الفصل الأول: " + formatNumber(first) + " /50"
                    + "\nالفصل الثاني: " + formatNumber(second) + " /50"
                    + "\nالمجموع السنوي: " + formatNumber(annual) + " /100"
                    + "\nالتقدير: " + DB.gradeFor(annual)
                    + "\nالنتيجة: " + (annual >= 50 ? "ناجح" : "راسب")
            );
        }

        double max = SUBJECTS_7.length * 100.0;
        double percent = grandTotal / max * 100.0;
        boolean passed = allPass && percent >= 50;

        addCard(
            "المحصلة النهائية",
            "المجموع: " + formatNumber(grandTotal) + " / " + (int) max
                + "\nالنسبة: " + formatNumber(percent) + "%"
                + "\nالتقدير: " + DB.gradeFor(percent)
                + "\nالنتيجة: " + (passed ? "ناجح" : "راسب")
        );

        Button certificate = btn("🖨️ شهادة النتيجة PDF");
        content.addView(
            certificate,
            new LinearLayout.LayoutParams(-1, dp(52))
        );
        certificate.setOnClickListener(v -> generateCertificatePdf(id));

        if (admin()) {
            Button edit = btn("✏️ تعديل بيانات الطالب");

            content.addView(
                edit,
                new LinearLayout.LayoutParams(-1, dp(52))
            );

            edit.setOnClickListener(v -> studentDialog(id));
        }

        addCard(
            "ملخص الحضور",
            db.studentAttendanceSummary(id)
        );
    }

    // ---------------- الصفوف ----------------

    ArrayList<String[]> allowedClasses() {
        ArrayList<String[]> all = db.classes();

        if (admin()) return all;

        ArrayList<String[]> result = new ArrayList<>();

        if (isTeacher()) {
            for (String[] c : all) {
                if (c.length >= 2
                    && c[0].equals(assignedGrade)
                    && c[1].equals(assignedClass)) {
                    result.add(c);
                }
            }
        }

        return result;
    }

    void classes() {
        page("🏫 الصفوف");

        ArrayList<String[]> list = allowedClasses();

        if (list.isEmpty()) {
            addCard("لا توجد صفوف", "أضف الطلاب وحدد الصف والشعبة.");
            return;
        }

        for (String[] c : list) {
            addCard(
                "الصف " + c[0] + " - الشعبة " + c[1],
                "عدد الطلاب: " + studentsForClass(c[0], c[1]).size()
            );
        }
    }

    // ---------------- الدرجات ----------------

    void grades() {
        page("📊 الدرجات");

        addCard(
            "إدخال الدرجات",
            "اختر الصف والشعبة، ثم اضغط على خانة المادة لإدخال المحصلة والاختبار."
                + "\nالمحصلة: 20 درجة"
                + "\nالاختبار: 30 درجة"
        );

        ArrayList<String[]> classes = allowedClasses();

        if (classes.isEmpty()) {
            addCard(
                "لا توجد صفوف متاحة",
                "أضف الطلاب وحدد صفوفهم وشعبهم أولاً."
            );
            return;
        }

        for (String[] c : classes) {
            if (c.length < 2) continue;

            String g = c[0];
            String cl = c[1];

            Button b = btn("📚 الصف " + g + " - الشعبة " + cl);

            content.addView(
                b,
                new LinearLayout.LayoutParams(-1, dp(54))
            );

            b.setOnClickListener(
                v -> navigateTo(() -> classGrades(g, cl))
            );
        }
    }

    void classGrades(String grade, String classroom) {
        page("📊 درجات " + grade + " / " + classroom);

        Spinner semester = new Spinner(this);

        semester.setAdapter(
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"الفصل الأول", "الفصل الثاني"}
            )
        );

        content.addView(
            semester,
            new LinearLayout.LayoutParams(-1, dp(50))
        );

        TextView instructions = tv(
            "اضغط على زر المادة في صف الطالب لإدخال الدرجات.",
            13,
            false
        );
        content.addView(instructions);

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setFillViewport(false);

        LinearLayout table = new LinearLayout(this);
        table.setOrientation(LinearLayout.VERTICAL);
        table.setPadding(0, dp(5), 0, dp(10));

        horizontal.addView(table);

        content.addView(
            horizontal,
            new LinearLayout.LayoutParams(-1, -2)
        );

        Runnable render = () -> {
            table.removeAllViews();

            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setBackgroundColor(WHITE);

            TextView studentHeading = tv("الطالب", 12, true);
            studentHeading.setGravity(Gravity.CENTER);

            header.addView(
                studentHeading,
                new LinearLayout.LayoutParams(dp(155), dp(55))
            );

            for (String subject : SUBJECTS_7) {
                TextView h = tv(subject, 11, true);
                h.setGravity(Gravity.CENTER);
                header.addView(
                    h,
                    new LinearLayout.LayoutParams(dp(125), dp(55))
                );
            }

            table.addView(header);

            int sem = semester.getSelectedItemPosition() + 1;

            ArrayList<String[]> students =
                studentsForClass(grade, classroom);

            if (students.isEmpty()) {
                TextView empty = tv(
                    "لا يوجد طلاب في هذا الصف والشعبة.",
                    14,
                    true
                );
                table.addView(empty);
                return;
            }

            for (String[] st : students) {
                if (st.length < 2) continue;

                String sid = st[1];
                String studentName = st[0];

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setBackground(rounded(WHITE, 8));

                TextView name = tv(
                    studentName + "\n" + sid,
                    11,
                    true
                );

                row.addView(
                    name,
                    new LinearLayout.LayoutParams(dp(155), dp(66))
                );

                for (String subject : SUBJECTS_7) {
                    double m = db.getMahsala(sid, subject, sem);
                    double e = db.getExam(sid, subject, sem);
                    double total = m + e;

                    String label = (m == 0 && e == 0)
                        ? "إدخال"
                        : formatNumber(total) + " /50";

                    Button cell = smallBtn(
                        label,
                        (m == 0 && e == 0) ? ORANGE : GREEN
                    );

                    LinearLayout.LayoutParams p =
                        new LinearLayout.LayoutParams(dp(125), dp(58));

                    p.setMargins(dp(2), dp(3), dp(2), dp(3));
                    row.addView(cell, p);

                    cell.setOnClickListener(v -> {
                        if (!admin() && !isTeacher()) {
                            Toast.makeText(
                                this,
                                "ليست لديك صلاحية إدخال الدرجات",
                                Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        scoreDialog(
                            sid,
                            studentName,
                            subject,
                            sem,
                            cell
                        );
                    });
                }

                table.addView(row);
            }
        };

        semester.setOnItemSelectedListener(
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id) {
                    render.run();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            }
        );

        render.run();

        Button sheet = btn("📋 كشف النتائج السنوي PDF");

        content.addView(
            sheet,
            new LinearLayout.LayoutParams(-1, dp(52))
        );

        sheet.setOnClickListener(
            v -> generateClassSheetPdf(grade, classroom)
        );

        Button cards = btn("🖨️ بطاقات نتائج الفصل PDF");

        content.addView(
            cards,
            new LinearLayout.LayoutParams(-1, dp(52))
        );

        cards.setOnClickListener(
            v -> generateExamCardsPdf(
                grade,
                classroom,
                semester.getSelectedItemPosition() + 1
            )
        );
    }

    void scoreDialog(
        String studentId,
        String studentName,
        String subject,
        int sem,
        Button cell) {

        EditText mahsala = scoreEdit("المحصلة من 20");
        EditText exam = scoreEdit("الاختبار من 30");

        double oldM = db.getMahsala(studentId, subject, sem);
        double oldE = db.getExam(studentId, subject, sem);

        mahsala.setText(formatNumber(oldM));
        exam.setText(formatNumber(oldE));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(5), dp(10), dp(5));

        box.addView(
            mahsala,
            new LinearLayout.LayoutParams(-1, dp(55))
        );

        box.addView(
            exam,
            new LinearLayout.LayoutParams(-1, dp(55))
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(studentName + "\n" + subject)
            .setView(box)
            .setPositiveButton("حفظ", null)
            .setNegativeButton("إلغاء", null)
            .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String mText = mahsala.getText().toString().trim();
                    String eText = exam.getText().toString().trim();

                    if (mText.isEmpty() || eText.isEmpty()) {
                        Toast.makeText(
                            this,
                            "أدخل المحصلة والاختبار",
                            Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    double m = parse(mahsala);
                    double e = parse(exam);

                    if (m < 0 || m > 20 || e < 0 || e > 30) {
                        Toast.makeText(
                            this,
                            "المحصلة من 0 إلى 20 والاختبار من 0 إلى 30",
                            Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    try {
                        db.setMahsala(studentId, subject, sem, m);
                        db.setExam(studentId, subject, sem, e);

                        cell.setText(formatNumber(m + e) + " /50");
                        cell.setBackground(rounded(GREEN, 10));

                        Toast.makeText(
                            this,
                            "تم حفظ درجات " + studentName,
                            Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();

                    } catch (Exception ex) {
                        Toast.makeText(
                            this,
                            "تعذر حفظ الدرجات: " + ex.getMessage(),
                            Toast.LENGTH_LONG
                        ).show();
                    }
                });
        });

        dialog.show();

        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        mahsala.setOnClickListener(v -> {
            mahsala.requestFocus();
            showKeyboard(mahsala);
        });

        exam.setOnClickListener(v -> {
            exam.requestFocus();
            showKeyboard(exam);
        });
    }

    void showKeyboard(EditText field) {
        field.requestFocus();

        InputMethodManager imm =
            (InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE
            );

        if (imm != null) {
            imm.showSoftInput(
                field,
                InputMethodManager.SHOW_IMPLICIT
            );
        }
    }

    // ---------------- الحضور ----------------

    void attendance() {
        page("✅ الحضور والغياب");

        addCard(
            "تسجيل الحضور",
            "اختر الصف والشعبة، ثم حدد حالة كل طالب."
        );

        ArrayList<String[]> classes = allowedClasses();

        if (classes.isEmpty()) {
            addCard("لا توجد صفوف", "أضف الطلاب أولاً.");
            return;
        }

        for (String[] c : classes) {
            if (c.length < 2) continue;

            String grade = c[0];
            String classroom = c[1];

            Button b = btn(
                "الصف " + grade + " - الشعبة " + classroom
            );

            content.addView(
                b,
                new LinearLayout.LayoutParams(-1, dp(52))
            );

            b.setOnClickListener(
                v -> navigateTo(
                    () -> attendanceClass(grade, classroom)
                )
            );
        }
    }

    void attendanceClass(String grade, String classroom) {
        page("الحضور: " + grade + " / " + classroom);

        ArrayList<String[]> students =
            studentsForClass(grade, classroom);

        if (students.isEmpty()) {
            addCard("لا يوجد طلاب", "أضف الطلاب إلى هذا الصف أولاً.");
            return;
        }

        addCard(
            "عدد الطلاب",
            String.valueOf(students.size())
        );

        for (String[] s : students) {
            if (s.length < 2) continue;

            String studentId = s[1];
            String studentName = s[0];

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(8), dp(6), dp(8), dp(6));
            row.setBackground(rounded(WHITE, 10));

            row.addView(tv(studentName, 14, true));

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);

            String[] statuses = {
                "حاضر", "غائب", "متأخر", "غائب بعذر"
            };

            int[] colors = {GREEN, RED, ORANGE, BLUE};

            for (int i = 0; i < statuses.length; i++) {
                String status = statuses[i];

                Button b = smallBtn(status, colors[i]);

                LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(0, dp(44), 1);

                p.setMargins(dp(2), dp(3), dp(2), dp(3));
                actions.addView(b, p);

                b.setOnClickListener(v -> {
                    if (!admin() && !isTeacher()) {
                        Toast.makeText(
                            this,
                            "ليست لديك صلاحية تسجيل الحضور",
                            Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    boolean saved = db.setAttendance(studentId, status);

                    Toast.makeText(
                        this,
                        saved
                            ? "تم تسجيل " + status + " للطالب " + studentName
                            : "تعذر حفظ الحضور",
                        Toast.LENGTH_SHORT
                    ).show();
                });
            }

            row.addView(actions);

            LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -2);

            p.setMargins(0, dp(4), 0, dp(4));
            content.addView(row, p);
        }
    }

    // ---------------- المعلمون ----------------

    void teachers() {
        page("👨‍🏫 المعلمون");

        if (admin()) {
            Button add = btn("＋ إضافة معلم");

            content.addView(
                add,
                new LinearLayout.LayoutParams(-1, dp(52))
            );

            add.setOnClickListener(v -> teacherDialog());
        }

        CursorHelper:
        {
            android.database.Cursor c = null;

            try {
                c = getReadableTeachersCursor();

                if (c == null || !c.moveToFirst()) {
                    addCard("المعلمون", "لا توجد بيانات معلمين.");
                    break CursorHelper;
                }

                do {
                    String name = c.getString(1);
                    String phone = c.isNull(2) ? "" : c.getString(2);
                    String subject = c.isNull(3) ? "" : c.getString(3);
                    String grade = c.isNull(4) ? "" : c.getString(4);
                    String classroom = c.isNull(5) ? "" : c.getString(5);

                    addCard(
                        name,
                        "الهاتف: " + phone
                            + "\nالمادة: " + subject
                            + "\nالصف: " + grade + " / " + classroom
                    );

                } while (c.moveToNext());

            } catch (Exception e) {
                addCard("المعلمون", "تعذر تحميل بيانات المعلمين.");
            } finally {
                if (c != null) c.close();
            }
        }
    }

    android.database.Cursor getReadableTeachersCursor() {
        return db.getReadableDatabase().rawQuery(
            "SELECT id,name,phone,subject,grade,class FROM teachers ORDER BY name",
            null
        );
    }

    void teacherDialog() {
        EditText id = edit("رقم المعلم");
        EditText name = edit("اسم المعلم");
        EditText phone = edit("رقم الهاتف");
        EditText subject = edit("المادة");
        EditText grade = edit("الصف المسند");
        EditText classroom = edit("الشعبة المسندة");

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);

        for (EditText e : new EditText[]{
            id, name, phone, subject, grade, classroom
        }) {
            box.addView(
                e,
                new LinearLayout.LayoutParams(-1, dp(48))
            );
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(box);

        new AlertDialog.Builder(this)
            .setTitle("إضافة معلم")
            .setView(scroll)
            .setPositiveButton("حفظ", (d, w) -> {
                if (id.getText().toString().trim().isEmpty()
                    || name.getText().toString().trim().isEmpty()) {
                    Toast.makeText(
                        this,
                        "رقم المعلم واسمه مطلوبان",
                        Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                android.content.ContentValues cv =
                    new android.content.ContentValues();

                cv.put("id", id.getText().toString().trim());
                cv.put("name", name.getText().toString().trim());
                cv.put("phone", phone.getText().toString().trim());
                cv.put("subject", subject.getText().toString().trim());
                cv.put("grade", grade.getText().toString().trim());
                cv.put("class", classroom.getText().toString().trim());

                long result = db.getWritableDatabase().insertWithOnConflict(
                    "teachers",
                    null,
                    cv,
                    android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                );

                Toast.makeText(
                    this,
                    result == -1 ? "تعذر الحفظ" : "تم حفظ المعلم",
                    Toast.LENGTH_SHORT
                ).show();

                teachers();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    // ---------------- الجدول ----------------

    void timetable() {
        page("📅 الجدول المدرسي");

        addCard(
            "أيام الدراسة",
            "السبت، الأحد، الاثنين، الثلاثاء، الأربعاء"
                + "\nالخميس والجمعة: إجازة"
        );

        android.database.Cursor c = null;

        try {
            c = db.getReadableDatabase().rawQuery(
                "SELECT grade,class,day,period,subject,teacher " +
                "FROM timetable ORDER BY grade,class,day,period",
                null
            );

            if (!c.moveToFirst()) {
                addCard("الجدول", "لم تتم إضافة حصص إلى الجدول بعد.");
                return;
            }

            do {
                addCard(
                    "الصف " + c.getString(0) + " / " + c.getString(1),
                    "اليوم: " + c.getString(2)
                        + "\nالحصة: " + c.getInt(3)
                        + "\nالمادة: " + c.getString(4)
                        + "\nالمعلم: " + c.getString(5)
                );
            } while (c.moveToNext());

        } catch (Exception e) {
            addCard("الجدول", "تعذر تحميل الجدول.");
        } finally {
            if (c != null) c.close();
        }
    }

    // ---------------- الإعلانات ----------------

    void announcements() {
        page("🔔 الإعلانات");

        if (admin()) {
            Button add = btn("＋ إضافة إعلان");

            content.addView(
                add,
                new LinearLayout.LayoutParams(-1, dp(52))
            );

            add.setOnClickListener(v -> announcementDialog());
        }

        android.database.Cursor c = null;

        try {
            c = db.getReadableDatabase().rawQuery(
                "SELECT title,body,date FROM announcements ORDER BY id DESC",
                null
            );

            if (!c.moveToFirst()) {
                addCard("الإعلانات", "لا توجد إعلانات.");
                return;
            }

            do {
                addCard(
                    c.isNull(0) ? "" : c.getString(0),
                    (c.isNull(1) ? "" : c.getString(1))
                        + "\nالتاريخ: "
                        + (c.isNull(2) ? "" : c.getString(2))
                );
            } while (c.moveToNext());

        } catch (Exception e) {
            addCard("الإعلانات", "تعذر تحميل الإعلانات.");
        } finally {
            if (c != null) c.close();
        }
    }

    void announcementDialog() {
        EditText title = edit("عنوان الإعلان");
        EditText body = edit("نص الإعلان");

        body.setSingleLine(false);
        body.setMinLines(3);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(title);
        box.addView(body);

        new AlertDialog.Builder(this)
            .setTitle("إضافة إعلان")
            .setView(box)
            .setPositiveButton("حفظ", (d, w) -> {
                boolean saved = db.saveAnnouncement(
                    title.getText().toString().trim(),
                    body.getText().toString().trim()
                );

                Toast.makeText(
                    this,
                    saved ? "تم حفظ الإعلان" : "تعذر حفظ الإعلان",
                    Toast.LENGTH_SHORT
                ).show();

                announcements();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    // ---------------- المستخدمون ----------------

    void users() {
        page("🔐 المستخدمون");

        if (!admin()) {
            addCard("تنبيه", "ليست لديك صلاحية إدارة المستخدمين.");
            return;
        }

        Button add = btn("＋ إضافة مستخدم");

        content.addView(
            add,
            new LinearLayout.LayoutParams(-1, dp(52))
        );

        add.setOnClickListener(v -> userDialog());

        android.database.Cursor c = null;

        try {
            c = db.getReadableDatabase().rawQuery(
                "SELECT name,username,role,grade,class FROM users ORDER BY name",
                null
            );

            while (c.moveToNext()) {
                addCard(
                    c.getString(0) + " (" + c.getString(1) + ")",
                    "الصلاحية: " + c.getString(2)
                        + "\nالصف: " + c.getString(3)
                        + "\nالشعبة: " + c.getString(4)
                );
            }
        } catch (Exception e) {
            addCard("المستخدمون", "تعذر تحميل المستخدمين.");
        } finally {
            if (c != null) c.close();
        }
    }

    void userDialog() {
        EditText name = edit("اسم المستخدم الظاهر");
        EditText username = edit("اسم الدخول");
        EditText password = edit("كلمة المرور");
        EditText grade = edit("الصف للمعلم");
        EditText classroom = edit("الشعبة للمعلم");

        Spinner roles = new Spinner(this);

        roles.setAdapter(
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                    "معلم",
                    "مدير المدرسة",
                    "القائم بأعمال المدير",
                    "مدير النظام"
                }
            )
        );

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);

        for (EditText e : new EditText[]{
            name, username, password, grade, classroom
        }) {
            box.addView(
                e,
                new LinearLayout.LayoutParams(-1, dp(48))
            );
        }

        box.addView(roles);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(box);

        new AlertDialog.Builder(this)
            .setTitle("إضافة مستخدم")
            .setView(scroll)
            .setPositiveButton("حفظ", (d, w) -> {
                String u = username.getText().toString().trim();
                String p = password.getText().toString();

                if (u.isEmpty() || p.isEmpty()) {
                    Toast.makeText(
                        this,
                        "اسم الدخول وكلمة المرور مطلوبان",
                        Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                android.content.ContentValues cv =
                    new android.content.ContentValues();

                cv.put("name", name.getText().toString().trim());
                cv.put("username", u);
                cv.put("pass", p);
                cv.put("role", roles.getSelectedItem().toString());
                cv.put("grade", grade.getText().toString().trim());
                cv.put("class", classroom.getText().toString().trim());

                long result = db.getWritableDatabase().insertWithOnConflict(
                    "users",
                    null,
                    cv,
                    android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                );

                Toast.makeText(
                    this,
                    result == -1 ? "تعذر حفظ المستخدم" : "تم حفظ المستخدم",
                    Toast.LENGTH_SHORT
                ).show();

                users();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    // ---------------- التقارير ----------------

    void reports() {
        page("📈 التقارير");

        addCard(
            "إحصاءات المدرسة",
            "عدد الطلاب: " + safeCount("students")
                + "\nعدد المعلمين: " + safeCount("teachers")
                + "\nحاضر اليوم: " + db.todayPresent()
                + "\nغائب اليوم: " + db.todayAbsent()
        );

        Button report = btn("📋 كشف نتائج الصف PDF");

        content.addView(
            report,
            new LinearLayout.LayoutParams(-1, dp(52))
        );

        report.setOnClickListener(v -> {
            ArrayList<String[]> classes = allowedClasses();

            if (classes.isEmpty()) {
                Toast.makeText(
                    this,
                    "لا توجد صفوف",
                    Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String[] options = new String[classes.size()];

            for (int i = 0; i < classes.size(); i++) {
                options[i] =
                    "الصف " + classes.get(i)[0]
                        + " / " + classes.get(i)[1];
            }

            new AlertDialog.Builder(this)
                .setTitle("اختر الصف")
                .setItems(options, (dialog, which) -> {
                    generateClassSheetPdf(
                        classes.get(which)[0],
                        classes.get(which)[1]
                    );
                })
                .setNegativeButton("إلغاء", null)
                .show();
        });
    }

    // ---------------- النسخ الاحتياطي ----------------

    void files() {
        page("📁 النسخ الاحتياطي");

        addCard(
            "البيانات",
            "يتم حفظ البيانات محليًا داخل قاعدة بيانات التطبيق."
        );

        Button backup = btn("إنشاء نسخة من قاعدة البيانات");

        content.addView(
            backup,
            new LinearLayout.LayoutParams(-1, dp(52))
        );

        backup.setOnClickListener(v -> {
            try {
                File source = getDatabasePath("school.db");
                File folder = new File(getExternalFilesDir(null), "backup");

                if (!folder.exists() && !folder.mkdirs()) {
                    throw new IOException("تعذر إنشاء مجلد النسخ الاحتياطي");
                }

                File destination = new File(
                    folder,
                    "school_backup_" + System.currentTimeMillis() + ".db"
                );

                try (
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(destination)
                ) {
                    byte[] buffer = new byte[8192];
                    int length;

                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }

                Toast.makeText(
                    this,
                    "تم إنشاء النسخة الاحتياطية",
                    Toast.LENGTH_LONG
                ).show();

            } catch (Exception e) {
                Toast.makeText(
                    this,
                    "تعذر إنشاء النسخة: " + e.getMessage(),
                    Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // ---------------- PDF: شهادة الطالب ----------------

    void generateCertificatePdf(String id) {
        String[] student = db.student(id);

        if (student == null) {
            Toast.makeText(
                this,
                "لم يتم العثور على الطالب",
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        PdfDocument document = new PdfDocument();

        int width = 595;
        int height = 842;

        PdfDocument.PageInfo info =
            new PdfDocument.PageInfo.Builder(width, height, 1).create();

        PdfDocument.Page page = document.startPage(info);
        Canvas canvas = page.getCanvas();

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);

        line.setColor(Color.BLACK);
        line.setStyle(Paint.Style.STROKE);

        p.setColor(Color.BLACK);
        p.setTextAlign(Paint.Align.CENTER);
        p.setFakeBoldText(true);
        p.setTextSize(13);

        canvas.drawRect(15, 15, width - 15, height - 15, line);

        canvas.drawText(
            "الجمهورية اليمنية - وزارة التربية والتعليم",
            width / 2f, 35, p
        );

        canvas.drawText(
            "مدرسة جعفر بن أبي طالب الأساسية",
            width / 2f, 55, p
        );

        p.setTextSize(17);

        canvas.drawText(
            "شهادة النتيجة السنوية",
            width / 2f, 88, p
        );

        p.setFakeBoldText(false);
        p.setTextSize(10);

        canvas.drawText(
            "الطالب: " + student[0]
                + "    الرقم: " + student[1],
            width / 2f, 112, p
        );

        canvas.drawText(
            "الصف: " + student[2] + " / " + student[3],
            width / 2f, 130, p
        );

        int top = 150;
        int rowHeight = 25;

        canvas.drawRect(25, top, width - 25, top + rowHeight, line);

        p.setFakeBoldText(true);
        p.setTextSize(9);

        canvas.drawText("المادة", 510, top + 17, p);
        canvas.drawText("فصل 1", 425, top + 17, p);
        canvas.drawText("فصل 2", 335, top + 17, p);
        canvas.drawText("المجموع", 245, top + 17, p);
        canvas.drawText("التقدير", 130, top + 17, p);

        p.setFakeBoldText(false);

        double grandTotal = 0;
        boolean allPass = true;

        int y = top + rowHeight;

        for (String subject : SUBJECTS_7) {
            double first =
                db.getMahsala(id, subject, 1)
                    + db.getExam(id, subject, 1);

            double second =
                db.getMahsala(id, subject, 2)
                    + db.getExam(id, subject, 2);

            double annual = first + second;
            grandTotal += annual;

            if (annual < 50) allPass = false;

            y += rowHeight;

            canvas.drawRect(25, y - rowHeight, width - 25, y, line);

            canvas.drawText(subject, 510, y - 8, p);
            canvas.drawText(formatNumber(first), 425, y - 8, p);
            canvas.drawText(formatNumber(second), 335, y - 8, p);
            canvas.drawText(formatNumber(annual), 245, y - 8, p);
            canvas.drawText(DB.gradeFor(annual), 130, y - 8, p);
        }

        double max = SUBJECTS_7.length * 100.0;
        double percent = grandTotal / max * 100.0;

        y += 35;

        p.setFakeBoldText(true);
        p.setTextSize(12);

        canvas.drawText(
            "المجموع: " + formatNumber(grandTotal) + " / " + (int) max,
            width / 2f, y, p
        );

        canvas.drawText(
            "النسبة: " + formatNumber(percent) + "%",
            width / 2f, y + 22, p
        );

        p.setColor(
            allPass && percent >= 50 ? GREEN : RED
        );

        p.setTextSize(18);

        canvas.drawText(
            allPass && percent >= 50 ? "ناجح" : "راسب",
            width / 2f, y + 55, p
        );

        document.finishPage(page);

        savePdf(document, "certificate_" + id + ".pdf");
    }

    // ---------------- PDF: كشف الصف ----------------

    void generateClassSheetPdf(String grade, String classroom) {
        ArrayList<String[]> students =
            studentsForClass(grade, classroom);

        if (students.isEmpty()) {
            Toast.makeText(
                this,
                "لا يوجد طلاب في هذا الصف",
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Collections.sort(students, (a, b) -> {
            double totalA = annualStudentTotal(a[1]);
            double totalB = annualStudentTotal(b[1]);
            return Double.compare(totalB, totalA);
        });

        PdfDocument document = new PdfDocument();

        int width = 842;
        int height = 595;
        int perPage = 18;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setColor(Color.BLACK);
        p.setTextAlign(Paint.Align.CENTER);
        line.setColor(Color.BLACK);
        line.setStyle(Paint.Style.STROKE);

        int pageNumber = 0;

        int[] widths = {
            32, 140, 52, 52, 52, 52,
            52, 60, 60, 50, 58, 60
        };

        String[] headers = {
            "م", "اسم الطالب", "قرآن", "إسلامية",
            "عربي", "رياضيات", "علوم", "اجتماعيات",
            "إنجليزي", "السلوك", "المجموع", "النتيجة"
        };

        int totalWidth = 0;

        for (int w : widths) totalWidth += w;

        int startX = (width - totalWidth) / 2;

        for (int start = 0; start < students.size(); start += perPage) {
            pageNumber++;

            PdfDocument.PageInfo info =
                new PdfDocument.PageInfo.Builder(
                    width, height, pageNumber
                ).create();

            PdfDocument.Page page = document.startPage(info);
            Canvas canvas = page.getCanvas();

            p.setTextSize(12);
            p.setFakeBoldText(true);

            canvas.drawRect(12, 12, width - 12, height - 12, line);

            canvas.drawText(
                "الجمهورية اليمنية - وزارة التربية والتعليم",
                width / 2f, 32, p
            );

            canvas.drawText(
                "مدرسة جعفر بن أبي طالب الأساسية",
                width / 2f, 49, p
            );

            p.setTextSize(13);

            canvas.drawText(
                "كشف النتائج السنوية - الصف " + grade + " / " + classroom,
                width / 2f, 68, p
            );

            p.setFakeBoldText(false);
            p.setTextSize(8);

            int top = 82;
            int rowHeight = 22;

            int x = startX;

            for (int col = 0; col < headers.length; col++) {
                int next = x + widths[col];

                canvas.drawRect(x, top, next, top + rowHeight, line);
                canvas.drawText(
                    headers[col],
                    (x + next) / 2f,
                    top + 14,
                    p
                );

                x = next;
            }

            int y = top + rowHeight;

            for (int i = start;
                 i < Math.min(start + perPage, students.size());
                 i++) {

                String[] st = students.get(i);

                String[] values = new String[12];

                values[0] = String.valueOf(i + 1);
                values[1] = st[0];

                for (int k = 0; k < SUBJECTS_7.length; k++) {
                    values[k + 2] = formatNumber(
                        db.annualTotal(st[1], SUBJECTS_7[k])
                    );
                }

                double total = annualStudentTotal(st[1]);

                values[9] = formatNumber(db.annualBehavior(st[1]));
                values[10] = formatNumber(total);
                values[11] = studentPassed(st[1]) ? "ناجح" : "راسب";

                x = startX;

                for (int col = 0; col < values.length; col++) {
                    int next = x + widths[col];

                    canvas.drawRect(x, y, next, y + rowHeight, line);

                    if (col == 1) {
                        p.setTextAlign(Paint.Align.RIGHT);
                        canvas.drawText(
                            values[col],
                            next - 4,
                            y + 14,
                            p
                        );
                        p.setTextAlign(Paint.Align.CENTER);
                    } else {
                        canvas.drawText(
                            values[col],
                            (x + next) / 2f,
                            y + 14,
                            p
                        );
                    }

                    x = next;
                }

                y += rowHeight;
            }

            p.setTextSize(8);

            canvas.drawText(
                "عدد الطلاب: " + students.size()
                    + "    الصفحة: " + pageNumber,
                width / 2f,
                height - 20,
                p
            );

            document.finishPage(page);
        }

        savePdf(
            document,
            "class_results_" + grade + "_" + classroom + ".pdf"
        );
    }

    double annualStudentTotal(String studentId) {
        double total = 0;

        for (String subject : SUBJECTS_7) {
            total += db.annualTotal(studentId, subject);
        }

        return total;
    }

    boolean studentPassed(String studentId) {
        for (String subject : SUBJECTS_7) {
            if (db.annualTotal(studentId, subject) < 50) {
                return false;
            }
        }

        return annualStudentTotal(studentId) >= 50 * SUBJECTS_7.length;
    }

    // ---------------- PDF: بطاقات النتائج ----------------

    void generateExamCardsPdf(
        String grade,
        String classroom,
        int semester) {

        ArrayList<String[]> students =
            studentsForClass(grade, classroom);

        if (students.isEmpty()) {
    Toast.makeText(
        this,
        "لا يوجد طلاب في الصف " + grade + " - " + classroom,
        Toast.LENGTH_LONG
    ).show();
    return;
}

Toast.makeText(
    this,
    "عدد الطلاب في كشف النتائج: " + students.size(),
    Toast.LENGTH_LONG
).show();
        PdfDocument document = new PdfDocument();

        int width = 595;
        int height = 842;
        int cardHeight = 258;

        for (int start = 0; start < students.size(); start += 3) {
            PdfDocument.PageInfo info =
                new PdfDocument.PageInfo.Builder(
                    width, height, start / 3 + 1
                ).create();

            PdfDocument.Page page = document.startPage(info);
            Canvas canvas = page.getCanvas();

            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);

            p.setColor(Color.BLACK);
            p.setTextAlign(Paint.Align.CENTER);

            line.setColor(Color.BLACK);
            line.setStyle(Paint.Style.STROKE);

            for (int k = 0; k < 3 && start + k < students.size(); k++) {
                String[] st = students.get(start + k);

                int top = 10 + k * (cardHeight + 10);

                canvas.drawRect(
                    10, top, width - 10, top + cardHeight, line
                );

                p.setTextSize(9);
                p.setFakeBoldText(true);

                canvas.drawText(
                    "الجمهورية اليمنية - وزارة التربية والتعليم",
                    width / 2f, top + 18, p
                );

                canvas.drawText(
                    "مدرسة جعفر بن أبي طالب الأساسية",
                    width / 2f, top + 34, p
                );

                p.setTextSize(11);

                canvas.drawText(
                    "نتيجة " + (semester == 1 ? "الفصل الأول" : "الفصل الثاني"),
                    width / 2f, top + 52, p
                );

                p.setFakeBoldText(false);
                p.setTextSize(9);

                canvas.drawText(
                    "الطالب: " + st[0]
                        + " - الصف: " + grade + " / " + classroom,
                    width / 2f, top + 70, p
                );

                int y = top + 90;

                canvas.drawText(
                    "المادة                 المحصلة       الاختبار       المجموع",
                    width / 2f, y, p
                );

                double total = 0;

                for (String subject : SUBJECTS_7) {
                    y += 19;

                    double m = db.getMahsala(st[1], subject, semester);
                    double e = db.getExam(st[1], subject, semester);

                    total += m + e;

                    canvas.drawText(
                        subject + "       "
                            + formatNumber(m) + "       "
                            + formatNumber(e) + "       "
                            + formatNumber(m + e),
                        width / 2f,
                        y,
                        p
                    );
                }

                y += 10;

                p.setFakeBoldText(true);

                canvas.drawText(
                    "المجموع: " + formatNumber(total)
                        + " / " + (SUBJECTS_7.length * 50),
                    width / 2f, y, p
                );

                p.setFakeBoldText(false);

                canvas.drawText(
                    "توقيع المعلم: ....................",
                    width / 2f, y + 18, p
                );
            }

            document.finishPage(page);
        }

        savePdf(
            document,
            "exam_cards_" + grade + "_" + classroom + ".pdf"
        );
    }

    // ---------------- حفظ وفتح ملفات PDF ----------------

    void savePdf(PdfDocument document, String fileName) {
        try {
            File folder = new File(getExternalFilesDir(null), "reports");

            if (!folder.exists() && !folder.mkdirs()) {
                throw new IOException("تعذر إنشاء مجلد التقارير");
            }

            File file = new File(folder, fileName);

            try (FileOutputStream output = new FileOutputStream(file)) {
                document.writeTo(output);
            } finally {
                document.close();
            }

            Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(intent);

        } catch (Exception e) {
            try {
                document.close();
            } catch (Exception ignored) {}

            Toast.makeText(
                this,
                "تعذر فتح التقرير. تحقق من وجود تطبيق لقراءة PDF.\n"
                    + e.getMessage(),
                Toast.LENGTH_LONG
            ).show();
        }
    }
}
