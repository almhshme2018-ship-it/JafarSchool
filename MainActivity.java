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
import android.database.Cursor;
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
                Toast.makeText(this, "تعذر الرجوع: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (navHistory.size() == 1) {
            Toast.makeText(this, "أنت في الصفحة الرئيسية", Toast.LENGTH_SHORT).show();
            return;
        }

        super.onBackPressed();
    }

    void goBack() {
        onBackPressed();
    }

    int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
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
        t.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);
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
            Toast.makeText(this, "حدث خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        top.addView(back, new LinearLayout.LayoutParams(dp(82), dp(46)));
        back.setOnClickListener(v -> goBack());

        TextView heading = tv(title, 17, true);
        heading.setGravity(Gravity.CENTER);
        top.addView(heading, new LinearLayout.LayoutParams(0, dp(48), 1));

        root.addView(top, new LinearLayout.LayoutParams(-1, dp(58)));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(10), dp(8), dp(10), dp(25));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        scroll.addView(content);

        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    void addCard(String title, String body) {
        if (content == null) return;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackground(rounded(WHITE, 12));

        card.addView(tv(title, 16, true));
        card.addView(tv(body, 14, false));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
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
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return e;
    }

    double parse(EditText e) {
        try {
            String value = e.getText().toString().trim();
            if (value.isEmpty()) return 0;
            value = value.replace(',', '.').replace('٫', '.');
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
        return r.equals("القائم بأعمال المدير") || r.equals("مدير المدرسة") || r.equals("مدير النظام");
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

    double annualStudentTotal(String studentId) {
        double total = 0;
        for (String subject : SUBJECTS_7) {
            total += db.getMahsala(studentId, subject, 1) + db.getExam(studentId, subject, 1);
            total += db.getMahsala(studentId, subject, 2) + db.getExam(studentId, subject, 2);
        }
        return total;
    }

    void savePdf(PdfDocument document, String fileName) {
        try {
            File folder = new File(getExternalFilesDir(null), "documents");
            if (!folder.exists() && !folder.mkdirs()) {
                throw new IOException("تعذر إنشاء مجلد المستندات");
            }
            File file = new File(folder, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

        } catch (Exception e) {
            try {
                document.close();
            } catch (Exception ignored) {}

            Toast.makeText(this, "تعذر حفظ أو فتح ملف الـ PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && data!=null && data.getData()!=null){
            if(requestCode==2001){exportBackup(data.getData());return;}
            if(requestCode==2002){restoreBackup(data.getData());return;}
        }

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    File logoFile = new File(getFilesDir(), "school_logo.png");
                    OutputStream outputStream = new FileOutputStream(logoFile);
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    inputStream.close();
                    outputStream.close();
                    Toast.makeText(this, "تم حفظ شعار المدرسة بنجاح", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "تعذر حفظ الشعار: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

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
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView logo = tv("🏫", 48, true);
        logo.setGravity(Gravity.CENTER);
        box.addView(logo);

        TextView title = tv("مدرسة جعفر بن أبي طالب الأساسية", 21, true);
        title.setGravity(Gravity.CENTER);
        box.addView(title);

        TextView address = tv("الجمهورية اليمنية - إب - مذيخرة - الأشعوب\nنظام إدارة المدرسة", 13, false);
        address.setGravity(Gravity.CENTER);
        box.addView(address);

        EditText username = edit("اسم المستخدم");
        EditText password = edit("كلمة المرور");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        box.addView(username, new LinearLayout.LayoutParams(-1, dp(55)));
        box.addView(password, new LinearLayout.LayoutParams(-1, dp(55)));

        Button login = btn("دخول");
        box.addView(login, new LinearLayout.LayoutParams(-1, dp(52)));

        TextView hint = tv("الحساب الافتراضي: admin / 1234", 12, false);
        hint.setGravity(Gravity.CENTER);
        box.addView(hint);

        login.setOnClickListener(v -> {
            String u = username.getText().toString().trim();
            String p = password.getText().toString();

            if (u.isEmpty()) {
                username.setError("اكتب اسم المستخدم هنا");
                username.requestFocus();
                Toast.makeText(this, "حقل اسم المستخدم فارغ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (p.trim().isEmpty()) {
                password.setError("اكتب كلمة المرور هنا");
                password.requestFocus();
                Toast.makeText(this, "حقل كلمة المرور فارغ", Toast.LENGTH_SHORT).show();
                return;
            }

            login.setEnabled(false);

            try {
                String[] a = db.authenticate(u, p);
                if (a == null || a.length < 5) {
                    Toast.makeText(this, "اسم المستخدم أو كلمة المرور غير صحيحة", Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "تعذر تسجيل الدخول: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                login.setEnabled(true);
            }
        });
    }

    void showHome() {
        base();

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(8), dp(6), dp(8), dp(6));
        header.setBackgroundColor(WHITE);

        header.addView(tv(role + "\n" + currentUser, 13, true), new LinearLayout.LayoutParams(0, -2, 1));

        Button changePass = smallBtn("🔑 كلمة المرور", GREEN);
        header.addView(changePass, new LinearLayout.LayoutParams(dp(110), dp(44)));
        changePass.setOnClickListener(v -> changePasswordDialog());

        Button logout = btn("خروج");
        header.addView(logout, new LinearLayout.LayoutParams(dp(82), dp(44)));
        logout.setOnClickListener(v -> showLogin());

        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(8), dp(8), dp(8), dp(20));

        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

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

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(70), 1);
            p.setMargins(dp(2), dp(3), dp(2), dp(6));
            stats.addView(item, p);
        }

        content.addView(stats);

        dashboardIndicators();


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
            content.addView(row, new LinearLayout.LayoutParams(-1, dp(72)));

            for (int j = i; j < Math.min(i + 2, items.size()); j++) {
                String[] item = items.get(j);
                Button b = btn(item[0]);

                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(62), 1);
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

    void dashboardIndicators() {
        int studentsCount=safeCount("students");
        int present=db.todayPresent();
        int absent=db.todayAbsent();
        int recorded=present+absent;
        double rate=recorded>0 ? (present*100.0/recorded) : 0;
        addCard("📊 ملخص اليوم","عدد الطلاب: "+studentsCount+"\\nالحضور المسجل: "+present+"\\nالغياب المسجل: "+absent+"\\nنسبة الحضور من السجلات المتاحة: "+formatNumber(rate)+"%");
        if(admin()){
            Button reportsShortcut=btn("📈 فتح التقارير");
            content.addView(reportsShortcut,new LinearLayout.LayoutParams(-1,dp(48)));
            reportsShortcut.setOnClickListener(v->open("reports"));
            Button attendanceShortcut=btn("✅ تسجيل الحضور");
            content.addView(attendanceShortcut,new LinearLayout.LayoutParams(-1,dp(48)));
            attendanceShortcut.setOnClickListener(v->open("attendance"));
        }
    }

    void changePasswordDialog() {
        EditText oldPass = edit("كلمة المرور الحالية");
        EditText newPass = edit("كلمة المرور الجديدة (6 أحرف على الأقل)");
        EditText confirm = edit("تأكيد كلمة المرور الجديدة");
        oldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(8), dp(4), dp(8), dp(4));
        box.addView(oldPass);
        box.addView(newPass);
        box.addView(confirm);
        new AlertDialog.Builder(this).setTitle("تغيير كلمة المرور")
            .setView(box)
            .setPositiveButton("حفظ", (dialog, which) -> {
                String oldValue = oldPass.getText().toString();
                String newValue = newPass.getText().toString();
                if (newValue.length() < 6) {
                    Toast.makeText(this, "كلمة المرور الجديدة يجب ألا تقل عن 6 أحرف", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!newValue.equals(confirm.getText().toString())) {
                    Toast.makeText(this, "تأكيد كلمة المرور غير متطابق", Toast.LENGTH_LONG).show();
                    return;
                }
                boolean changed = db.changePassword(currentUsername, oldValue, newValue);
                Toast.makeText(this, changed ? "تم تغيير كلمة المرور" : "تعذر التغيير: تحقق من كلمة المرور الحالية", Toast.LENGTH_LONG).show();
            }).setNegativeButton("إلغاء", null).show();
    }

    void open(String key) {
        switch (key) {
            case "students": navigateTo(this::students); break;
            case "grades": navigateTo(this::grades); break;
            case "attendance": navigateTo(this::attendance); break;
            case "timetable": navigateTo(this::timetable); break;
            case "teachers": navigateTo(this::teachers); break;
            case "classes": navigateTo(this::classes); break;
            case "reports": navigateTo(this::reports); break;
            case "announcements": navigateTo(this::announcements); break;
            case "users": if (admin()) navigateTo(this::users); break;
            case "files": if (admin()) navigateTo(this::files); break;
        }
    }

    void students() {
        page("👨‍🎓 الطلاب");

        EditText search = edit("ابحث باسم الطالب أو رقمه");
        content.addView(search, new LinearLayout.LayoutParams(-1, dp(52)));

        if (admin()) {
            Button add = btn("＋ إضافة طالب");
            content.addView(add, new LinearLayout.LayoutParams(-1, dp(52)));
            add.setOnClickListener(v -> studentDialog(null));
        }

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list);

        Runnable refresh = () -> {
            list.removeAllViews();
            for (String[] s : db.students(search.getText().toString().trim())) {
                if (s.length < 4) continue;

                Button b = btn(s[0] + "\nالرقم: " + s[1] + " | الصف: " + s[2] + " / " + s[3]);
                b.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
                list.addView(b, new LinearLayout.LayoutParams(-1, dp(68)));

                String id = s[1];
                b.setOnClickListener(v -> navigateTo(() -> studentDetails(id)));
            }
        };

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { refresh.run(); }
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

        for (EditText e : new EditText[]{name, number, grade, classroom, parent, phone}) {
            box.addView(e, new LinearLayout.LayoutParams(-1, dp(50)));
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
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String n = name.getText().toString().trim();
                String sid = number.getText().toString().trim();

                if (n.isEmpty() || sid.isEmpty()) {
                    Toast.makeText(this, "اسم الطالب ورقمه مطلوبان", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean saved = db.saveStudent(
                    n, sid,
                    grade.getText().toString().trim(),
                    classroom.getText().toString().trim(),
                    parent.getText().toString().trim(),
                    phone.getText().toString().trim()
                );

                if (saved) {
                    dialog.dismiss();
                    students();
                    Toast.makeText(this, "تم حفظ بيانات الطالب", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "تعذر حفظ بيانات الطالب", Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    ArrayList<String[]> studentsForClass(String grade, String classroom) {
        ArrayList<String[]> result = db.studentsInClass(grade, classroom);
        if (!result.isEmpty()) return result;

        for (String[] s : db.students("")) {
            if (s.length >= 4 && grade.equals(s[2]) && classroom.equals(s[3])) {
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
            "الاسم: " + s[0] + "\nالرقم: " + s[1] + "\nالصف: " + s[2] + " / " + s[3]
                + "\nولي الأمر: " + s[4] + "\nالهاتف: " + s[5]
        );

        double grandTotal = 0;
        boolean allPass = true;

        for (String subject : SUBJECTS_7) {
            double first = db.getMahsala(id, subject, 1) + db.getExam(id, subject, 1);
            double second = db.getMahsala(id, subject, 2) + db.getExam(id, subject, 2);
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
        content.addView(certificate, new LinearLayout.LayoutParams(-1, dp(52)));
        certificate.setOnClickListener(v -> generateCertificatePdf(id));

        if (admin()) {
            Button edit = btn("✏️ تعديل بيانات الطالب");
            content.addView(edit, new LinearLayout.LayoutParams(-1, dp(52)));
            edit.setOnClickListener(v -> studentDialog(id));
        }

        addCard("ملخص الحضور", db.studentAttendanceSummary(id));
        Button history = btn("📅 سجل الحضور الأخير");
        content.addView(history,new LinearLayout.LayoutParams(-1,dp(48)));
        history.setOnClickListener(v->{
            ArrayList<String[]> records=db.studentAttendanceHistory(id,30);
            if(records.isEmpty()){new AlertDialog.Builder(this).setTitle("سجل الحضور").setMessage("لا توجد سجلات حضور").setPositiveButton("إغلاق",null).show();return;}
            StringBuilder text=new StringBuilder();
            for(String[] record:records)text.append(record[0]).append("  —  ").append(record[1]).append("\\n");
            new AlertDialog.Builder(this).setTitle("آخر 30 سجل حضور").setMessage(text.toString()).setPositiveButton("إغلاق",null).show();
        });
    }

    ArrayList<String[]> allowedClasses() {
        ArrayList<String[]> all = db.classes();
        if (admin()) return all;

        ArrayList<String[]> result = new ArrayList<>();
        if (isTeacher()) {
            for (String[] c : all) {
                if (c.length >= 2 && c[0].equals(assignedGrade) && c[1].equals(assignedClass)) {
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

    void grades() {
        page("📊 الدرجات");
        addCard(
            "إدخال الدرجات",
            "اختر الصف والشعبة، ثم اضغط على خانة المادة لإدخال المحصلة والاختبار."
                + "\nالمحصلة: 20 درجة\nالاختبار: 30 درجة"
        );

        ArrayList<String[]> classes = allowedClasses();
        if (classes.isEmpty()) {
            addCard("لا توجد صفوف متاحة", "أضف الطلاب وحدد صفوفهم وشعبهم أولاً.");
            return;
        }

        for (String[] c : classes) {
            if (c.length < 2) continue;
            String g = c[0];
            String cl = c[1];

            Button b = btn("📚 الصف " + g + " - الشعبة " + cl);
            content.addView(b, new LinearLayout.LayoutParams(-1, dp(54)));
            b.setOnClickListener(v -> navigateTo(() -> classGrades(g, cl)));
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
        content.addView(semester, new LinearLayout.LayoutParams(-1, dp(50)));

        TextView instructions = tv("اضغط على زر المادة في صف الطالب لإدخال الدرجات.", 13, false);
        content.addView(instructions);

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        LinearLayout table = new LinearLayout(this);
        table.setOrientation(LinearLayout.VERTICAL);
        table.setPadding(0, dp(5), 0, dp(10));
        horizontal.addView(table);
        content.addView(horizontal, new LinearLayout.LayoutParams(-1, -2));

        Runnable render = () -> {
            table.removeAllViews();
            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setBackgroundColor(WHITE);

            TextView studentHeading = tv("الطالب", 12, true);
            studentHeading.setGravity(Gravity.CENTER);
            header.addView(studentHeading, new LinearLayout.LayoutParams(dp(155), dp(55)));

            for (String subject : SUBJECTS_7) {
                TextView h = tv(subject, 11, true);
                h.setGravity(Gravity.CENTER);
                header.addView(h, new LinearLayout.LayoutParams(dp(125), dp(55)));
            }
            table.addView(header);

            int sem = semester.getSelectedItemPosition() + 1;
            ArrayList<String[]> students = studentsForClass(grade, classroom);

            if (students.isEmpty()) {
                table.addView(tv("لا يوجد طلاب في هذا الصف والشعبة.", 14, true));
                return;
            }

            for (String[] st : students) {
                if (st.length < 2) continue;
                String sid = st[1];
                String studentName = st[0];

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setBackground(rounded(WHITE, 8));

                TextView name = tv(studentName + "\n" + sid, 11, true);
                row.addView(name, new LinearLayout.LayoutParams(dp(155), dp(66)));

                for (String subject : SUBJECTS_7) {
                    double m = db.getMahsala(sid, subject, sem);
                    double e = db.getExam(sid, subject, sem);
                    double total = m + e;

                    String label = (m == 0 && e == 0) ? "إدخال" : formatNumber(total) + " /50";
                    Button cell = smallBtn(label, (m == 0 && e == 0) ? ORANGE : GREEN);

                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(125), dp(58));
                    p.setMargins(dp(2), dp(3), dp(2), dp(3));
                    row.addView(cell, p);

                    cell.setOnClickListener(v -> {
                        if (!admin() && !isTeacher()) {
                            Toast.makeText(this, "ليست لديك صلاحية إدخال الدرجات", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        scoreDialog(sid, studentName, subject, sem, cell);
                    });
                }
                table.addView(row);
            }
        };

        semester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                render.run();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        render.run();

        Button bulk = btn("✏️ إدخال درجات جماعي لمادة");
        content.addView(bulk, new LinearLayout.LayoutParams(-1, dp(52)));
        bulk.setOnClickListener(v -> bulkScoreDialog(grade, classroom, semester.getSelectedItemPosition() + 1));

        Button audit = btn("🔎 تدقيق الدرجات للفصل");
        content.addView(audit, new LinearLayout.LayoutParams(-1, dp(52)));
        audit.setOnClickListener(v -> scoreAuditDialog(grade, classroom, semester.getSelectedItemPosition() + 1));

        Button sheet = btn("📋 كشف النتائج السنوي PDF");
        content.addView(sheet, new LinearLayout.LayoutParams(-1, dp(52)));
        sheet.setOnClickListener(v -> generateClassSheetPdf(grade, classroom));

        Button cards = btn("🖨️ بطاقات نتائج الفصل PDF");
        content.addView(cards, new LinearLayout.LayoutParams(-1, dp(52)));
        cards.setOnClickListener(v -> generateExamCardsPdf(grade, classroom, semester.getSelectedItemPosition() + 1));
    }

    void scoreAuditDialog(String grade, String classroom, int sem) {
        ArrayList<String[]> students = studentsForClass(grade, classroom);
        StringBuilder report = new StringBuilder();
        int incomplete = 0;
        for (String[] student : students) {
            ArrayList<String> missing = new ArrayList<>();
            for (String subject : SUBJECTS_7) {
                if (!db.hasScoreRecord(student[1], subject, sem)) missing.add(subject);
            }
            if (!missing.isEmpty()) {
                incomplete++;
                report.append("• ").append(student[0]).append("\\n");
                report.append("  المواد غير المدخلة: ").append(String.join("، ", missing)).append("\\n\\n");
            }
        }
        if (incomplete == 0) {
            report.append("اكتمل وجود سجلات الدرجات لجميع الطلاب والمواد في هذا الفصل.");
        } else {
            report.insert(0, "طلاب يحتاجون مراجعة: " + incomplete + " من " + students.size() + "\\n\\n");
        }
        TextView view = tv(report.toString(), 14, false);
        view.setGravity(Gravity.RIGHT | Gravity.TOP);
        new AlertDialog.Builder(this).setTitle("تدقيق درجات الفصل " + sem)
            .setView(new ScrollView(this) {{ addView(view); }})
            .setPositiveButton("إغلاق", null).show();
    }

    void bulkScoreDialog(String grade, String classroom, int sem) {
        ArrayList<String[]> students = studentsForClass(grade, classroom);
        if (students.isEmpty()) {
            Toast.makeText(this, "لا يوجد طلاب لإدخال درجاتهم", Toast.LENGTH_SHORT).show();
            return;
        }
        Spinner subjectSpinner = new Spinner(this);
        subjectSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SUBJECTS_7));
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(8), dp(6), dp(8), dp(6));
        box.addView(subjectSpinner);
        LinearLayout rowsBox = new LinearLayout(this);
        rowsBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(rowsBox);
        ArrayList<EditText[]> fields = new ArrayList<>();
        Runnable renderRows = () -> {
            rowsBox.removeAllViews();
            fields.clear();
            String subject = subjectSpinner.getSelectedItem().toString();
            for (String[] student : students) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                TextView label = tv(student[0], 12, true);
                row.addView(label, new LinearLayout.LayoutParams(0, dp(48), 1));
                EditText m = scoreEdit("20");
                EditText e = scoreEdit("30");
                m.setText(formatNumber(db.getMahsala(student[1], subject, sem)));
                e.setText(formatNumber(db.getExam(student[1], subject, sem)));
                row.addView(m, new LinearLayout.LayoutParams(dp(70), dp(48)));
                row.addView(e, new LinearLayout.LayoutParams(dp(70), dp(48)));
                rowsBox.addView(row);
                fields.add(new EditText[]{m,e});
            }
        };
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View view, int position, long id) { renderRows.run(); }
            public void onNothingSelected(AdapterView parent) {}
        });
        renderRows.run();
        ScrollView scroll = new ScrollView(this);
        scroll.addView(box);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("إدخال درجات جماعي — الفصل " + sem)
            .setView(scroll).setPositiveButton("حفظ الكل", null).setNegativeButton("إلغاء", null)
            .create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            ArrayList<String[]> data = new ArrayList<>();
            for (int i = 0; i < students.size(); i++) {
                double mv = parse(fields.get(i)[0]), ev = parse(fields.get(i)[1]);
                if (mv < 0 || mv > 20 || ev < 0 || ev > 30) {
                    Toast.makeText(this, "راجع درجات الطالب: " + students.get(i)[0] + " (المحصلة 0-20 والاختبار 0-30)", Toast.LENGTH_LONG).show();
                    return;
                }
                data.add(new String[]{students.get(i)[1], String.valueOf(mv), String.valueOf(ev)});
            }
            boolean saved = db.saveBulkScores(subjectSpinner.getSelectedItem().toString(), sem, data);
            Toast.makeText(this, saved ? "تم حفظ درجات الصف للمادة" : "تعذر حفظ الدرجات", Toast.LENGTH_LONG).show();
            if (saved) { dialog.dismiss(); classGrades(grade, classroom); }
        }));
        dialog.show();
    }

    void scoreDialog(String studentId, String studentName, String subject, int sem, Button cell) {
        EditText mahsala = scoreEdit("المحصلة من 20");
        EditText exam = scoreEdit("الاختبار من 30");

        mahsala.setText(formatNumber(db.getMahsala(studentId, subject, sem)));
        exam.setText(formatNumber(db.getExam(studentId, subject, sem)));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(5), dp(10), dp(5));
        box.addView(mahsala, new LinearLayout.LayoutParams(-1, dp(55)));
        box.addView(exam, new LinearLayout.LayoutParams(-1, dp(55)));

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(studentName + "\n" + subject)
            .setView(box)
            .setPositiveButton("حفظ", null)
            .setNegativeButton("إلغاء", null)
            .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                double m = parse(mahsala);
                double e = parse(exam);

                if (m < 0 || m > 20 || e < 0 || e > 30) {
                    Toast.makeText(this, "المحصلة من 0 إلى 20 والاختبار من 0 إلى 30", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    db.setMahsala(studentId, subject, sem, m);
                    db.setExam(studentId, subject, sem, e);

                    cell.setText(formatNumber(m + e) + " /50");
                    cell.setBackground(rounded(GREEN, 10));

                    Toast.makeText(this, "تم حفظ درجات " + studentName, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } catch (Exception ex) {
                    Toast.makeText(this, "تعذر حفظ الدرجات: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    void attendance() {
        page("✅ الحضور والغياب");
        addCard("تسجيل الحضور", "اختر الصف والشعبة، ثم حدد حالة كل طالب.");

        ArrayList<String[]> classes = allowedClasses();
        if (classes.isEmpty()) {
            addCard("لا توجد صفوف", "أضف الطلاب أولاً.");
            return;
        }

        for (String[] c : classes) {
            if (c.length < 2) continue;
            Button b = btn("الصف " + c[0] + " - الشعبة " + c[1]);
            content.addView(b, new LinearLayout.LayoutParams(-1, dp(52)));
            b.setOnClickListener(v -> navigateTo(() -> attendanceClass(c[0], c[1])));
        }
    }

    void attendanceClass(String grade, String classroom) {
        attendanceClassForDate(grade, classroom, new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(new java.util.Date()));
    }

    void attendanceClassForDate(String grade, String classroom, String date) {

        page("الحضور: " + grade + " / " + classroom);
        addCard("التاريخ المحدد", date);
        Button chooseDate = btn("📅 اختيار تاريخ آخر");
        content.addView(chooseDate, new LinearLayout.LayoutParams(-1, dp(48)));
        chooseDate.setOnClickListener(v -> { java.util.Calendar cal = java.util.Calendar.getInstance(); try { java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US); cal.setTime(sdf.parse(date)); } catch (Exception ignored) {} new DatePickerDialog(this, (picker, year, month, day) -> attendanceClassForDate(grade, classroom, String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month+1, day)), cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show(); });
        ArrayList<String[]> students = studentsForClass(grade, classroom);

        if (students.isEmpty()) {
            addCard("لا يوجد طلاب", "أضف الطلاب إلى هذا الصف أولاً.");
            return;
        }

        addCard("عدد الطلاب", String.valueOf(students.size()));

        for (String[] s : students) {
            if (s.length < 2) continue;
            String studentId = s[1];
            String studentName = s[0];

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(8), dp(6), dp(8), dp(6));
            row.setBackground(rounded(WHITE, 10));
            row.addView(tv(studentName + (db.attendanceStatus(studentId, date).isEmpty() ? "\\nلم يسجل" : "\\nالحالة: " + db.attendanceStatus(studentId, date)), 14, true));

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);

            String[] statuses = {"حاضر", "غائب", "متأخر", "غائب بعذر"};
            int[] colors = {GREEN, RED, ORANGE, BLUE};

            for (int i = 0; i < statuses.length; i++) {
                String status = statuses[i];
                Button b = smallBtn(status, colors[i]);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(44), 1);
                p.setMargins(dp(2), dp(3), dp(2), dp(3));
                actions.addView(b, p);

                b.setOnClickListener(v -> {
                    if (!admin() && !isTeacher()) {
                        Toast.makeText(this, "ليست لديك صلاحية تسجيل الحضور", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean saved = db.setAttendanceForDate(studentId, date, status);
                    Toast.makeText(this, saved ? "تم تسجيل " + status : "تعذر حفظ الحضور", Toast.LENGTH_SHORT).show();
                });
            }

            row.addView(actions);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
            p.setMargins(0, dp(4), 0, dp(4));
            content.addView(row, p);
        }
    }

    void teachers() {
        page("👨‍🏫 المعلمون");
        if (admin()) {
            Button add = btn("＋ إضافة معلم");
            content.addView(add, new LinearLayout.LayoutParams(-1, dp(52)));
            add.setOnClickListener(v -> teacherDialog());
        }

        android.database.Cursor c = null;
        try {
            c = getReadableTeachersCursor();
            if (c == null || !c.moveToFirst()) {
                addCard("المعلمون", "لا توجد بيانات معلمين.");
                return;
            }

            do {
                addCard(
                    c.getString(1),
                    "الهاتف: " + (c.isNull(2) ? "" : c.getString(2))
                        + "\nالمادة: " + (c.isNull(3) ? "" : c.getString(3))
                        + "\nالصف: " + (c.isNull(4) ? "" : c.getString(4)) + " / " + (c.isNull(5) ? "" : c.getString(5))
                );
            } while (c.moveToNext());
        } catch (Exception e) {
            addCard("المعلمون", "تعذر تحميل بيانات المعلمين.");
        } finally {
            if (c != null) c.close();
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
        for (EditText e : new EditText[]{id, name, phone, subject, grade, classroom}) {
            box.addView(e, new LinearLayout.LayoutParams(-1, dp(48)));
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(box);

        new AlertDialog.Builder(this)
            .setTitle("إضافة معلم")
            .setView(scroll)
            .setPositiveButton("حفظ", (d, w) -> {
                if (id.getText().toString().trim().isEmpty() || name.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "رقم المعلم واسمه مطلوبان", Toast.LENGTH_SHORT).show();
                    return;
                }

                ContentValues cv = new ContentValues();
                cv.put("id", id.getText().toString().trim());
                cv.put("name", name.getText().toString().trim());
                cv.put("phone", phone.getText().toString().trim());
                cv.put("subject", subject.getText().toString().trim());
                cv.put("grade", grade.getText().toString().trim());
                cv.put("class", classroom.getText().toString().trim());

                long result = db.getWritableDatabase().insertWithOnConflict(
                    "teachers", null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                );

                Toast.makeText(this, result == -1 ? "تعذر الحفظ" : "تم حفظ المعلم", Toast.LENGTH_SHORT).show();
                teachers();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    void timetable() {
        page("📅 الجدول المدرسي");
        addCard("أيام الدراسة", "السبت، الأحد، الاثنين، الثلاثاء، الأربعاء\nالخميس والجمعة: إجازة");

        android.database.Cursor c = null;
        try {
            c = db.getReadableDatabase().rawQuery(
                "SELECT grade,class,day,period,subject,teacher FROM timetable ORDER BY grade,class,day,period",
                null
            );

            if (!c.moveToFirst()) {
                addCard("الجدول", "لم تتم إضافة حصص إلى الجدول بعد.");
                return;
            }

            do {
                addCard(
                    "الصف " + c.getString(0) + " / " + c.getString(1),
                    "اليوم: " + c.getString(2) + "\nالحصة: " + c.getInt(3)
                        + "\nالمادة: " + c.getString(4) + "\nالمعلم: " + c.getString(5)
                );
            } while (c.moveToNext());
        } catch (Exception e) {
            addCard("الجدول", "تعذر تحميل الجدول.");
        } finally {
            if (c != null) c.close();
        }
    }

    void announcements() {
        page("🔔 الإعلانات");
        if (admin()) {
            Button add = btn("＋ إضافة إعلان");
            content.addView(add, new LinearLayout.LayoutParams(-1, dp(52)));
            add.setOnClickListener(v -> announcementDialog());
        }

        android.database.Cursor c = null;
        try {
            c = db.getReadableDatabase().rawQuery("SELECT title,body,date FROM announcements ORDER BY id DESC", null);
            if (!c.moveToFirst()) {
                addCard("الإعلانات", "لا توجد إعلانات.");
                return;
            }

            do {
                addCard(
                    c.isNull(0) ? "" : c.getString(0),
                    (c.isNull(1) ? "" : c.getString(1)) + "\nالتاريخ: " + (c.isNull(2) ? "" : c.getString(2))
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
                boolean saved = db.saveAnnouncement(title.getText().toString().trim(), body.getText().toString().trim());
                Toast.makeText(this, saved ? "تم حفظ الإعلان" : "تعذر حفظ الإعلان", Toast.LENGTH_SHORT).show();
                announcements();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    void users() {
        page("🔐 المستخدمون");
        if (!admin()) {
            addCard("تنبيه", "ليست لديك صلاحية إدارة المستخدمين.");
            return;
        }

        Button add = btn("＋ إضافة مستخدم");
        content.addView(add, new LinearLayout.LayoutParams(-1, dp(52)));
        add.setOnClickListener(v -> userDialog());

        android.database.Cursor c = null;
        try {
            c = db.getReadableDatabase().rawQuery("SELECT name,username,role,grade,class FROM users ORDER BY name", null);
            while (c.moveToNext()) {
                addCard(
                    c.getString(0) + " (" + c.getString(1) + ")",
                    "الصلاحية: " + c.getString(2) + "\nالصف: " + c.getString(3) + "\nالشعبة: " + c.getString(4)
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
                new String[]{"معلم", "مدير المدرسة", "القائم بأعمال المدير", "مدير النظام"}
            )
        );

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        for (EditText e : new EditText[]{name, username, password, grade, classroom}) {
            box.addView(e, new LinearLayout.LayoutParams(-1, dp(48)));
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

                if (u.isEmpty() || p.length() < 6) {
                    Toast.makeText(this, "اسم الدخول مطلوب وكلمة المرور 6 أحرف على الأقل", Toast.LENGTH_SHORT).show();
                    return;
                }

                ContentValues cv = new ContentValues();
                cv.put("name", name.getText().toString().trim());
                cv.put("username", u);
                cv.put("pass", db.passwordForStorage(p));
                cv.put("role", roles.getSelectedItem().toString());
                cv.put("grade", grade.getText().toString().trim());
                cv.put("class", classroom.getText().toString().trim());

                long result = db.getWritableDatabase().insertWithOnConflict(
                    "users", null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                );

                Toast.makeText(this, result == -1 ? "تعذر حفظ المستخدم" : "تم حفظ المستخدم", Toast.LENGTH_SHORT).show();
                users();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    void attendanceReportDialog() {
        ArrayList<String[]> classes = allowedClasses();
        if (classes.isEmpty()) { Toast.makeText(this, "لا توجد صفوف", Toast.LENGTH_SHORT).show(); return; }
        String[] options = new String[classes.size()];
        for (int i=0;i<classes.size();i++) options[i]="الصف "+classes.get(i)[0]+" / "+classes.get(i)[1];
        new AlertDialog.Builder(this).setTitle("اختر الصف والشعبة").setItems(options,(dlg,idx)->{
            String grade=classes.get(idx)[0], classroom=classes.get(idx)[1];
            java.util.Calendar start=java.util.Calendar.getInstance(), end=java.util.Calendar.getInstance();
            java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US);
            java.util.Date[] dates={start.getTime(),end.getTime()};
            DatePickerDialog first=new DatePickerDialog(this,(p,y,mo,day)->{
                dates[0]=calendarDate(y,mo,day);
                new DatePickerDialog(this,(p2,y2,mo2,day2)->{
                    dates[1]=calendarDate(y2,mo2,day2);
                    String a=sdf.format(dates[0]), b=sdf.format(dates[1]);
                    if(a.compareTo(b)>0){String tmp=a;a=b;b=tmp;}
                    attendanceReportView(grade,classroom,a,b);
                },end.get(Calendar.YEAR),end.get(Calendar.MONTH),end.get(Calendar.DAY_OF_MONTH)).show();
            },start.get(Calendar.YEAR),start.get(Calendar.MONTH),start.get(Calendar.DAY_OF_MONTH));
            first.setTitle("حدد تاريخ البداية"); first.show();
        }).setNegativeButton("إلغاء",null).show();
    }

    java.util.Date calendarDate(int year,int month,int day) {
        java.util.Calendar c=java.util.Calendar.getInstance();
        c.set(year,month,day,12,0,0); c.set(java.util.Calendar.MILLISECOND,0);
        return c.getTime();
    }

    void attendanceReportView(String grade,String classroom,String start,String end) {
        page("تقرير الغياب");
        addCard("الصف "+grade+" / "+classroom,"الفترة: "+start+" إلى "+end);
        ArrayList<String[]> rows=db.attendanceRangeForClass(grade,classroom,start,end);
        if(rows.isEmpty()){addCard("التقرير","لا توجد سجلات في الفترة المحددة.");return;}
        java.util.LinkedHashMap<String,int[]> totals=new java.util.LinkedHashMap<>();
        for(String[] row:rows){
            int[] values=totals.get(row[1]);
            if(values==null){values=new int[4];totals.put(row[1],values);}
            int count=Integer.parseInt(row[3]);
            if("حاضر".equals(row[2]))values[0]+=count;
            else if("غائب".equals(row[2]))values[1]+=count;
            else if("متأخر".equals(row[2]))values[2]+=count;
            else if("غائب بعذر".equals(row[2]))values[3]+=count;
        }
        for(String id:totals.keySet()) {
            int[] t=totals.get(id);
            String student="";
            for(String[] row:rows) if(row[1].equals(id)){student=row[0];break;}
            addCard(student,"حاضر: "+t[0]+"\\nغائب: "+t[1]+"\\nمتأخر: "+t[2]+"\\nغائب بعذر: "+t[3]);
        }
        Button pdf=btn("📄 حفظ تقرير الغياب PDF");
        content.addView(pdf,new LinearLayout.LayoutParams(-1,dp(52)));
        pdf.setOnClickListener(v->generateAttendancePdf(grade,classroom,start,end,totals,rows));
    }

    void generateAttendancePdf(String grade,String classroom,String start,String end,java.util.LinkedHashMap<String,int[]> totals,ArrayList<String[]> rows) {
        PdfDocument document=new PdfDocument();
        int width=595,height=842, pageNo=1;
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK); paint.setTextAlign(Paint.Align.CENTER);
        PdfDocument.Page pdfPage=document.startPage(new PdfDocument.PageInfo.Builder(width,height,pageNo).create());
        Canvas canvas=pdfPage.getCanvas(); canvas.drawColor(Color.WHITE);
        paint.setTextSize(18); paint.setFakeBoldText(true);
        canvas.drawText("مدرسة جعفر بن أبي طالب الأساسية",width/2f,40,paint);
        paint.setTextSize(15); canvas.drawText("كشف الحضور والغياب",width/2f,68,paint);
        paint.setTextSize(12); paint.setFakeBoldText(false);
        canvas.drawText("الصف: "+grade+"    الشعبة: "+classroom,width/2f,95,paint);
        canvas.drawText("الفترة: "+start+" إلى "+end,width/2f,118,paint);
        int y=155;
        paint.setTextSize(10); paint.setFakeBoldText(true);
        String[] headings={"اسم الطالب","حاضر","غائب","متأخر","بعذر"};
        int[] xs={475,370,280,190,100};
        for(int i=0;i<headings.length;i++) canvas.drawText(headings[i],xs[i],y,paint);
        paint.setFakeBoldText(false); y+=22;
        for(String id:totals.keySet()){
            if(y>height-80){document.finishPage(pdfPage);pageNo++;pdfPage=document.startPage(new PdfDocument.PageInfo.Builder(width,height,pageNo).create());canvas=pdfPage.getCanvas();canvas.drawColor(Color.WHITE);y=50;}
            String name=""; for(String[] row:rows)if(row[1].equals(id)){name=row[0];break;}
            int[] t=totals.get(id);
            canvas.drawText(name.length()>28?name.substring(0,28):name,475,y,paint);
            for(int k=0;k<4;k++)canvas.drawText(String.valueOf(t[k]),xs[k+1],y,paint);
            canvas.drawLine(40,y+5,width-40,y+5,paint);y+=24;
        }
        paint.setTextSize(11); paint.setFakeBoldText(true);
        canvas.drawText("توقيع الإدارة: __________________     ملاحظات: __________________",width/2f,height-35,paint);
        document.finishPage(pdfPage);
        savePdf(document,"attendance_"+grade+"_"+classroom+"_"+start+"_"+end+".pdf");
    }


    void reports() {
        page("📈 التقارير");
        if(admin()){ Button subjectsManage=btn("📚 إدارة المواد (إضافة/تعديل/حذف)"); content.addView(subjectsManage,new LinearLayout.LayoutParams(-1,dp(50))); subjectsManage.setOnClickListener(v->manageSubjects()); Button schoolHeader=btn("⚙️ إعداد ترويسة كشف النتائج");content.addView(schoolHeader,new LinearLayout.LayoutParams(-1,dp(50)));schoolHeader.setOnClickListener(v->editExamHeader()); }
        addCard(
            "إحصاءات المدرسة",
            "عدد الطلاب: " + safeCount("students") + "\nعدد المعلمين: " + safeCount("teachers")
                + "\nحاضر اليوم: " + db.todayPresent() + "\nغائب اليوم: " + db.todayAbsent()
        );

        Button absence = btn("📅 تقرير الغياب خلال فترة");
        content.addView(absence, new LinearLayout.LayoutParams(-1, dp(52)));
        absence.setOnClickListener(v -> attendanceReportDialog());
        
        Button ranking = btn("🏆 ترتيب الطلاب والمتفوقين");
        content.addView(ranking, new LinearLayout.LayoutParams(-1, dp(52)));
        ranking.setOnClickListener(v -> chooseRankingClass());

        Button report = btn("📋 كشف نتائج الصف PDF");
        content.addView(report, new LinearLayout.LayoutParams(-1, dp(52)));

        report.setOnClickListener(v -> {
            ArrayList<String[]> classes = allowedClasses();
            if (classes.isEmpty()) {
                Toast.makeText(this, "لا توجد صفوف", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] options = new String[classes.size()];
            for (int i = 0; i < classes.size(); i++) {
                options[i] = "الصف " + classes.get(i)[0] + " / " + classes.get(i)[1];
            }

            new AlertDialog.Builder(this)
                .setTitle("اختر الصف")
                .setItems(options, (dialog, which) -> {
                    generateClassSheetPdf(classes.get(which)[0], classes.get(which)[1]);
                })
                .setNegativeButton("إلغاء", null)
                .show();
        });
    }

    void chooseRankingClass() {
        ArrayList<String[]> classes = allowedClasses();
        if (classes.isEmpty()) {
            Toast.makeText(this, "لا توجد صفوف", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] options = new String[classes.size()];
        for (int i=0;i<classes.size();i++) options[i]="الصف "+classes.get(i)[0]+" / "+classes.get(i)[1];
        new AlertDialog.Builder(this).setTitle("اختر الصف لترتيب الطلاب")
            .setItems(options,(dialog,which)->showClassRanking(classes.get(which)[0],classes.get(which)[1]))
            .setNegativeButton("إلغاء",null).show();
    }

    boolean annualScoresComplete(String studentId) {
        for (String subject : SUBJECTS_7) {
            if (!db.hasScoreRecord(studentId, subject, 1) || !db.hasScoreRecord(studentId, subject, 2)) return false;
        }
        return true;
    }

    void showClassRanking(String grade, String classroom) {
        ArrayList<String[]> students=studentsForClass(grade,classroom);
        if(students.isEmpty()){Toast.makeText(this,"لا يوجد طلاب",Toast.LENGTH_SHORT).show();return;}
        Collections.sort(students,(a,b)->{
            int scoreCompare=Double.compare(annualStudentTotal(b[1]),annualStudentTotal(a[1]));
            return scoreCompare!=0 ? scoreCompare : a[0].compareToIgnoreCase(b[0]);
        });
        StringBuilder text=new StringBuilder("الصف "+grade+" / "+classroom+"\\n");
        text.append("الترتيب | الطالب | المجموع من 700 | حالة الدرجات\\n\\n");
        int rank=0; double previous=Double.NaN;
        for(int i=0;i<students.size();i++){
            String[] st=students.get(i); double total=annualStudentTotal(st[1]);
            if(i==0 || Double.compare(total,previous)!=0) rank=i+1;
            previous=total;
            text.append(rank).append(" - ").append(st[0]).append(" : ")
                .append(formatNumber(total)).append(" / 700 — ")
                .append(annualScoresComplete(st[1]) ? "مكتملة" : "ناقص سجل في فصل أو مادة")
                .append("\\n");
        }
        text.append("\\nتنبيه: الطالب ذو السجلات الناقصة قد يظهر بمجموع منخفض؛ راجع تدقيق الدرجات قبل اعتماد الترتيب.");
        TextView view=tv(text.toString(),14,false);view.setGravity(Gravity.RIGHT|Gravity.TOP);
        new AlertDialog.Builder(this).setTitle("🏆 ترتيب الطلاب السنوي")
            .setView(new ScrollView(this) {{addView(view);}})
            .setPositiveButton("إغلاق",null).show();
    }

    void files() {
        page("📁 النسخ الاحتياطي والاستعادة");
        addCard("حماية بيانات المدرسة", "أنشئ نسخة خارج التطبيق، أو استعد نسخة سابقة بعد التحقق.");

        Button backup=btn("⬇️ تصدير نسخة احتياطية");
        content.addView(backup,new LinearLayout.LayoutParams(-1,dp(52)));
        backup.setOnClickListener(v->{Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("application/octet-stream");intent.putExtra(Intent.EXTRA_TITLE,"JafarSchool_backup.db");startActivityForResult(intent,2001);});

        Button restore=btn("⬆️ استعادة نسخة احتياطية");
        content.addView(restore,new LinearLayout.LayoutParams(-1,dp(52)));
        restore.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("تحذير الاستعادة").setMessage("ستستبدل الاستعادة بيانات المدرسة الحالية. أنشئ نسخة احتياطية أولاً.").setPositiveButton("اختيار ملف",(dlg,w)->{Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("*/*");startActivityForResult(intent,2002);}).setNegativeButton("إلغاء",null).show());

        Button selectLogoBtn=btn("🖼️ اختيار شعار المدرسة من الهاتف");
        content.addView(selectLogoBtn,new LinearLayout.LayoutParams(-1,dp(52)));
        selectLogoBtn.setOnClickListener(v->{Intent intent=new Intent(Intent.ACTION_GET_CONTENT);intent.setType("image/*");startActivityForResult(Intent.createChooser(intent,"اختر شعار المدرسة"),1001);});
    }

    void copyStream(InputStream in,OutputStream out)throws IOException{byte[] buffer=new byte[8192];int length;while((length=in.read(buffer))>0)out.write(buffer,0,length);out.flush();}

    void exportBackup(Uri uri){
        try(InputStream in=new FileInputStream(getDatabasePath("school.db"));OutputStream out=getContentResolver().openOutputStream(uri)){
            if(out==null)throw new IOException("تعذر فتح الملف");copyStream(in,out);Toast.makeText(this,"تم تصدير النسخة الاحتياطية",Toast.LENGTH_LONG).show();
        }catch(Exception e){Toast.makeText(this,"فشل التصدير: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }

    void restoreBackup(Uri uri){
        File temp=new File(getCacheDir(),"restore_validation.db");
        try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(temp)){if(in==null)throw new IOException("تعذر فتح الملف");copyStream(in,out);}
        catch(Exception e){Toast.makeText(this,"تعذر قراءة الملف",Toast.LENGTH_LONG).show();return;}
        android.database.sqlite.SQLiteDatabase check=null;
        try{
            check=android.database.sqlite.SQLiteDatabase.openDatabase(temp.getAbsolutePath(),null,android.database.sqlite.SQLiteDatabase.OPEN_READONLY);
            Cursor c=check.rawQuery("PRAGMA integrity_check",null);boolean valid=c.moveToFirst()&&"ok".equalsIgnoreCase(c.getString(0));c.close();
            if(!valid)throw new IOException("قاعدة البيانات غير سليمة");check.close();check=null;
            db.close();File target=getDatabasePath("school.db");File replacement=new File(target.getParent(),"school_restore.tmp");
            try(InputStream src=new FileInputStream(temp);OutputStream dst=new FileOutputStream(replacement)){copyStream(src,dst);}
            if(target.exists()&&!target.delete())throw new IOException("تعذر استبدال قاعدة البيانات");
            if(!replacement.renameTo(target))throw new IOException("تعذر تثبيت النسخة");
            db=new DB(this);Toast.makeText(this,"تمت الاستعادة، سيعاد تحميل التطبيق",Toast.LENGTH_LONG).show();recreate();
        }catch(Exception e){if(check!=null)try{check.close();}catch(Exception ignored){}Toast.makeText(this,"لم تتم الاستعادة: "+e.getMessage(),Toast.LENGTH_LONG).show();}
        finally{temp.delete();}
    }

    void generateCertificatePdf(String id) {
        String[] student = db.student(id);
        if (student == null) {
            Toast.makeText(this, "لم يتم العثور على الطالب", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        int width = 595, height = 842;
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(width, height, 1).create();
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
        canvas.drawText("الجمهورية اليمنية - وزارة التربية والتعليم", width / 2f, 35, p);
        canvas.drawText("مدرسة جعفر بن أبي طالب الأساسية", width / 2f, 55, p);

        p.setTextSize(17);
        canvas.drawText("شهادة النتيجة السنوية", width / 2f, 88, p);

        p.setFakeBoldText(false);
        p.setTextSize(10);
        canvas.drawText("الطالب: " + student[0] + "    الرقم: " + student[1], width / 2f, 112, p);
        canvas.drawText("الصف: " + student[2] + " / " + student[3], width / 2f, 130, p);

        int top = 150, rowHeight = 25;
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
            double first = db.getMahsala(id, subject, 1) + db.getExam(id, subject, 1);
            double second = db.getMahsala(id, subject, 2) + db.getExam(id, subject, 2);
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
        canvas.drawText("المجموع: " + formatNumber(grandTotal) + " / " + (int) max, width / 2f, y, p);
        canvas.drawText("النسبة: " + formatNumber(percent) + "%", width / 2f, y + 22, p);

        p.setColor(allPass && percent >= 50 ? GREEN : RED);
        p.setTextSize(18);
        canvas.drawText(allPass && percent >= 50 ? "ناجح" : "راسب", width / 2f, y + 55, p);

        document.finishPage(page);
        savePdf(document, "certificate_" + id + ".pdf");
    }

    void generateClassSheetPdf(String grade, String classroom) {
        ArrayList<String[]> students = studentsForClass(grade, classroom);
        if (students.isEmpty()) {
            Toast.makeText(this, "لا يوجد طلاب في هذا الصف", Toast.LENGTH_SHORT).show();
            return;
        }

        Collections.sort(students, (a, b) -> {
            double totalA = annualStudentTotal(a[1]);
            double totalB = annualStudentTotal(b[1]);
            return Double.compare(totalB, totalA);
        });

        ArrayList<String> activeSubjects = new ArrayList<>();
        activeSubjects.add("القرآن الكريم");
        activeSubjects.add("التربية الإسلامية");
        activeSubjects.add("اللغة العربية");
        activeSubjects.add("الرياضيات");
        activeSubjects.add("العلوم");
        activeSubjects.add("الاجتماعيات");
        activeSubjects.add("اللغة الإنجليزية");

        PdfDocument document = new PdfDocument();
        int width = 842;
        int height = 595;
        int perPage = 15;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.BLACK);
        line.setColor(Color.BLACK);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(0.8f);

        int pageNumber = 0;

        ArrayList<String> headers = new ArrayList<>();
        ArrayList<Integer> widths = new ArrayList<>();

        headers.add("م"); widths.add(30);
        headers.add("اسم الطالب"); widths.add(130);

        for (String sub : activeSubjects) {
            headers.add(sub + " (مح)"); widths.add(45);
            headers.add(sub + " (اخت)"); widths.add(45);
            headers.add(sub + " (مجم)"); widths.add(45);
        }

        headers.add("المجموع العام"); widths.add(65);
        headers.add("النتيجة"); widths.add(55);

        for (int start = 0; start < students.size(); start += perPage) {
            pageNumber++;
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(width, height, pageNumber).create();
            PdfDocument.Page page = document.startPage(info);
            Canvas canvas = page.getCanvas();
            canvas.drawColor(Color.WHITE);

            canvas.drawRect(15, 15, width - 15, height - 15, line);

            Paint emblemPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            emblemPaint.setColor(Color.BLACK);
            emblemPaint.setTextAlign(Paint.Align.CENTER);

            int emblemBoxRight = width - 30;
            int emblemBoxTop = 20;
            int emblemBoxWidth = 65;
            int emblemBoxHeight = 46;
            
            canvas.drawRect(emblemBoxRight - emblemBoxWidth, emblemBoxTop, emblemBoxRight, emblemBoxTop + emblemBoxHeight, line);
            emblemPaint.setTextSize(16);
            canvas.drawText("🦅", emblemBoxRight - (emblemBoxWidth / 2f), emblemBoxTop + 22, emblemPaint);
            emblemPaint.setTextSize(6.5f);
            canvas.drawText("الجمهورية اليمنية", emblemBoxRight - (emblemBoxWidth / 2f), emblemBoxTop + 38, emblemPaint);

            int logoBoxLeft = 30;
            int logoBoxTop = 20;
            int logoBoxWidth = 55;
            int logoBoxHeight = 46;
            
            File logoFile = new File(getFilesDir(), "school_logo.png");
            if (logoFile.exists()) {
                try {
                    Bitmap logoBitmap = BitmapFactory.decodeFile(logoFile.getAbsolutePath());
                    if (logoBitmap != null) {
                        Rect dstRect = new Rect(logoBoxLeft, logoBoxTop, logoBoxLeft + logoBoxWidth, logoBoxTop + logoBoxHeight);
                        canvas.drawBitmap(logoBitmap, null, dstRect, null);
                    }
                } catch (Exception e) {
                    canvas.drawRect(logoBoxLeft, logoBoxTop, logoBoxLeft + logoBoxWidth, logoBoxTop + logoBoxHeight, line);
                }
            } else {
                canvas.drawRect(logoBoxLeft, logoBoxTop, logoBoxLeft + logoBoxWidth, logoBoxTop + logoBoxHeight, line);
                emblemPaint.setTextSize(6f);
                canvas.drawText("شعار المدرسة", logoBoxLeft + (logoBoxWidth / 2f), logoBoxTop + 25, emblemPaint);
            }

            p.setTextSize(11);
            p.setFakeBoldText(true);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("الجمهورية اليمنية - وزارة التربية والتعليم", width / 2f, 32, p);
            canvas.drawText("مدرسة جعفر بن أبي طالب الأساسية", width / 2f, 47, p);

            p.setTextSize(12);
            canvas.drawText("كشف النتائج السنوية التفصيلي - الصف " + grade + " / " + classroom, width / 2f, 65, p);

            p.setFakeBoldText(false);
            p.setTextSize(7.5f);

            int top = 78;
            int rowHeight = 22;

            int currentX = width - 25;
            for (int col = 0; col < headers.size(); col++) {
                int colWidth = widths.get(col);
                currentX -= colWidth;

                canvas.drawRect(currentX, top, currentX + colWidth, top + rowHeight, line);
                drawCenteredText(canvas, headers.get(col), currentX, top, colWidth, rowHeight, p, 7);
            }

            int y = top + rowHeight;
            for (int i = start; i < Math.min(start + perPage, students.size()); i++) {
                String[] st = students.get(i);
                String sid = st[1];
                String sname = st[0];

                currentX = width - 25;
                
                currentX -= widths.get(0);
                canvas.drawRect(currentX, y, currentX + widths.get(0), y + rowHeight, line);
                drawCenteredText(canvas, String.valueOf(i + 1), currentX, y, widths.get(0), rowHeight, p, 8);

                currentX -= widths.get(1);
                canvas.drawRect(currentX, y, currentX + widths.get(1), y + rowHeight, line);
                canvas.drawText(sname, currentX + 5, y + 15, p);

                double studentGrandTotal = 0;
                int headerIndex = 2;

                for (String subject : activeSubjects) {
                    double m1 = db.getMahsala(sid, subject, 1);
                    double e1 = db.getExam(sid, subject, 1);
                    double m2 = db.getMahsala(sid, subject, 2);
                    double e2 = db.getExam(sid, subject, 2);
                    
                    double subAnnualTotal = (m1 + e1) + (m2 + e2);
                    studentGrandTotal += subAnnualTotal;

                    double totalMahsala = m1 + m2; 
                    double totalExam = e1 + e2;

                    int wMah = widths.get(headerIndex++);
                    currentX -= wMah;
                    canvas.drawRect(currentX, y, currentX + wMah, y + rowHeight, line);
                    drawCenteredText(canvas, formatNumber(totalMahsala), currentX, y, wMah, rowHeight, p, 8);

                    int wExam = widths.get(headerIndex++);
                    currentX -= wExam;
                    canvas.drawRect(currentX, y, currentX + wExam, y + rowHeight, line);
                    drawCenteredText(canvas, formatNumber(totalExam), currentX, y, wExam, rowHeight, p, 8);

                    int wTot = widths.get(headerIndex++);
                    currentX -= wTot;
                    canvas.drawRect(currentX, y, currentX + wTot, y + rowHeight, line);
                    drawCenteredText(canvas, formatNumber(subAnnualTotal), currentX, y, wTot, rowHeight, p, 8);
                }

                int wGenTot = widths.get(headerIndex++);
                currentX -= wGenTot;
                canvas.drawRect(currentX, y, currentX + wGenTot, y + rowHeight, line);
                drawCenteredText(canvas, formatNumber(studentGrandTotal), currentX, y, wGenTot, rowHeight, p, 8);

                int wRes = widths.get(headerIndex++);
                currentX -= wRes;
                canvas.drawRect(currentX, y, currentX + wRes, y + rowHeight, line);
                
                double maxPossible = activeSubjects.size() * 100.0;
                boolean isPassed = (studentGrandTotal >= (maxPossible * 0.5));
                
                p.setFakeBoldText(true);
                drawCenteredText(canvas, isPassed ? "ناجح" : "راسب", currentX, y, wRes, rowHeight, p, 8);
                p.setFakeBoldText(false);

                y += rowHeight;
            }

            document.finishPage(page);
        }

        savePdf(document, "class_detailed_sheet_" + grade + "_" + classroom + ".pdf");
    }


    int semesterRank(ArrayList<String[]> students, String sid, int semester){
        double mine=semesterStudentTotal(sid,semester); int rank=1;
        for(String[] s:students) if(s!=null&&s.length>1&&!s[1].equals(sid)&&semesterStudentTotal(s[1],semester)>mine)rank++;
        return rank;
    }
    double semesterStudentTotal(String sid,int sem){double t=0;for(String sub:db.subjects())t+=db.getMahsala(sid,sub,sem)+db.getExam(sid,sub,sem);return t;}
    void drawExamCardHeader(Canvas canvas,Paint p,int left,int right,int top,String grade,String classroom){
        android.content.SharedPreferences pref=getSharedPreferences("exam_header",MODE_PRIVATE);
        String rightText=pref.getString("right","الجمهورية اليمنية\\nوزارة التربية والتعليم\\nمحافظة: ........");
        String leftText=pref.getString("left","مكتب التربية والتعليم مديرية: ........\\nمدرسة: جعفر بن أبي طالب الأساسية\\nالصف: "+grade+" / "+classroom);
        p.setTextSize(7.5f);p.setFakeBoldText(true);p.setTextAlign(Paint.Align.RIGHT);
        drawMultiline(canvas,rightText,right-8,top+13,p,9);
        p.setTextAlign(Paint.Align.LEFT);drawMultiline(canvas,leftText,left+8,top+13,p,9);
        p.setTextAlign(Paint.Align.CENTER);p.setTextSize(24);canvas.drawText("🦅",widthCenter(left,right),top+30,p);
        p.setTextSize(7);canvas.drawText("النسر اليمني",widthCenter(left,right),top+43,p);p.setFakeBoldText(false);
    }
    float widthCenter(int l,int r){return (l+r)/2f;}
    void drawMultiline(Canvas c,String str,float x,float y,Paint p,float lineHeight){String[] lines=str.split("\\n");for(int i=0;i<Math.min(lines.length,3);i++)c.drawText(lines[i],x,y+i*lineHeight,p);}
    void editExamHeader(){
        android.content.SharedPreferences pref=getSharedPreferences("exam_header",MODE_PRIVATE);
        EditText right=edit("الجهة اليمنى: الجمهورية و الوزارة و المحافظة");right.setText(pref.getString("right","الجمهورية اليمنية\\nوزارة التربية والتعليم\\nمحافظة: ........"));
        EditText left=edit("الجهة اليسرى: المكتب والمديرية والمدرسة والصف");left.setText(pref.getString("left","مكتب التربية والتعليم مديرية: ........\\nمدرسة: جعفر بن أبي طالب الأساسية\\nالصف:"));
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.addView(right);box.addView(left);
        new AlertDialog.Builder(this).setTitle("ترويسة بطاقات النتائج").setView(box).setPositiveButton("حفظ",(d,w)->pref.edit().putString("right",right.getText().toString()).putString("left",left.getText().toString()).apply()).setNegativeButton("إلغاء",null).show();
    }
    void manageSubjects(){
        page("إدارة المواد");
        Button add=btn("＋ إضافة مادة");content.addView(add,new LinearLayout.LayoutParams(-1,dp(48)));add.setOnClickListener(v->{EditText e=edit("اسم المادة");new AlertDialog.Builder(this).setTitle("إضافة مادة").setView(e).setPositiveButton("حفظ",(d,w)->{if(db.addSubject(e.getText().toString()))manageSubjects();else Toast.makeText(this,"تعذر إضافة المادة (قد تكون موجودة)",Toast.LENGTH_SHORT).show();}).setNegativeButton("إلغاء",null).show();});
        for(String subject:db.subjects()){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);TextView name=tv(subject,14,true);row.addView(name,new LinearLayout.LayoutParams(0,dp(50),1));Button edit=smallBtn("تعديل",BLUE);row.addView(edit);Button del=smallBtn("حذف",RED);row.addView(del);content.addView(row);edit.setOnClickListener(v->{EditText e=edit("اسم المادة");e.setText(subject);new AlertDialog.Builder(this).setTitle("تعديل المادة").setView(e).setPositiveButton("حفظ",(d,w)->{db.renameSubject(subject,e.getText().toString());manageSubjects();}).setNegativeButton("إلغاء",null).show();});del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("حذف المادة").setMessage("سيتم حذف سجلات درجات هذه المادة. هل تريد المتابعة؟").setPositiveButton("حذف",(d,w)->{db.removeSubject(subject);manageSubjects();}).setNegativeButton("إلغاء",null).show());}
    }

    private void drawCenteredText(Canvas canvas, String text, int left, int top, int width, int height, Paint paint, float textSize) {
        paint.setTextSize(textSize);
        float textWidth = paint.measureText(text);
        float x = left + (width - textWidth) / 2f;
        float y = top + (height / 2f) + (textSize / 3f);
        canvas.drawText(text, x, y, paint);
    }

    void generateExamCardsPdf(String grade, String classroom, int semester) {
        ArrayList<String[]> students = studentsForClass(grade, classroom);
        if (students == null || students.isEmpty()) {
            Toast.makeText(this, "لا يوجد طلاب في الصف " + grade + " - " + classroom, Toast.LENGTH_LONG).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        final int width = 595, height = 842;
        final int margin = 12, gap = 8;
        final int cardHeight = (height - 2 * margin - 2 * gap) / 3;

        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setColor(Color.BLACK);
        text.setTextAlign(Paint.Align.CENTER);

        border.setColor(Color.BLACK);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(1.2f);

        int pageNumber = 0;

        try {
            for (int start = 0; start < students.size(); start += 3) {
                pageNumber++;
                PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(width, height, pageNumber).create();
                PdfDocument.Page pdfPage = document.startPage(info);
                Canvas canvas = pdfPage.getCanvas();
                canvas.drawColor(Color.WHITE);

                for (int k = 0; k < 3 && start + k < students.size(); k++) {
                    String[] student = students.get(start + k);
                    if (student == null || student.length < 2) continue;

                    String studentName = student[0] == null ? "" : student[0];
                    String studentId = student[1] == null ? "" : student[1];

                    int left = margin, right = width - margin;
                    int top = margin + k * (cardHeight + gap);
                    int bottom = top + cardHeight;

                    canvas.drawRect(left, top, right, bottom, border);
                    canvas.drawRect(left + 3, top + 3, right - 3, bottom - 3, border);

                    text.setTextSize(9);
                    text.setFakeBoldText(true);
                    drawExamCardHeader(canvas, text, left, right, top, grade, classroom);

                    text.setTextSize(11);
                    canvas.drawText(
                        "نتيجة نهاية " + (semester == 1 ? "الفصل الدراسي الأول" : "الفصل الدراسي الثاني"),
                        width / 2f, top + 48, text
                    );

                    text.setTextSize(9);
                    text.setFakeBoldText(false);
                    canvas.drawText("اسم الطالب: " + studentName, width / 2f, top + 62, text);
                    canvas.drawText("الصف: " + grade + "     الشعبة: " + classroom + "     الرقم: " + studentId, width / 2f, top + 75, text);

                    int tableTop = top + 96;
                    ArrayList<String> subjects = new ArrayList<>(Arrays.asList(db.subjects()));
                    int labelW = 48, usableW = right-left-10-labelW;
                    int cellW = subjects.isEmpty()?1:usableW/subjects.size();
                    int rowH = 18;
                    text.setTextSize(7);
                    text.setFakeBoldText(true);
                    canvas.drawLine(left+5,tableTop,right-5,tableTop,border);
                    canvas.drawText("المادة",left+5+labelW/2f,tableTop+12,text);
                    for(int si=0;si<subjects.size();si++)canvas.drawText(subjects.get(si),left+5+labelW+si*cellW+cellW/2f,tableTop+12,text);
                    canvas.drawLine(left+5,tableTop+rowH,right-5,tableTop+rowH,border);
                    double total=0;
                    String[] rowLabels={"المحصلة","الاختبار","المجموع"};
                    for(int ri=0;ri<3;ri++){
                        int yy=tableTop+rowH*(ri+1);
                        text.setTextSize(7.5f);text.setFakeBoldText(ri==2);
                        canvas.drawText(rowLabels[ri],left+5+labelW/2f,yy+12,text);
                        for(int si=0;si<subjects.size();si++){
                            String sub=subjects.get(si);double m=db.getMahsala(studentId,sub,semester),e=db.getExam(studentId,sub,semester);
                            double st=m+e;if(ri==2)total+=st;
                            canvas.drawText(formatNumber(ri==0?m:ri==1?e:st),left+5+labelW+si*cellW+cellW/2f,yy+12,text);
                        }
                        canvas.drawLine(left+5,yy+rowH,right-5,yy+rowH,border);
                    }
                    int y=tableTop+rowH*4, summaryY=y+12;
                    text.setTextSize(9);text.setFakeBoldText(true);
                    int subjectCount=subjects.size();
                    canvas.drawText("المجموع: "+formatNumber(total)+" / "+(subjectCount*50),width/2f,summaryY,text);
                    double percentage=subjectCount==0?0:total/(subjectCount*50.0)*100.0;
                    text.setTextSize(8);text.setFakeBoldText(false);
                    canvas.drawText("النسبة: "+formatNumber(percentage)+"%   التقدير: "+DB.gradeFor(percentage)+"   النتيجة: "+(percentage>=50?"ناجح":"راسب"),width/2f,summaryY+11,text);

                    canvas.drawText("توقيع المعلم: ................", width / 4f, bottom - 8, text);
                    canvas.drawText("مدير المدرسة: ................", width * 3f / 4f, bottom - 8, text);
                }

                document.finishPage(pdfPage);
            }

            savePdf(document, "exam_cards_" + grade + "_" + classroom + ".pdf");

        } catch (Exception e) {
            try {
                document.close();
            } catch (Exception ignored) {}

            Toast.makeText(this, "تعذر إنشاء بطاقات النتائج: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
