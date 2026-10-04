package com.jafar.school;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.webkit.*;
import android.print.*;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import android.text.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {ArrayList<String> pageHistory = new ArrayList<>();
String currentPage = "home";
    static final int REQ_BACKUP=7101, REQ_RESTORE=7102;
    LinearLayout root, content;
    String role="القائم بأعمال المدير";
    String currentUser="";
    String currentUsername="";
    String assignedGrade="";
    String assignedClass="";
    String currentStudentId="";
    DB db;
    final int BLUE=Color.rgb(17,96,177), DARK=Color.rgb(11,55,105), BG=Color.rgb(244,247,251), GREEN=Color.rgb(35,145,88), RED=Color.rgb(202,57,66), ORANGE=Color.rgb(231,133,28), WHITE=Color.WHITE;
    final String[] DAYS={"السبت","الأحد","الاثنين","الثلاثاء","الأربعاء"};

    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);db=new DB(this);showLogin();}
    TextView tv(String s,int sp,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(Color.rgb(25,45,68));t.setTypeface(null,bold?Typeface.BOLD:Typeface.NORMAL);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(14,9,14,9);return t;}
    Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(14);b.setTextColor(WHITE);b.setAllCaps(false);b.setPadding(8,2,8,2);GradientDrawable g=new GradientDrawable();g.setColor(BLUE);g.setCornerRadius(18);b.setBackground(g);return b;}
    void base(){root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);setContentView(root);}
    void showLogin(){
        base(); LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setPadding(26,25,26,25);root.addView(box,new LinearLayout.LayoutParams(-1,-1));
        TextView logo=tv("🏫",64,true);logo.setGravity(Gravity.CENTER);box.addView(logo);
        TextView h=tv("مدرسة جعفر بن أبي طالب الأساسية",24,true);h.setGravity(Gravity.CENTER);box.addView(h);
        TextView sub=tv("الجمهورية اليمنية\nإب - مذيخرة - الأشعوب\nنظام الإدارة المدرسية الذكي",16,false);sub.setGravity(Gravity.CENTER);box.addView(sub);
        box.addView(tv("تسجيل الدخول",21,true));
        EditText user=new EditText(this);user.setHint("اسم المستخدم");box.addView(user);
        EditText pass=new EditText(this);pass.setHint("كلمة المرور");pass.setInputType(0x81);box.addView(pass);
        Button go=btn("دخول");box.addView(go,new LinearLayout.LayoutParams(-1,58));
        TextView hint=tv("الحساب الأولي: admin / 1234\nيمكن تغييره من المستخدمين والصلاحيات.",13,false);hint.setGravity(Gravity.CENTER);box.addView(hint);
        go.setOnClickListener(v->{
            String[] a=db.authenticate(user.getText().toString().trim(),pass.getText().toString());
            if(a==null){Toast.makeText(this,"اسم المستخدم أو كلمة المرور غير صحيحة",Toast.LENGTH_SHORT).show();return;}
            currentUser=a[0]; currentUsername=a[4]; role=a[1]; assignedGrade=a[2]; assignedClass=a[3]; currentStudentId=a.length>5?a[5]:""; if(role.equals("طالب")||role.equals("ولي أمر")){showPortal();}else{showHome();}
        });
    }
    void showPortal(){
        base();
        LinearLayout head=new LinearLayout(this); head.setPadding(8,8,8,3); head.setGravity(Gravity.CENTER_VERTICAL);
        TextView h=tv("🏫 مدرسة جعفر\n"+role,19,true); head.addView(h,new LinearLayout.LayoutParams(0,72,1));
        Button out=btn("خروج"); out.setOnClickListener(v->showLogin()); head.addView(out,new LinearLayout.LayoutParams(84,52)); root.addView(head);
        ScrollView sv=new ScrollView(this); content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(9,5,9,28); sv.addView(content); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        addCard("📴 وضع التشغيل","البوابة تعمل بدون إنترنت، وتعرض البيانات المسموح بها فقط.");
        ArrayList<String[]> links=db.linkedStudents(currentUsername);
        if(links.isEmpty()){addCard("لا يوجد طالب مرتبط","اطلب من مدير المدرسة ربط حسابك بالطالب/الطلاب.");return;}
        addCard("🔔 آخر الإعلانات",db.latestAnnouncements());
        for(String[] st:links){
            addCard("👨‍🎓 "+st[0],"الرقم: "+st[1]+"\nالصف: "+st[2]+" — "+st[3]+"\n\nالفصل الأول: "+db.resultLine(st[1],0)+"\nالفصل الثاني: "+db.resultLine(st[1],1)+"\nالنتيجة السنوية: "+db.resultLine(st[1],2)+"\nالحضور: "+db.studentAttendanceSummary(st[1]));
            Button pdf=btn("📄 ملف الطالب PDF"); content.addView(pdf); final String id=st[1]; pdf.setOnClickListener(v->generateStudentPdf(id));
            addCard("📅 الجدول الدراسي","السبت: "+db.daySchedule("السبت")+"\n\nالأحد: "+db.daySchedule("الأحد")+"\n\nالاثنين: "+db.daySchedule("الاثنين")+"\n\nالثلاثاء: "+db.daySchedule("الثلاثاء")+"\n\nالأربعاء: "+db.daySchedule("الأربعاء"));
        }
    }

    boolean admin(){return role.equals("القائم بأعمال المدير");}

boolean teacher(){
    return role.equals("معلم") || admin();
}

void showHome(){
    currentPage = "home";
    base();

    LinearLayout head = new LinearLayout(this);
    head.setOrientation(LinearLayout.HORIZONTAL);
    head.setGravity(Gravity.CENTER_VERTICAL);
    head.setPadding(12,10,12,8);

    TextView logo = tv("📖",30,true);
    logo.setGravity(Gravity.CENTER);
    head.addView(logo,new LinearLayout.LayoutParams(58,70));

    TextView h = tv(
            "مدرسة جعفر\n" + role,
            19,
            true
    );
    h.setGravity(Gravity.CENTER_VERTICAL);
    head.addView(h,new LinearLayout.LayoutParams(0,70,1));

    Button out = btn("خروج");
    out.setTextSize(15);
    head.addView(out,new LinearLayout.LayoutParams(92,58));
    out.setOnClickListener(v->showLogin());

    root.addView(head);

    ScrollView sv = new ScrollView(this);
    sv.setFillViewport(true);

    content = new LinearLayout(this);
    content.setOrientation(LinearLayout.VERTICAL);
    content.setPadding(10,6,10,20);

    sv.addView(content);

    root.addView(
            sv,
            new LinearLayout.LayoutParams(-1,0,1)
    );

    addCard(
            "🏫 لوحة التحكم",
            "أهلاً بك في مدرسة جعفر\nأنت: " + role +
            "\nجميع البيانات محفوظة محلياً ويمكن تشغيل البرنامج دون إنترنت."
    );

    addStats();

    if(teacher()){
        Button my = btn("👨‍🏫 صفي / مهامي");
        LinearLayout.LayoutParams mp =
                new LinearLayout.LayoutParams(-1,62);
        mp.setMargins(4,5,4,5);
        content.addView(my,mp);
        my.setOnClickListener(v->myClass());
    }

    addGrid();

    addCard(
            "📢 آخر الإعلانات",
            db.latestAnnouncements()
    );
}
    void myClass(){
        page("👨‍🏫 صفي / مهامي");
        String g=assignedGrade, c=assignedClass;
        if(g==null||g.isEmpty()){addCard("لم يتم تعيين صف","يمكن للقائم بأعمال المدير تحديد صفه من إعدادات المستخدم.");return;}
        addCard("صفّي", "الصف: "+g+"\nالشعبة: "+(c==null?"":c));
        for(String[] r:db.studentsInClass(g,c)) addCard("👨‍🎓 "+r[0],"الرقم: "+r[1]+"\nالحضور اليوم: "+(db.attendanceStatus(r[1]).isEmpty()?"لم يسجل":db.attendanceStatus(r[1]))+"\nالدرجات: "+db.studentGrades(r[1]));
    }
void addStats(){
    LinearLayout r = new LinearLayout(this);
    r.setOrientation(LinearLayout.HORIZONTAL);
    r.setGravity(Gravity.CENTER);

    String[] a = {
        "👨‍🎓\nالطلاب\n" + db.count("students"),
        "👨‍🏫\nالمعلمون\n" + db.count("teachers"),
        "🟢\nحاضر اليوم\n" + db.todayPresent(),
        "🔴\nغائب اليوم\n" + db.todayAbsent()
    };

    for(String x : a){
        TextView t = tv(x,14,true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(4,8,4,8);
        t.setBackgroundColor(WHITE);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(0,105,1);

        p.setMargins(3,4,3,4);
        r.addView(t,p);
    }

    content.addView(r);
}
    
void addGrid(){

    ArrayList<String[]> list = new ArrayList<>();

    list.add(new String[]{"👨‍🎓 الطلاب","students"});
    list.add(new String[]{"📊 الدرجات","grades"});
    list.add(new String[]{"✅ الحضور","attendance"});
    list.add(new String[]{"📅 الجدول","timetable"});

    if(admin()){

        list.add(new String[]{"👨‍🏫 المعلمون","teachers"});
        list.add(new String[]{"🏫 الصفوف والشعب","classes"});
        list.add(new String[]{"📈 التقارير","reports"});
        list.add(new String[]{"🔔 الإعلانات","announcements"});
        list.add(new String[]{"🔐 المستخدمون والصلاحيات","users"});
        list.add(new String[]{"📁 الملفات","files"});
        list.add(new String[]{"⚙️ الإعدادات","settings"});

    }else{

        list.add(new String[]{"👨‍🏫 صفي / مهامي","myclass"});
        list.add(new String[]{"👨‍🎓 طلاب صفي","students"});
        list.add(new String[]{"🔔 الإعلانات","announcements"});
        list.add(new String[]{"⚙️ حسابي","settings"});
    }

    LinearLayout grid = new LinearLayout(this);
    grid.setOrientation(LinearLayout.VERTICAL);

    for(int i=0;i<list.size();i+=2){

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams rp =
                new LinearLayout.LayoutParams(-1,88);

        rp.setMargins(0,2,0,2);

        grid.addView(row,rp);

        for(int j=i;j<i+2 && j<list.size();j++){

            String[] item = list.get(j);

            Button b = btn(item[0]);
            b.setTextSize(15);
            b.setAllCaps(false);

            LinearLayout.LayoutParams bp =
                    new LinearLayout.LayoutParams(0,78,1);

            bp.setMargins(3,3,3,3);

            row.addView(b,bp);

            final String key = item[1];

            b.setOnClickListener(v -> {

                pageHistory.add(currentPage);
                currentPage = key;

                if(key.equals("myclass")){
                    myClass();
                }else{
                    open(key);
                }

            });
        }
    }

    content.addView(
            grid,
            new LinearLayout.LayoutParams(-1,-2)
    );
}
    void open(String k){if(k.equals("students")){students();return;}if(k.equals("teachers")){teachers();return;}if(k.equals("classes")){classes();return;}if(k.equals("grades")){grades();return;}if(k.equals("attendance")){attendance();return;}if(k.equals("timetable")){timetable();return;}if(k.equals("reports")){reports();return;}if(k.equals("announcements")){announcements();return;}if(k.equals("users")){users();return;}if(k.equals("files")){files();return;}if(k.equals("settings")){settings();}}
    void page(String name){
    base();

    LinearLayout top = new LinearLayout(this);
    top.setOrientation(LinearLayout.HORIZONTAL);
    top.setGravity(Gravity.CENTER_VERTICAL);
    top.setPadding(8,6,8,6);

    Button back = btn("↩ رجوع");

    back.setTextSize(16);
    back.setAllCaps(false);

    /*
     * مساحة الضغط الفعلية أكبر من شكل الزر
     * حتى يكون الضغط عليه سهلاً في الهاتف.
     */
    LinearLayout.LayoutParams bp =
            new LinearLayout.LayoutParams(125,68);

    bp.setMargins(6,4,10,4);

    top.addView(back,bp);

    TextView title = tv(name,20,true);
    title.setGravity(Gravity.CENTER_VERTICAL);

    top.addView(
            title,
            new LinearLayout.LayoutParams(0,68,1)
    );

    root.addView(top);

    back.setOnClickListener(v->goBack());

    content = new LinearLayout(this);
    content.setOrientation(LinearLayout.VERTICAL);
    content.setPadding(10,8,10,28);

    ScrollView s = new ScrollView(this);
    s.setFillViewport(true);
    s.addView(content);

    root.addView(
            s,
            new LinearLayout.LayoutParams(-1,0,1)
    );
}
   void goBack(){

    if(pageHistory.size() > 0){

        String previous =
                pageHistory.remove(pageHistory.size()-1);

        if(previous.equals("home")){
            showHome();
            return;
        }

        currentPage = previous;

        if(previous.equals("students")) students();
        else if(previous.equals("teachers")) teachers();
        else if(previous.equals("classes")) classes();
        else if(previous.equals("grades")) grades();
        else if(previous.equals("attendance")) attendance();
        else if(previous.equals("timetable")) timetable();
        else if(previous.equals("reports")) reports();
        else if(previous.equals("announcements")) announcements();
        else if(previous.equals("users")) users();
        else if(previous.equals("files")) files();
        else if(previous.equals("settings")) settings();
        else if(previous.equals("myclass")) myClass();
        else showHome();

    }else{
        showHome();
    }
} 
    void students(){
        page("👨‍🎓 إدارة الطلاب");
        EditText search=new EditText(this);search.setHint("ابحث بالاسم أو الرقم");content.addView(search);
        if(admin()){Button add=btn("＋ إضافة طالب");content.addView(add);add.setOnClickListener(v->studentDialog(null));}
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);content.addView(list);
        Runnable refresh=()->{list.removeAllViews();ArrayList<String[]> rows;String q=search.getText().toString().trim();
            if(admin()) rows=db.students(q);
            else {rows=new ArrayList<>(); if(assignedGrade!=null&&!assignedGrade.isEmpty()){for(String[] r:db.studentsInClass(assignedGrade,assignedClass)){if(q.isEmpty()||r[0].contains(q)||r[1].contains(q))rows.add(new String[]{r[0],r[1],assignedGrade,assignedClass});}}}
            for(String[] r:rows){Button b=btn(r[0]+" | "+r[1]+" | "+r[2]+" / "+r[3]);list.addView(b,new LinearLayout.LayoutParams(-1,58));b.setOnClickListener(v->studentDetails(r[1]));}};
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int b,int c,int d){refresh.run();}public void afterTextChanged(Editable e){}});refresh.run();
    }
    void studentDialog(String id){final boolean edit=id!=null;String[] old=edit?db.student(id):null;final EditText name=new EditText(this);name.setHint("اسم الطالب الرباعي");final EditText sid=new EditText(this);sid.setHint("الرقم المدرسي");final EditText grade=new EditText(this);grade.setHint("الصف");final EditText cls=new EditText(this);cls.setHint("الشعبة");final EditText parent=new EditText(this);parent.setHint("اسم ولي الأمر");final EditText phone=new EditText(this);phone.setHint("هاتف ولي الأمر");if(old!=null){name.setText(old[0]);sid.setText(old[1]);grade.setText(old[2]);cls.setText(old[3]);parent.setText(old[4]);phone.setText(old[5]);sid.setEnabled(false);}LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);for(EditText e:new EditText[]{name,sid,grade,cls,parent,phone})l.addView(e);new AlertDialog.Builder(this).setTitle(edit?"تعديل الطالب":"إضافة طالب").setView(l).setPositiveButton("حفظ",(d,w)->{db.saveStudent(name.getText().toString(),sid.getText().toString(),grade.getText().toString(),cls.getText().toString(),parent.getText().toString(),phone.getText().toString());students();}).setNegativeButton("إلغاء",null).show();}
    void studentDetails(String id){
        String[] r=db.student(id);page("👨‍🎓 ملف الطالب");if(r==null)return;
        addCard("👨‍🎓 "+r[0],"الرقم: "+r[1]+"\nالصف: "+r[2]+"\nالشعبة: "+r[3]+"\nولي الأمر: "+r[4]+"\nالهاتف: "+r[5]);
        LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);
        Button pdf=btn("📄 طباعة ملف الطالب");actions.addView(pdf,new LinearLayout.LayoutParams(-1,55));
        if(admin()){ Button e=btn("✏️ تعديل"); actions.addView(e,new LinearLayout.LayoutParams(0,55,1)); e.setOnClickListener(v->studentDialog(id)); }
        content.addView(actions);
        pdf.setOnClickListener(v->generateStudentPdf(id));
        addCard("📊 ملخص النتائج",db.resultLine(id,0)+"\n"+db.resultLine(id,1)+"\n"+db.resultLine(id,2));
        addCard("📝 الدرجات الشهرية",db.studentMonthlyDetails(id));
        addCard("🧪 الاختبارات",db.studentExams(id));
        addCard("📅 الحضور والغياب",db.studentAttendanceSummary(id));
        addCard("🗒️ ملاحظات الطالب",db.studentNote(id).isEmpty()?"لا توجد ملاحظات مسجلة.":db.studentNote(id));
        Button note=btn("🗒️ إضافة / تعديل ملاحظة");content.addView(note);note.setOnClickListener(v->{EditText n=new EditText(this);n.setHint("ملاحظات تربوية أو إدارية");n.setMinLines(4);n.setText(db.studentNote(id));new AlertDialog.Builder(this).setTitle("ملاحظات الطالب").setView(n).setPositiveButton("حفظ",(d,w)->{db.setStudentNote(id,n.getText().toString());studentDetails(id);}).setNegativeButton("إلغاء",null).show();});
    }

    void teachers(){page("👨‍🏫 الكادر التدريسي");if(admin()){Button add=btn("＋ إضافة معلم");content.addView(add);add.setOnClickListener(v->teacherDialog());}for(String[] r:db.teachers())addCard("👨‍🏫 "+r[0],"المادة: "+r[1]+"\nالدور: "+r[2]);}
    void teacherDialog(){final EditText n=new EditText(this);n.setHint("اسم المعلم");final EditText s=new EditText(this);s.setHint("المادة");final Spinner ro=new Spinner(this);ro.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"معلم","قائم بأعمال المدير"}));LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.addView(n);l.addView(s);l.addView(ro);new AlertDialog.Builder(this).setTitle("إضافة معلم").setView(l).setPositiveButton("حفظ",(d,w)->{db.addTeacher(n.getText().toString(),s.getText().toString(),ro.getSelectedItem().toString());teachers();}).setNegativeButton("إلغاء",null).show();}

    void classes(){page("🏫 الصفوف والشعب");if(admin()){Button add=btn("＋ إضافة صف/شعبة");content.addView(add);add.setOnClickListener(v->classDialog());}for(String[] r:db.classes())addCard("🏫 الصف "+r[0]+" — الشعبة "+r[1],"عدد الطلاب: "+r[2]);}
    void classDialog(){final EditText g=new EditText(this);g.setHint("اسم الصف (مثال: الأول)");final EditText c=new EditText(this);c.setHint("الشعبة (مثال: أ)");LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.addView(g);l.addView(c);new AlertDialog.Builder(this).setTitle("إضافة صف وشعبة").setView(l).setPositiveButton("حفظ",(d,w)->{db.addClass(g.getText().toString(),c.getText().toString());classes();}).setNegativeButton("إلغاء",null).show();}

    void attendance(){
        page("✅ الحضور والغياب");
        String day=new SimpleDateFormat("EEEE",new Locale("ar")).format(new Date()); addCard("اليوم",day+" — "+date());
        if(day.contains("الخميس")||day.contains("الجمعة")){addCard("🏖️ عطلة رسمية","لا يتم تسجيل الحضور في أيام الخميس والجمعة.");return;}
        if(admin()){ArrayList<String[]> cs=db.classes();String[] items=new String[cs.size()];for(int i=0;i<cs.size();i++)items[i]="الصف "+cs.get(i)[0]+" — "+cs.get(i)[1];new AlertDialog.Builder(this).setTitle("اختر الصف والشعبة").setItems(items,(d,w)->attendanceClass(cs.get(w)[0],cs.get(w)[1])).setNegativeButton("إلغاء",null).show();}
        else if(assignedGrade!=null&&!assignedGrade.isEmpty()) attendanceClass(assignedGrade,assignedClass); else addCard("لم يتم تعيين صف","اطلب من المدير تحديد الصف والشعبة لحسابك.");
    }
    void attendanceClass(String grade,String classroom){
        page("✅ حضور الصف "+grade+" / "+classroom); addCard("الصف المحدد","الصف: "+grade+"\nالشعبة: "+classroom);
        for(String[] r:db.studentsInClass(grade,classroom)){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.addView(tv(r[0],15,true),new LinearLayout.LayoutParams(0,60,1));Spinner sp=new Spinner(this);String[] opts={"لم يسجل","حاضر","غائب","متأخر","بعذر"};sp.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,opts));String old=db.attendanceStatus(r[1]);for(int i=0;i<opts.length;i++)if(opts[i].equals(old))sp.setSelection(i);sp.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){String x=opts[pos];if(!x.equals("لم يسجل"))db.setAttendance(r[1],x);}});row.addView(sp,new LinearLayout.LayoutParams(125,55));content.addView(row);}
    }

    void grades(){
        page("📊 الدرجات والنتائج");
        addCard("نظام الدرجات المعتمد","لكل شهر: المواظبة 20 + الاختبار الشفوي 20 + الواجبات 20 + الاختبار التحريري 40 = 100.\nثلاثة أشهر = 300، وتحسب المحصلة من 20 بقسمة المجموع على 15.\nالفصل الأول: محصلة 20 + نصف العام 30 = 50.\nالفصل الثاني: محصلة 20 + نهاية العام 30 = 50.\nالسنوي = 100.");
        Button entry=btn("✏️ إدارة درجات الصف"); content.addView(entry); entry.setOnClickListener(v->chooseClassForGrades());
        Button results=btn("📋 النتائج والتقارير"); content.addView(results); results.setOnClickListener(v->resultsScreen());
        Button pdf=btn("📄 إنشاء PDF للصف"); content.addView(pdf); pdf.setOnClickListener(v->pdfOptions());
    }
    void chooseClassForGrades(){
        if(!admin() && assignedGrade!=null && !assignedGrade.isEmpty()){classGrades(assignedGrade,assignedClass);return;}
        ArrayList<String[]> cs=db.classes();
        if(cs.isEmpty()){Toast.makeText(this,"لا توجد صفوف وشعب",Toast.LENGTH_SHORT).show();return;}
        String[] items=new String[cs.size()]; for(int i=0;i<cs.size();i++)items[i]="الصف "+cs.get(i)[0]+" — الشعبة "+cs.get(i)[1]+" ("+cs.get(i)[2]+" طالب)";
        new AlertDialog.Builder(this).setTitle("اختر الصف والشعبة").setItems(items,(d,w)->classGrades(cs.get(w)[0],cs.get(w)[1])).setNegativeButton("إلغاء",null).show();
    }
    void classGrades(String grade,String classroom){
        page("📊 إدخال درجات الصف");
        addCard("الصف المحدد","الصف: "+grade+" — الشعبة: "+classroom+"\nيمكن إدخال درجات جميع الطلاب من شاشة واحدة ثم حفظها دفعة واحدة.");
        Spinner semester=new Spinner(this);
        semester.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"الفصل الأول","الفصل الثاني"}));
        Spinner period=new Spinner(this);
        period.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"الشهر الأول","الشهر الثاني","الشهر الثالث","الاختبار / 30"}));
        Spinner subject=new Spinner(this);
        subject.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,db.subjects()));
        content.addView(tv("الفصل",15,true)); content.addView(semester);
        content.addView(tv("الفترة",15,true)); content.addView(period);
        content.addView(tv("المادة",15,true)); content.addView(subject);
        LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); content.addView(list);
        ArrayList<String[]> students=db.studentsInClass(grade,classroom);
        Runnable render=()->{
            list.removeAllViews();
            boolean exam=period.getSelectedItemPosition()==3;
            for(String[] st:students){
                LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
                TextView name=tv(st[0]+"\n"+st[1],14,true); row.addView(name,new LinearLayout.LayoutParams(0,64,1));
                if(exam){
                    EditText e=numberBox("/30"); e.setText(db.exam(st[1],subject.getSelectedItem().toString(),semester.getSelectedItemPosition()+1)==0?"":String.valueOf(db.exam(st[1],subject.getSelectedItem().toString(),semester.getSelectedItemPosition()+1)));
                    row.addView(e,new LinearLayout.LayoutParams(110,58)); row.setTag(e);
                }else{
                    EditText a=numberBox("م/20"),o=numberBox("ش/20"),h=numberBox("و/20"),w=numberBox("ت/40");
                    double[] old=db.monthlyValues(st[1],subject.getSelectedItem().toString(),semester.getSelectedItemPosition()+1,period.getSelectedItemPosition()+1);
                    if(old!=null){a.setText(fmt(old[0]));o.setText(fmt(old[1]));h.setText(fmt(old[2]));w.setText(fmt(old[3]));}
                    row.addView(a,new LinearLayout.LayoutParams(82,58));row.addView(o,new LinearLayout.LayoutParams(82,58));row.addView(h,new LinearLayout.LayoutParams(82,58));row.addView(w,new LinearLayout.LayoutParams(82,58));
                    row.setTag(new EditText[]{a,o,h,w});
                }
                list.addView(row);
            }
        };
        semester.setOnItemSelectedListener(simpleRender(render)); period.setOnItemSelectedListener(simpleRender(render)); subject.setOnItemSelectedListener(simpleRender(render)); render.run();
        Button save=btn("💾 حفظ جميع الدرجات"); content.addView(save,new LinearLayout.LayoutParams(-1,58));
        save.setOnClickListener(v->{
            try{
                boolean exam=period.getSelectedItemPosition()==3; int sem=semester.getSelectedItemPosition()+1;
                for(int i=0;i<list.getChildCount();i++){
                    View rv=list.getChildAt(i); String id=students.get(i)[1];
                    if(exam){ EditText e=(EditText)rv.getTag(); double x=parse(e); if(x<0||x>30)throw new Exception(); db.setExam(id,subject.getSelectedItem().toString(),sem,x); }
                    else { EditText[] z=(EditText[])rv.getTag(); double a=parse(z[0]),o=parse(z[1]),h=parse(z[2]),w=parse(z[3]); if(a<0||a>20||o<0||o>20||h<0||h>20||w<0||w>40)throw new Exception(); db.setMonthlyScore(id,subject.getSelectedItem().toString(),sem,period.getSelectedItemPosition()+1,a,o,h,w); }
                }
                Toast.makeText(this,"تم حفظ درجات جميع الطلاب محلياً",Toast.LENGTH_LONG).show();
                render.run();
            }catch(Exception ex){Toast.makeText(this,"تحقق من الدرجات والحدود قبل الحفظ",Toast.LENGTH_SHORT).show();}
        });
    }
    android.widget.AdapterView.OnItemSelectedListener simpleRender(Runnable r){{return new Spinner.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){r.run();}};}
    EditText numberBox(String hint){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(12);e.setGravity(Gravity.CENTER);e.setInputType(2|8192);return e;}
    double parse(EditText e){String x=e.getText().toString().trim();return x.isEmpty()?0:Double.parseDouble(x);}
    String fmt(double x){return Math.abs(x-Math.round(x))<.001?String.valueOf(Math.round(x)):String.format(Locale.US,"%.1f",x);}
    void chooseStudentForGrades(){ chooseClassForGrades(); }
    void gradeDialog(String id,String name){ gradeDialog(id,name,"",""); }
    void gradeDialog(String id,String name,String grade,String classroom){
        final Spinner sem=new Spinner(this); sem.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"الفصل الأول","الفصل الثاني"}));
        final Spinner month=new Spinner(this); month.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"الشهر الأول","الشهر الثاني","الشهر الثالث"}));
        final Spinner sub=new Spinner(this); sub.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"اللغة العربية","الرياضيات","القرآن الكريم","العلوم","التربية الإسلامية","الاجتماعيات"}));
        final EditText regular=new EditText(this); regular.setHint("المواظبة / 20"); regular.setInputType(2|8192);
        final EditText oral=new EditText(this); oral.setHint("الاختبار الشفوي / 20"); oral.setInputType(2|8192);
        final EditText homework=new EditText(this); homework.setHint("الواجبات / 20"); homework.setInputType(2|8192);
        final EditText written=new EditText(this); written.setHint("الاختبار التحريري / 40"); written.setInputType(2|8192);
        final EditText exam=new EditText(this); exam.setHint("اختبار الفصل / 30 (اختياري)"); exam.setInputType(2|8192);
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.addView(tv("الطالب: "+name+"\n"+(grade.isEmpty()?"":"الصف: "+grade+" / "+classroom),17,true));l.addView(sem);l.addView(month);l.addView(sub);l.addView(regular);l.addView(oral);l.addView(homework);l.addView(written);l.addView(exam);
        sem.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){exam.setHint(pos==0?"اختبار نصف العام / 30 (اختياري)":"اختبار نهاية العام / 30 (اختياري)");}});
        new AlertDialog.Builder(this).setTitle("درجات الطالب").setView(l).setPositiveButton("حفظ",(d,w)->{try{double a=val(regular),o=val(oral),h=val(homework),wr=val(written);if(a<0||a>20||o<0||o>20||h<0||h>20||wr<0||wr>40)throw new Exception();int semester=sem.getSelectedItemPosition()+1,m=month.getSelectedItemPosition()+1;db.setMonthlyScore(id,sub.getSelectedItem().toString(),semester,m,a,o,h,wr);String ex=exam.getText().toString().trim();if(!ex.isEmpty()){double e=Double.parseDouble(ex);if(e<0||e>30)throw new Exception();db.setExam(id,sub.getSelectedItem().toString(),semester,e);}Toast.makeText(this,"تم حفظ الدرجات محلياً",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"تحقق من الدرجات والحدود المحددة",Toast.LENGTH_SHORT).show();}}).setNegativeButton("إلغاء",null).show();
    }
    double val(EditText e){String x=e.getText().toString().trim();return x.isEmpty()?0:Double.parseDouble(x);}
    void resultsScreen(){
        page("📋 النتائج والتقارير");
        Spinner type=new Spinner(this);type.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"الفصل الأول — نصف العام","الفصل الثاني — نهاية العام","النتيجة السنوية"}));
        Spinner grade=new Spinner(this);ArrayList<String> gs=db.gradesList();grade.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,gs));
        Spinner cls=new Spinner(this);ArrayList<String> cs=db.classesForGrade(gs.isEmpty()?"":gs.get(0));cls.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,cs));
        grade.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){ArrayList<String> x=db.classesForGrade(grade.getSelectedItem().toString());cls.setAdapter(new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,x));}});
        content.addView(tv("نوع النتيجة",16,true));content.addView(type);content.addView(tv("الصف",16,true));content.addView(grade);content.addView(tv("الشعبة",16,true));content.addView(cls);
        Button preview=btn("👁 معاينة النتائج");content.addView(preview);
        Button make=btn("📄 إنشاء ملف PDF جماعي");content.addView(make);
        Button share=btn("📤 مشاركة آخر PDF");content.addView(share);
        preview.setOnClickListener(v->showResultsPreview(type.getSelectedItemPosition(),grade.getSelectedItem().toString(),cls.getSelectedItem()==null?"":cls.getSelectedItem().toString()));
        make.setOnClickListener(v->generateClassPdf(type.getSelectedItemPosition(),grade.getSelectedItem().toString(),cls.getSelectedItem()==null?"":cls.getSelectedItem().toString()));
        share.setOnClickListener(v->shareLastPdf());
        addCard("ملاحظة","الحدود الافتراضية للنتيجة: 90 ممتاز، 80 جيد جداً، 70 جيد، 60 مقبول، أقل من 60 ضعيف. النجاح مضبوط افتراضياً من 50 ويمكن تغييره لاحقاً من الإعدادات.");
    }
    void showResultsPreview(int mode,String grade,String cls){
        page("👁 معاينة النتائج");
        ArrayList<String[]> ss=db.studentsInClass(grade,cls);
        if(ss.isEmpty()){addCard("لا توجد بيانات","لا يوجد طلاب في الصف والشعبة المحددين.");return;}
        for(String[] st:ss){
            String line=db.resultLine(st[1],mode);
            addCard("👨‍🎓 "+st[0],line);
        }
    }
    void pdfOptions(){resultsScreen();}
    String lastPdfPath="";
    void generateClassPdf(int mode,String grade,String cls){
        ArrayList<String[]> ss=db.studentsInClass(grade,cls);
        if(ss.isEmpty()){Toast.makeText(this,"لا يوجد طلاب في الصف المحدد",Toast.LENGTH_SHORT).show();return;}
        try{
            String title=mode==0?"نتائج_نصف_العام":mode==1?"نتائج_نهاية_العام":"النتائج_السنوية";
            File f=new File(getCacheDir(),title+"_الصف_"+grade+"_"+cls+".pdf");
            PdfDocument doc=new PdfDocument(); int pageNo=1;
            PdfDocument.Page page=doc.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNo++).create());
            Canvas c=page.getCanvas(); Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setTextSize(22);p.setTypeface(Typeface.DEFAULT_BOLD);
            drawRtl(c,"الجمهورية اليمنية",297,55,p);p.setTextSize(20);drawRtl(c,"مدرسة جعفر بن أبي طالب الأساسية",297,90,p);
            p.setTextSize(15);drawRtl(c,"إب - مذيخرة - الأشعوب",297,120,p);drawRtl(c,"كشف نتائج الطلاب",297,165,p);
            drawRtl(c,"الصف: "+grade+"   الشعبة: "+cls,297,195,p);
            p.setTypeface(Typeface.DEFAULT);p.setTextSize(12);
            int y=235; drawRtl(c,"م     اسم الطالب                         المجموع       النسبة       التقدير       النتيجة",560,y,p);y+=28;
            int rank=1;
            for(String[] st:ss){
                if(y>790){doc.finishPage(page);page=doc.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNo++).create());c=page.getCanvas();y=55;}
                String line=db.summaryPdfLine(st[1],st[0],mode,rank);
                drawRtl(c,line,560,y,p);y+=24;rank++;
            }
            doc.finishPage(page);
            int n=1;
            for(String[] st:ss){
                page=doc.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNo++).create());c=page.getCanvas();p.setTextSize(18);p.setTypeface(Typeface.DEFAULT_BOLD);
                drawRtl(c,"مدرسة جعفر بن أبي طالب الأساسية",297,50,p);p.setTextSize(15);drawRtl(c,"كشف نتيجة الطالب",297,80,p);
                p.setTypeface(Typeface.DEFAULT);p.setTextSize(12);
                drawRtl(c,"الطالب: "+st[0],560,115,p);drawRtl(c,"الصف: "+grade+"   الشعبة: "+cls,560,138,p);
                int yy=180;
                if(mode==0){
                    drawRtl(c,"المادة        المحصلة /20        نصف العام /30        المجموع /50",560,yy,p);
                }else if(mode==1){
                    drawRtl(c,"المادة        المحصلة /20        نهاية العام /30        المجموع /50",560,yy,p);
                }else{
                    drawRtl(c,"المادة        الفصل الأول /50        الفصل الثاني /50        السنوي /100",560,yy,p);
                }
                yy+=25;
                String[] subjects=db.subjects();for(String subject:subjects){
                    String ln=db.subjectPdfLine(st[1],subject,mode);drawRtl(c,ln,560,yy,p);yy+=22;
                }
                yy+=20;p.setTextSize(13);drawRtl(c,db.resultLine(st[1],mode),560,yy,p);
                yy+=55;drawRtl(c,"ملاحظات: ________________________________",560,yy,p);
                yy+=55;drawRtl(c,"معلم الصف: ________________    قائم بأعمال المدير: ________________",560,yy,p);
                yy+=35;drawRtl(c,"ختم المدرسة",560,yy,p);doc.finishPage(page);n++;
            }
            FileOutputStream out=new FileOutputStream(f);doc.writeTo(out);out.close();doc.close();lastPdfPath=f.getAbsolutePath();
            Toast.makeText(this,"تم إنشاء PDF: "+f.getName(),Toast.LENGTH_LONG).show();
            shareFile(f);
        }catch(Exception e){Toast.makeText(this,"تعذر إنشاء PDF: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }
    void generateStudentPdf(String id){
        String[] st=db.student(id);if(st==null)return;
        try{
            File f=new File(getCacheDir(),"ملف_الطالب_"+st[1]+".pdf");PdfDocument doc=new PdfDocument();
            PdfDocument.Page pg=doc.startPage(new PdfDocument.PageInfo.Builder(595,842,1).create());Canvas c=pg.getCanvas();Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(20);drawRtl(c,"الجمهورية اليمنية",560,50,p);drawRtl(c,"مدرسة جعفر بن أبي طالب الأساسية",560,82,p);
            p.setTypeface(Typeface.DEFAULT);p.setTextSize(13);drawRtl(c,"إب - مذيخرة - الأشعوب",560,108,p);
            p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(17);drawRtl(c,"الملف الدراسي للطالب",560,145,p);p.setTypeface(Typeface.DEFAULT);p.setTextSize(12);
            int y=180;drawRtl(c,"الاسم: "+st[0],560,y,p);y+=24;drawRtl(c,"الرقم: "+st[1]+"   الصف: "+st[2]+"   الشعبة: "+st[3],560,y,p);y+=24;drawRtl(c,"ولي الأمر: "+st[4]+"   الهاتف: "+st[5],560,y,p);y+=34;
            drawRtl(c,"النتيجة السنوية: "+db.resultLine(id,2),560,y,p);y+=34;
            drawRtl(c,"الحضور: "+db.studentAttendanceSummary(id),560,y,p);y+=45;
            drawRtl(c,"الدرجات الشهرية",560,y,p);y+=24;
            for(String line:db.studentMonthlyLines(id)){if(y>790){doc.finishPage(pg);pg=doc.startPage(new PdfDocument.PageInfo.Builder(595,842,doc.getPages().size()+1).create());c=pg.getCanvas();y=50;}drawRtl(c,line,560,y,p);y+=20;}
            y+=12;drawRtl(c,"ملاحظات: "+(db.studentNote(id).isEmpty()?"لا توجد":db.studentNote(id)),560,y,p);y+=45;drawRtl(c,"معلم الصف: __________________   قائم بأعمال المدير: __________________",560,y,p);y+=35;drawRtl(c,"ختم المدرسة",560,y,p);doc.finishPage(pg);
            FileOutputStream out=new FileOutputStream(f);doc.writeTo(out);out.close();doc.close();lastPdfPath=f.getAbsolutePath();Toast.makeText(this,"تم إنشاء ملف الطالب",Toast.LENGTH_LONG).show();shareFile(f);
        }catch(Exception e){Toast.makeText(this,"تعذر إنشاء ملف الطالب: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }

    void drawRtl(Canvas c,String text,float x,float y,Paint p){p.setTextAlign(Paint.Align.RIGHT);c.drawText(text,x,y,p);}
    void shareLastPdf(){if(lastPdfPath.isEmpty()){Toast.makeText(this,"أنشئ PDF أولاً",Toast.LENGTH_SHORT).show();return;}shareFile(new File(lastPdfPath));}
    void shareFile(File f){
        if(!f.exists())return;
        Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",f);
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,uri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"مشاركة ملف النتائج"));
    }

    void timetable(){page("📅 الجدول المدرسي");addCard("أيام الدوام","السبت، الأحد، الاثنين، الثلاثاء، الأربعاء\nالخميس والجمعة عطلة رسمية");if(admin()){Button edit=btn("✏️ تعديل الجدول");content.addView(edit);edit.setOnClickListener(v->scheduleEditor());}for(String d:DAYS)addCard("📅 "+d,db.daySchedule(d));}
    void scheduleEditor(){
    page("✏️ تعديل الجدول");
    for(String day:DAYS){
        final String d=day;
        for(int lesson=1;lesson<=5;lesson++){
            final int l=lesson;

            LinearLayout row=new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);

            row.addView(
                tv(d+" — الحصة "+l,15,true),
                new LinearLayout.LayoutParams(0,58,1)
            );

            EditText e=new EditText(this);
            e.setHint("المادة");
            e.setText(db.scheduleSubject(d,l));

            Button save=btn("حفظ");

            save.setOnClickListener(v->{
                db.setSchedule(d,l,e.getText().toString());
                Toast.makeText(
                    this,
                    "تم حفظ الحصة",
                    Toast.LENGTH_SHORT
                ).show();
            });

            row.addView(e,new LinearLayout.LayoutParams(0,55,1));
            row.addView(save,new LinearLayout.LayoutParams(72,52));
            content.addView(row);
        }
    }
}
    void reports(){page("📈 التقارير والإحصائيات");Button pr=btn("🖨️ طباعة تقرير المدرسة");content.addView(pr);pr.setOnClickListener(v->printReport());addCard("👨‍🎓 الطلاب","الإجمالي: "+db.count("students"));addCard("👨‍🏫 المعلمون","الإجمالي: "+db.count("teachers"));addCard("🏫 الصفوف والشعب","الإجمالي: "+db.count("classes"));addCard("✅ الحضور اليوم","حاضر: "+db.todayPresent()+"\nغائب: "+db.todayAbsent()+"\nمتأخر: "+db.todayLate()+"\nبعذر: "+db.todayExcused());addCard("📊 متوسط الدرجات",String.format(Locale.US,"%.1f / 100",db.averageGrades()));}
    void printReport(){
        WebView w=new WebView(this);
        String html="<html dir='rtl'><meta charset='utf-8'><body style='font-family:sans-serif;padding:24px'>"+
        "<h1>مدرسة جعفر بن أبي طالب الأساسية</h1><h2>تقرير الإدارة المدرسية</h2>"+
        "<p>الجمهورية اليمنية – إب – مذيخرة – الأشعوب</p>"+
        "<hr><h3>الإحصائيات</h3>"+
        "<p>الطلاب: "+db.count("students")+"</p><p>المعلمون: "+db.count("teachers")+"</p>"+
        "<p>الصفوف والشعب: "+db.count("classes")+"</p>"+
        "<p>حاضر اليوم: "+db.todayPresent()+" | غائب: "+db.todayAbsent()+" | متأخر: "+db.todayLate()+" | بعذر: "+db.todayExcused()+"</p>"+
        "<p>متوسط الدرجات: "+String.format(Locale.US,"%.1f",db.averageGrades())+" / 100</p>"+
        "<p>أيام الدوام: السبت إلى الأربعاء — الخميس والجمعة عطلة رسمية.</p></body></html>";
        w.loadDataWithBaseURL(null,html,"text/html","UTF-8",null);
        ((android.view.ViewGroup)root).addView(w,new LinearLayout.LayoutParams(1,1));
        w.setWebViewClient(new WebViewClient(){public void onPageFinished(WebView v,String u){PrintManager pm=(PrintManager)getSystemService(PRINT_SERVICE);pm.print("تقرير مدرسة جعفر",v.createPrintDocumentAdapter("تقرير مدرسة جعفر"),new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build());}});
    }

    void announcements(){page("🔔 الإعلانات والتنبيهات");if(admin()){Button add=btn("＋ إضافة إعلان");content.addView(add);add.setOnClickListener(v->{EditText e=new EditText(this);e.setHint("نص الإعلان");new AlertDialog.Builder(this).setTitle("إضافة إعلان").setView(e).setPositiveButton("نشر",(d,w)->{db.addAnnouncement(e.getText().toString());announcements();}).setNegativeButton("إلغاء",null).show();});}for(String[] r:db.announcements())addCard("📢 "+r[1],"التاريخ: "+r[0]);}
    void users(){page("🔐 المستخدمون والصلاحيات");if(!admin()){addCard("صلاحيات محدودة","هذه الصفحة متاحة للقائم بأعمال المدير فقط.");return;}addCard("حسابك","القائم بأعمال المدير — صلاحيات الإدارة الكاملة.");if(db.users().isEmpty())addCard("لا يوجد مستخدمون إضافيون","أضف حساباً جديداً لتحديد صلاحيات مستقلة لكل مستخدم.");Button add=btn("＋ إضافة مستخدم");content.addView(add);add.setOnClickListener(v->userDialog());for(String[] u:db.users())addCard("👤 "+u[0],"الدور: "+u[1]+"\nالصلاحيات: "+u[2]);}
    void userDialog(){
        final EditText n=new EditText(this);n.setHint("اسم المستخدم / الاسم الظاهر");
        final EditText pw=new EditText(this);pw.setHint("كلمة المرور");pw.setInputType(0x81);
        final Spinner ro=new Spinner(this);ro.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"معلم","طالب","ولي أمر","قائم بأعمال المدير"}));
        final EditText g=new EditText(this);g.setHint("الصف المكلف به (اختياري)");
        final EditText c=new EditText(this);c.setHint("الشعبة (اختياري)");
        final EditText childIds=new EditText(this);childIds.setHint("أرقام الطلاب المرتبطين (لولي الأمر: افصل بينها بفاصلة)");
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.addView(n);l.addView(pw);l.addView(ro);l.addView(g);l.addView(c);l.addView(childIds);
        new AlertDialog.Builder(this).setTitle("إضافة مستخدم").setView(l).setPositiveButton("حفظ",(d,w)->{String rr=ro.getSelectedItem().toString();String perms=rr.equals("قائم بأعمال المدير")?"إدارة كاملة":rr.equals("معلم")?"الصفوف المكلف بها + الدرجات + الحضور":"البيانات المسموح بها";String requested=n.getText().toString().trim(); String username=db.addUser(requested,pw.getText().toString(),rr,perms,g.getText().toString(),c.getText().toString()); db.setUserStudents(username,childIds.getText().toString()); users();}).setNegativeButton("إلغاء",null).show();
    }
    void files(){
        page("📁 الملفات والنسخ الاحتياطي");
        addCard("التعاميم","قسم الملفات مهيأ لإضافة مستندات المدرسة.");
        addCard("💾 النسخ الاحتياطي","احفظ نسخة من بيانات المدرسة على الهاتف أو بطاقة الذاكرة أو أي مكان تختاره. لا يحتاج ذلك إلى إنترنت.");
        Button backup=btn("💾 إنشاء نسخة احتياطية"); content.addView(backup);
        backup.setOnClickListener(v->createBackup());
        Button restore=btn("♻️ استعادة نسخة احتياطية"); content.addView(restore);
        restore.setOnClickListener(v->chooseRestore());
        addCard("⚠️ تنبيه","الاستعادة تستبدل بيانات التطبيق الحالية ببيانات النسخة المختارة. خذ نسخة احتياطية من البيانات الحالية قبل الاستعادة.");
    }
    void createBackup(){
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("application/octet-stream");
        i.putExtra(Intent.EXTRA_TITLE,"JafarSchool_Backup_"+date()+".db");
        startActivityForResult(i,REQ_BACKUP);
    }
    void chooseRestore(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("application/octet-stream");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i,REQ_RESTORE);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode!=RESULT_OK||data==null||data.getData()==null)return;
        try{
            if(requestCode==REQ_BACKUP){
                db.close();
                copyFile(getDatabasePath("jafar_school.db"),getContentResolver().openOutputStream(data.getData()));
                db=new DB(this);
                Toast.makeText(this,"تم إنشاء النسخة الاحتياطية بنجاح",Toast.LENGTH_LONG).show();
            }else if(requestCode==REQ_RESTORE){
                new AlertDialog.Builder(this).setTitle("تأكيد الاستعادة").setMessage("سيتم استبدال بيانات التطبيق الحالية ببيانات النسخة الاحتياطية. هل تريد المتابعة؟").setPositiveButton("استعادة",(d,w)->restoreFromUri(data.getData())).setNegativeButton("إلغاء",null).show();
            }
        }catch(Exception e){try{db=new DB(this);}catch(Exception ignored){} Toast.makeText(this,"تعذر تنفيذ العملية: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }
    void restoreFromUri(Uri uri){
        File target=getDatabasePath("jafar_school.db");
        File temp=new File(getCacheDir(),"jafar_restore.tmp");
        try{
            db.close();
            InputStream in=getContentResolver().openInputStream(uri);
            if(in==null)throw new Exception("تعذر قراءة ملف النسخة الاحتياطية");
            copyFile(in,new FileOutputStream(temp));
            in.close();
            if(!temp.exists()||temp.length()<100)throw new Exception("ملف النسخة الاحتياطية غير صالح");
            File backupCurrent=new File(getCacheDir(),"jafar_current_before_restore.db");
            if(target.exists())copyFile(target,backupCurrent);
            if(target.exists()&&!target.delete())throw new Exception("تعذر استبدال قاعدة البيانات");
            if(!temp.renameTo(target)){copyFile(temp,target);temp.delete();}
            db=new DB(this);
            Toast.makeText(this,"تمت استعادة البيانات بنجاح. أعد فتح التطبيق للتأكد من جميع البيانات.",Toast.LENGTH_LONG).show();
            showLogin();
}catch(Exception e){
    try{db=new DB(this);}catch(Exception ignored){}
    Toast.makeText(this,"فشلت الاستعادة: "+e.getMessage(),Toast.LENGTH_LONG).show();
}
    }
    void copyFile(File source,File target)throws Exception{
    InputStream in=new FileInputStream(source);
    OutputStream out=new FileOutputStream(target);
    try{
        byte[] buf=new byte[8192];
        int n;
        while((n=in.read(buf))!=-1)out.write(buf,0,n);
        out.flush();
    }finally{
        try{in.close();}catch(Exception ignored){}
        try{out.close();}catch(Exception ignored){}
    }
}

void copyFile(File source,OutputStream out)throws Exception{
    InputStream in=new FileInputStream(source);
    copyFile(in,out);
}

void copyFile(InputStream in,OutputStream out)throws Exception{
    try{
        byte[] buf=new byte[8192];
        int n;
        while((n=in.read(buf))!=-1)out.write(buf,0,n);
        out.flush();
    }finally{
        try{in.close();}catch(Exception ignored){}
        try{out.close();}catch(Exception ignored){}
    }
}
    void settings(){page("⚙️ الإعدادات");addCard("👤 حسابي","الاسم: "+currentUser+"\nاسم المستخدم: "+currentUsername+"\nالدور: "+role);Button cp=btn("🔑 تغيير كلمة المرور");content.addView(cp);cp.setOnClickListener(v->changePassword());if(admin()){addCard("🔐 الصلاحيات","صلاحيات القائم بأعمال المدير مستقلة ولا تُمنح لأي مدرس آخر تلقائياً.");}else{addCard("🔐 صلاحياتي","يمكنك الوصول فقط إلى المهام المسموحة لدورك والصف المكلف به.");}addCard("📅 التقويم المدرسي","الدوام: السبت إلى الأربعاء\nالعطلة الرسمية: الخميس والجمعة");
        addCard("📴 التشغيل دون إنترنت","لا يحتاج التطبيق إلى اتصال بالإنترنت للوظائف الأساسية. سيتم استخدام الاتصال فقط مستقبلاً إذا أضيفت مزامنة أو تحديثات اختيارية.");addCard("🏫 بيانات المدرسة","مدرسة جعفر بن أبي طالب الأساسية\nالجمهورية اليمنية – إب – مذيخرة – الأشعوب");}
    void changePassword(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);EditText old=new EditText(this);old.setHint("كلمة المرور الحالية");old.setInputType(0x81);EditText nw=new EditText(this);nw.setHint("كلمة المرور الجديدة");nw.setInputType(0x81);EditText confirm=new EditText(this);confirm.setHint("تأكيد كلمة المرور الجديدة");confirm.setInputType(0x81);l.addView(old);l.addView(nw);l.addView(confirm);new AlertDialog.Builder(this).setTitle("تغيير كلمة المرور").setView(l).setPositiveButton("حفظ",(d,w)->{if(nw.getText().length()<4||!nw.getText().toString().equals(confirm.getText().toString())){Toast.makeText(this,"تأكد من كلمة المرور الجديدة (4 أحرف على الأقل)",Toast.LENGTH_SHORT).show();return;}if(!db.changePassword(currentUsername,old.getText().toString(),nw.getText().toString()))Toast.makeText(this,"كلمة المرور الحالية غير صحيحة",Toast.LENGTH_SHORT).show();else Toast.makeText(this,"تم تغيير كلمة المرور بنجاح",Toast.LENGTH_SHORT).show();}).setNegativeButton("إلغاء",null).show();}
    String date(){return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());}
}

class DB extends android.database.sqlite.SQLiteOpenHelper {
    DB(Context c){super(c,"jafar_school.db",null,7);}
    public void onCreate(android.database.sqlite.SQLiteDatabase d){
        d.execSQL("CREATE TABLE students(name TEXT,id TEXT PRIMARY KEY,grade TEXT,classroom TEXT,parent TEXT,phone TEXT)");
        d.execSQL("CREATE TABLE teachers(name TEXT,subject TEXT,role TEXT)");
        d.execSQL("CREATE TABLE classes(grade TEXT,classroom TEXT,PRIMARY KEY(grade,classroom))");
        d.execSQL("CREATE TABLE attendance(student_id TEXT,date TEXT,status TEXT,PRIMARY KEY(student_id,date))");
        d.execSQL("CREATE TABLE grades(student_id TEXT,subject TEXT,score REAL,PRIMARY KEY(student_id,subject))");
        d.execSQL("CREATE TABLE monthly_scores(student_id TEXT,subject TEXT,semester INTEGER,month INTEGER,regular REAL,oral REAL,homework REAL,written REAL,PRIMARY KEY(student_id,subject,semester,month))");
        d.execSQL("CREATE TABLE exams(student_id TEXT,subject TEXT,semester INTEGER,score REAL,PRIMARY KEY(student_id,subject,semester))");
        d.execSQL("CREATE TABLE announcements(date TEXT,text TEXT)");
        d.execSQL("CREATE TABLE timetable(day TEXT,lesson INTEGER,subject TEXT,PRIMARY KEY(day,lesson))");
        d.execSQL("CREATE TABLE users(name TEXT,role TEXT,permissions TEXT,username TEXT,password_hash TEXT,grade TEXT,classroom TEXT)");
        d.execSQL("CREATE TABLE student_notes(student_id TEXT PRIMARY KEY,note TEXT)");
        d.execSQL("CREATE TABLE user_students(username TEXT,student_id TEXT,PRIMARY KEY(username,student_id))");
        seed(d);
    }
    void seed(android.database.sqlite.SQLiteDatabase d){
        d.execSQL("INSERT INTO students VALUES('أحمد محمد علي','1001','الأول','أ','محمد علي','777123456')");
        d.execSQL("INSERT INTO students VALUES('سارة عبدالله أحمد','1002','الأول','أ','عبدالله أحمد','777222333')");
        d.execSQL("INSERT INTO students VALUES('محمد قاسم علي','1003','الثاني','ب','قاسم علي','777333444')");
        d.execSQL("INSERT INTO teachers VALUES('أ. محمد عبدالله','اللغة العربية','معلم')");
        d.execSQL("INSERT INTO teachers VALUES('أ. فاطمة علي','الرياضيات','معلم')");
        d.execSQL("INSERT INTO teachers VALUES('أ. هشام محمد','إدارة المدرسة','قائم بأعمال المدير')");
        for(String g:new String[]{"الأول","الثاني"})for(String c:new String[]{"أ","ب"})d.execSQL("INSERT OR IGNORE INTO classes VALUES(?,?)",new Object[]{g,c});
        for(String day:new String[]{"السبت","الأحد","الاثنين","الثلاثاء","الأربعاء"})for(int i=1;i<=5;i++)d.execSQL("INSERT INTO timetable VALUES(?,?,?)",new Object[]{day,i,"—"});
        d.execSQL("INSERT INTO users(name,role,permissions,username,password_hash,grade,classroom) VALUES(?,?,?,?,?,?,?)",new Object[]{"القائم بأعمال المدير","القائم بأعمال المدير","إدارة كاملة","admin",sha256("1234"),"",""});
        d.execSQL("INSERT INTO users(name,role,permissions,username,password_hash,grade,classroom) VALUES(?,?,?,?,?,?,?)",new Object[]{"أ. محمد عبدالله","معلم","درجات وحضور الصف","teacher1",sha256("1234"),"الأول","أ"});
        d.execSQL("INSERT INTO users(name,role,permissions,username,password_hash,grade,classroom) VALUES(?,?,?,?,?,?,?)",new Object[]{"ولي أمر أحمد","ولي أمر","عرض الأبناء","parent1001",sha256("1234"),"",""});
        d.execSQL("INSERT INTO users(name,role,permissions,username,password_hash,grade,classroom) VALUES(?,?,?,?,?,?,?)",new Object[]{"أحمد محمد علي","طالب","عرض بيانات الطالب","student1001",sha256("1234"),"الأول","أ"});
        d.execSQL("INSERT INTO user_students(username,student_id) VALUES(?,?)",new Object[]{"parent1001","1001"});
        d.execSQL("INSERT INTO user_students(username,student_id) VALUES(?,?)",new Object[]{"student1001","1001"});
        d.execSQL("INSERT INTO announcements VALUES(?,?)",new Object[]{new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()),"مرحباً بكم في نظام مدرسة جعفر المدرسي."} );
    }
    public void onUpgrade(android.database.sqlite.SQLiteDatabase d,int oldV,int newV){
        if(oldV<7){
            d.execSQL("CREATE TABLE IF NOT EXISTS student_notes(student_id TEXT PRIMARY KEY,note TEXT)");
            d.execSQL("CREATE TABLE IF NOT EXISTS user_students(username TEXT,student_id TEXT,PRIMARY KEY(username,student_id))");
        }
        if(oldV<5){
            d.execSQL("CREATE TABLE IF NOT EXISTS monthly_scores(student_id TEXT,subject TEXT,semester INTEGER,month INTEGER,regular REAL,oral REAL,homework REAL,written REAL,PRIMARY KEY(student_id,subject,semester,month))");
            d.execSQL("CREATE TABLE IF NOT EXISTS exams(student_id TEXT,subject TEXT,semester INTEGER,score REAL,PRIMARY KEY(student_id,subject,semester))");
        }
        if(oldV<4){
            try{d.execSQL("ALTER TABLE users ADD COLUMN username TEXT");}catch(Exception ignored){}
            try{d.execSQL("ALTER TABLE users ADD COLUMN password_hash TEXT");}catch(Exception ignored){}
            try{d.execSQL("ALTER TABLE users ADD COLUMN grade TEXT");}catch(Exception ignored){}
            try{d.execSQL("ALTER TABLE users ADD COLUMN classroom TEXT");}catch(Exception ignored){}
            d.execSQL("UPDATE users SET username=name WHERE username IS NULL");
            d.execSQL("UPDATE users SET password_hash=? WHERE password_hash IS NULL",new Object[]{sha256("1234")});
            android.database.Cursor c=d.rawQuery("SELECT COUNT(*) FROM users WHERE username=?",new String[]{"admin"});c.moveToFirst();int n=c.getInt(0);c.close();if(n==0)d.execSQL("INSERT INTO users(name,role,permissions,username,password_hash,grade,classroom) VALUES(?,?,?,?,?,?,?)",new Object[]{"القائم بأعمال المدير","قائم بأعمال المدير","إدارة كاملة","admin",sha256("1234"),"الأول","أ"});
        }
    }
    String[] authenticate(String username,String password){
        android.database.Cursor c=getReadableDatabase().rawQuery("SELECT name,role,grade,classroom,username FROM users WHERE username=? AND password_hash=? LIMIT 1",new String[]{username,sha256(password)});
        if(!c.moveToFirst()){c.close();return null;}String[] r={c.getString(0),c.getString(1),c.getString(2)==null?"":c.getString(2),c.getString(3)==null?"":c.getString(3),c.getString(4)};c.close();return r;
    }
    ArrayList<String[]> studentsInClass(String g,String cl){ArrayList<String[]> a=new ArrayList<>();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT name,id FROM students WHERE grade=? AND classroom=? ORDER BY name",new String[]{g,cl});while(c.moveToNext())a.add(new String[]{c.getString(0),c.getString(1)});c.close();return a;}
    String addUser(String name,String password,String role,String permissions,String grade,String classroom){String base=name.trim().replaceAll("\\s+",".");if(base.isEmpty())base="user"+System.currentTimeMillis();String username=base;int i=1;while(userExists(username)){username=base+i;i++;}getWritableDatabase().execSQL("INSERT INTO users(name,role,permissions,username,password_hash,grade,classroom) VALUES(?,?,?,?,?,?,?)",new Object[]{name,role,permissions,username,sha256(password),grade,classroom});return username;}
    boolean changePassword(String username,String oldPassword,String newPassword){android.database.sqlite.SQLiteDatabase d=getWritableDatabase();android.database.Cursor c=d.rawQuery("SELECT 1 FROM users WHERE username=? AND password_hash=?",new String[]{username,sha256(oldPassword)});boolean ok=c.moveToFirst();c.close();if(ok)d.execSQL("UPDATE users SET password_hash=? WHERE username=?",new Object[]{sha256(newPassword),username});return ok;}
    boolean userExists(String u){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT 1 FROM users WHERE username=?",new String[]{u});boolean x=c.moveToFirst();c.close();return x;}
    static String sha256(String x){try{MessageDigest md=MessageDigest.getInstance("SHA-256");byte[] b=md.digest(x.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte v:b)s.append(String.format(Locale.US,"%02x",v));return s.toString();}catch(Exception e){return x;}}
    int count(String t){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM "+t,null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}
    void saveStudent(String n,String id,String g,String cl,String p,String ph){getWritableDatabase().execSQL("INSERT OR REPLACE INTO students(name,id,grade,classroom,parent,phone) VALUES(?,?,?,?,?,?)",new Object[]{n,id,g,cl,p,ph});}
    ArrayList<String[]> students(String q){ArrayList<String[]> a=new ArrayList<>();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT name,id,grade,classroom FROM students WHERE name LIKE ? OR id LIKE ? ORDER BY name",new String[]{"%"+q+"%","%"+q+"%"});while(c.moveToNext())a.add(new String[]{c.getString(0),c.getString(1),c.getString(2),c.getString(3)});c.close();return a;}
    String[] student(String id){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT name,id,grade,classroom,parent,phone FROM students WHERE id=?",new String[]{id});if(!c.moveToFirst()){c.close();return null;}String[] r={c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5)};c.close();return r;}
    void addTeacher(String n,String s,String r){getWritableDatabase().execSQL("INSERT INTO teachers VALUES(?,?,?)",new Object[]{n,s,r});}
    ArrayList<String[]> teachers(){ArrayList<String[]> a=new ArrayList<>();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT name,subject,role FROM teachers ORDER BY name",null);while(c.moveToNext())a.add(new String[]{c.getString(0),c.getString(1),c.getString(2)});c.close();return a;}
    void addClass(String g,String c){getWritableDatabase().execSQL("INSERT OR IGNORE INTO classes VALUES(?,?)",new Object[]{g,c});}
    ArrayList<String[]> classes(){ArrayList<String[]> a=new ArrayList<>();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT c.grade,c.classroom,(SELECT COUNT(*) FROM students s WHERE s.grade=c.grade AND s.classroom=c.classroom) FROM classes c ORDER BY c.grade,c.classroom",null);while(c.moveToNext())a.add(new String[]{c.getString(0),c.getString(1),c.getString(2)});c.close();return a;}
    String attendanceStatus(String id){String date=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());android.database.Cursor c=getReadableDatabase().rawQuery("SELECT status FROM attendance WHERE student_id=? AND date=?",new String[]{id,date});String s="";if(c.moveToFirst())s=c.getString(0);c.close();return s;}
    void setAttendance(String id,String st){String date=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());getWritableDatabase().execSQL("INSERT OR REPLACE INTO attendance VALUES(?,?,?)",new Object[]{id,date,st});}
    int todayPresent(){return todayCount("حاضر");}int todayAbsent(){return todayCount("غائب");}int todayLate(){return todayCount("متأخر");}int todayExcused(){return todayCount("بعذر");}
    int todayCount(String st){String date=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());android.database.Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM attendance WHERE date=? AND status=?",new String[]{date,st});c.moveToFirst();int n=c.getInt(0);c.close();return n;}
    void setGrade(String id,String sub,double score){getWritableDatabase().execSQL("INSERT OR REPLACE INTO grades VALUES(?,?,?)",new Object[]{id,sub,score});}
    String grade(String id,String sub){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT score FROM grades WHERE student_id=? AND subject=?",new String[]{id,sub});String s="";if(c.moveToFirst())s=String.valueOf(c.getDouble(0));c.close();return s;}
    String studentGrades(String id){StringBuilder b=new StringBuilder();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT subject,score FROM grades WHERE student_id=? ORDER BY subject",new String[]{id});if(!c.moveToFirst())return "لا توجد درجات مسجلة بعد.";do{b.append(c.getString(0)).append(": ").append(c.getDouble(1)).append("\n");}while(c.moveToNext());c.close();return b.toString().trim();}
    String studentAttendance(String id){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT date,status FROM attendance WHERE student_id=? ORDER BY date DESC LIMIT 20",new String[]{id});if(!c.moveToFirst())return "لا توجد سجلات حضور بعد.";StringBuilder b=new StringBuilder();do{b.append(c.getString(0)).append(" — ").append(c.getString(1)).append("\n");}while(c.moveToNext());c.close();return b.toString().trim();}
    String studentMonthlyDetails(String id){StringBuilder b=new StringBuilder();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT subject,semester,month,regular,oral,homework,written FROM monthly_scores WHERE student_id=? ORDER BY semester,month,subject",new String[]{id});if(!c.moveToFirst())return "لا توجد درجات شهرية مسجلة.";do{double total=c.getDouble(3)+c.getDouble(4)+c.getDouble(5)+c.getDouble(6);b.append(c.getString(0)).append(" — الفصل ").append(c.getInt(1)).append(" — الشهر ").append(c.getInt(2)).append(": ").append(String.format(Locale.US,"%.1f/100",total)).append("\n");}while(c.moveToNext());c.close();return b.toString().trim();}
    ArrayList<String> studentMonthlyLines(String id){ArrayList<String>a=new ArrayList<>();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT subject,semester,month,regular,oral,homework,written FROM monthly_scores WHERE student_id=? ORDER BY semester,month,subject",new String[]{id});while(c.moveToNext()){double total=c.getDouble(3)+c.getDouble(4)+c.getDouble(5)+c.getDouble(6);a.add(String.format(Locale.US,"%s | ف%d/ش%d | %.1f/100 (مواظبة %.1f، شفوي %.1f، واجبات %.1f، تحريري %.1f)",c.getString(0),c.getInt(1),c.getInt(2),total,c.getDouble(3),c.getDouble(4),c.getDouble(5),c.getDouble(6)));}c.close();if(a.isEmpty())a.add("لا توجد درجات شهرية مسجلة.");return a;}
    String studentExams(String id){StringBuilder b=new StringBuilder();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT subject,semester,score FROM exams WHERE student_id=? ORDER BY semester,subject",new String[]{id});if(!c.moveToFirst())return "لا توجد اختبارات مسجلة.";do{b.append(c.getString(0)).append(" — الفصل ").append(c.getInt(1)).append(": ").append(c.getDouble(2)).append(" / 30\n");}while(c.moveToNext());c.close();return b.toString().trim();}
    String studentAttendanceSummary(String id){int p=attendanceCount(id,"حاضر"),a=attendanceCount(id,"غائب"),l=attendanceCount(id,"متأخر"),e=attendanceCount(id,"بعذر");return "حاضر: "+p+" | غائب: "+a+" | متأخر: "+l+" | بعذر: "+e;}
    int attendanceCount(String id,String status){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM attendance WHERE student_id=? AND status=?",new String[]{id,status});c.moveToFirst();int n=c.getInt(0);c.close();return n;}
    String studentNote(String id){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT note FROM student_notes WHERE student_id=?",new String[]{id});String s="";if(c.moveToFirst())s=c.getString(0);c.close();return s==null?"":s;}
    void setStudentNote(String id,String note){getWritableDatabase().execSQL("INSERT OR REPLACE INTO student_notes(student_id,note) VALUES(?,?)",new Object[]{id,note});}
    double averageGrades(){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT AVG(score) FROM grades",null);c.moveToFirst();double n=c.isNull(0)?0:c.getDouble(0);c.close();return n;}
    void setSchedule(String day,int lesson,String subject){getWritableDatabase().execSQL("INSERT OR REPLACE INTO timetable VALUES(?,?,?)",new Object[]{day,lesson,subject});}
    String scheduleSubject(String day,int lesson){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT subject FROM timetable WHERE day=? AND lesson=?",new String[]{day,String.valueOf(lesson)});String s="";if(c.moveToFirst())s=c.getString(0);c.close();return s;}
    String daySchedule(String day){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT lesson,subject FROM timetable WHERE day=? ORDER BY lesson",new String[]{day});StringBuilder b=new StringBuilder();while(c.moveToNext())b.append("الحصة ").append(c.getInt(0)).append(": ").append(c.getString(1)).append("\n");c.close();return b.length()==0?"لا يوجد جدول مسجل.":b.toString().trim();}
    void addAnnouncement(String text){getWritableDatabase().execSQL("INSERT INTO announcements VALUES(?,?)",new Object[]{new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()),text});}
    ArrayList<String[]> announcements(){ArrayList<String[]> a=new ArrayList<>();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT date,text FROM announcements ORDER BY date DESC",null);while(c.moveToNext())a.add(new String[]{c.getString(0),c.getString(1)});c.close();return a;}
    String latestAnnouncements(){ArrayList<String[]> a=announcements();return a.isEmpty()?"لا توجد إعلانات جديدة حالياً":a.get(0)[1]+"\n"+a.get(0)[0];}
    ArrayList<String[]> users(){ArrayList<String[]> a=new ArrayList<>();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT name,role,permissions FROM users ORDER BY name",null);while(c.moveToNext())a.add(new String[]{c.getString(0),c.getString(1),c.getString(2)});c.close();return a;}
    void setUserStudents(String username,String csv){
        android.database.sqlite.SQLiteDatabase w=getWritableDatabase(); w.delete("user_students","username=?",new String[]{username});
        if(csv==null)return; for(String raw:csv.split(",")){String id=raw.trim(); if(id.isEmpty())continue; android.database.Cursor c=getReadableDatabase().rawQuery("SELECT 1 FROM students WHERE id=?",new String[]{id}); boolean ok=c.moveToFirst(); c.close(); if(ok)w.execSQL("INSERT OR REPLACE INTO user_students(username,student_id) VALUES(?,?)",new Object[]{username,id});}
    }
    ArrayList<String[]> linkedStudents(String username){ArrayList<String[]> a=new ArrayList<>(); android.database.Cursor c=getReadableDatabase().rawQuery("SELECT s.name,s.id,s.grade,s.classroom FROM students s INNER JOIN user_students u ON u.student_id=s.id WHERE u.username=? ORDER BY s.name",new String[]{username}); while(c.moveToNext())a.add(new String[]{c.getString(0),c.getString(1),c.getString(2),c.getString(3)}); c.close(); return a;}

    void setMonthlyScore(String id,String subject,int semester,int month,double regular,double oral,double homework,double written){
        getWritableDatabase().execSQL("INSERT OR REPLACE INTO monthly_scores(student_id,subject,semester,month,regular,oral,homework,written) VALUES(?,?,?,?,?,?,?,?)",
                new Object[]{id,subject,semester,month,regular,oral,homework,written});
    }
    void setExam(String id,String subject,int semester,double score){
        getWritableDatabase().execSQL("INSERT OR REPLACE INTO exams(student_id,subject,semester,score) VALUES(?,?,?,?)",new Object[]{id,subject,semester,score});
    }
    double[] monthlyValues(String id,String subject,int semester,int month){
        android.database.Cursor c=getReadableDatabase().rawQuery("SELECT regular,oral,homework,written FROM monthly_scores WHERE student_id=? AND subject=? AND semester=? AND month=?",new String[]{id,subject,String.valueOf(semester),String.valueOf(month)});
        if(!c.moveToFirst()){c.close();return null;} double[] v={c.getDouble(0),c.getDouble(1),c.getDouble(2),c.getDouble(3)};c.close();return v;
    }
    double monthlyTotal(String id,String subject,int semester){
        android.database.Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(regular+oral+homework+written),0) FROM monthly_scores WHERE student_id=? AND subject=? AND semester=?",
                new String[]{id,subject,String.valueOf(semester)});c.moveToFirst();double x=c.getDouble(0);c.close();return x;
    }
    double semesterMark20(String id,String subject,int semester){return monthlyTotal(id,subject,semester)/15.0;}
    double exam(String id,String subject,int semester){
        android.database.Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(score,0) FROM exams WHERE student_id=? AND subject=? AND semester=?",
                new String[]{id,subject,String.valueOf(semester)});c.moveToFirst();double x=c.getDouble(0);c.close();return x;
    }
    double subjectAnnual(String id,String subject){return semesterMark20(id,subject,1)+exam(id,subject,1)+semesterMark20(id,subject,2)+exam(id,subject,2);}
    String[] subjects(){return new String[]{"اللغة العربية","الرياضيات","القرآن الكريم","العلوم","التربية الإسلامية","الاجتماعيات"};}
    ArrayList<String> gradesList(){ArrayList<String> a=new ArrayList<>();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT DISTINCT grade FROM students ORDER BY grade",null);while(c.moveToNext())a.add(c.getString(0));c.close();if(a.isEmpty())a.add("الأول");return a;}
    ArrayList<String> classesForGrade(String g){ArrayList<String>a=new ArrayList<>();android.database.Cursor c=getReadableDatabase().rawQuery("SELECT DISTINCT classroom FROM students WHERE grade=? ORDER BY classroom",new String[]{g});while(c.moveToNext())a.add(c.getString(0));c.close();if(a.isEmpty())a.add("أ");return a;}
    double studentResult(String id,int mode){
        double total=0;for(String sub:subjects()){if(mode==0)total+=semesterMark20(id,sub,1)+exam(id,sub,1);else if(mode==1)total+=semesterMark20(id,sub,2)+exam(id,sub,2);else total+=subjectAnnual(id,sub);}
        return total;
    }
    String gradeLabel(double pct){if(pct>=90)return"ممتاز";if(pct>=80)return"جيد جداً";if(pct>=70)return"جيد";if(pct>=60)return"مقبول";return"ضعيف";}
    String resultLabel(double pct){return pct>=50?"ناجح":"راسب";}
    String resultLine(String id,int mode){
        double max=subjects().length*(mode==2?100:50);double total=studentResult(id,mode);double pct=max==0?0:total*100/max;
        return String.format(Locale.US,"المجموع: %.1f / %.0f   النسبة: %.1f%%   التقدير: %s   النتيجة: %s",total,max,pct,gradeLabel(pct),resultLabel(pct));
    }
    String summaryPdfLine(String id,String name,int mode,int rank){
        double max=subjects().length*(mode==2?100:50),total=studentResult(id,mode),pct=max==0?0:total*100/max;
        String rk=rank<=3?String.valueOf(rank):"—";
        return String.format(Locale.US,"%d   %s   %.1f / %.0f   %.1f%%   %s   %s   %s",rank,name,total,max,pct,gradeLabel(pct),resultLabel(pct),rk);
    }
    String subjectPdfLine(String id,String subject,int mode){
        if(mode==0)return String.format(Locale.US,"%s   %.1f   %.1f   %.1f",subject,semesterMark20(id,subject,1),exam(id,subject,1),semesterMark20(id,subject,1)+exam(id,subject,1));
        if(mode==1)return String.format(Locale.US,"%s   %.1f   %.1f   %.1f",subject,semesterMark20(id,subject,2),exam(id,subject,2),semesterMark20(id,subject,2)+exam(id,subject,2));
        return String.format(Locale.US,"%s   %.1f   %.1f   %.1f",subject,semesterMark20(id,subject,1)+exam(id,subject,1),semesterMark20(id,subject,2)+exam(id,subject,2),subjectAnnual(id,subject));
    }



// V1.7.0 GROUP GRADE ENTRY
// شاشة إدخال جماعي للدرجات: المادة ← الشهر/الفترة ← قائمة الطلاب.
// الدوال التالية تمثل نقطة التكامل للواجهة الجماعية في النسخة التالية.
private void openGroupGradeEntry() {
    // يتم ربطها من قائمة النتائج/المعلم لإظهار جميع طلاب الصف والشعبة.
    // الحفظ محليًا، دون الحاجة للإنترنت.
}
private String gradeEntryPeriodName(int period) {
    switch (period) {
        case 1: return "الشهر الأول";
        case 2: return "الشهر الثاني";
        case 3: return "الشهر الثالث";
        case 4: return "المحصلة الأولى";
        case 5: return "اختبار نصف العام";
        case 6: return "المحصلة الثانية";
        case 7: return "الاختبار النهائي";
        default: return "الفترة";
    }
}

}
