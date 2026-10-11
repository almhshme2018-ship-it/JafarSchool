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
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root, content;
    DB db;
    String role = "القائم بأعمال المدير";
    String currentUser = "", currentUsername = "", assignedGrade = "", assignedClass = "";

    final int BLUE = Color.rgb(17, 96, 177);
    final int BG = Color.rgb(244, 247, 251);
    final int WHITE = Color.WHITE;
    final int TEXT = Color.rgb(25, 45, 68);
    final int GREEN = Color.rgb(30, 130, 76);
    final int RED = Color.rgb(190, 50, 50);
    final int ORANGE = Color.rgb(220, 130, 30);
    final int GRAY = Color.rgb(100, 105, 112);

    // المواد 7 بالترتيب المتفق عليه + السلوك
    final String[] SUBJECTS_7 = {
        "القرآن الكريم",
        "التربية الإسلامية",
        "اللغة العربية",
        "الرياضيات",
        "العلوم",
        "الاجتماعيات",
        "اللغة الإنجليزية"
    };
    final String BEHAVIOR = "السلوك";

    final Stack<Runnable> navHistory = new Stack<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BLUE);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        db = new DB(this);
        showLogin();
    }

    @Override public void onBackPressed() {
        if (navHistory.size() > 1) { navHistory.pop(); navHistory.peek().run(); return; }
        if (navHistory.size() == 1) { Toast.makeText(this,"أنت في الرئيسية",Toast.LENGTH_SHORT).show(); return; }
        super.onBackPressed();
    }
    void goBack(){ onBackPressed(); }

    // ============ Responsive ============
    int dp(int v){ return (int)(v * getResources().getDisplayMetrics().density); }
    android.graphics.drawable.Drawable background(int color){
        GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(10)); return g;
    }
    TextView tv(String s,int sp,boolean bold){
        TextView t=new TextView(this); t.setText(s==null?"":s); t.setTextSize(sp); t.setTextColor(TEXT);
        t.setTypeface(null, bold?Typeface.BOLD:Typeface.NORMAL);
        t.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
        t.setPadding(dp(14),dp(10),dp(14),dp(10)); t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return t;
    }
    Button btn(String s){
        Button b=new Button(this); b.setText(s); b.setTextSize(14); b.setTextColor(WHITE); b.setAllCaps(false);
        b.setGravity(Gravity.CENTER); b.setSingleLine(false); b.setMaxLines(3); b.setPadding(dp(12),dp(8),dp(12),dp(8));
        GradientDrawable g=new GradientDrawable(); g.setColor(BLUE); g.setCornerRadius(dp(14)); b.setBackground(g); return b;
    }
    Button smallBtn(String text,int color){
        Button b=new Button(this); b.setText(text); b.setTextSize(12); b.setTextColor(WHITE); b.setAllCaps(false);
        b.setSingleLine(false); b.setGravity(Gravity.CENTER); b.setPadding(dp(4),dp(2),dp(4),dp(2));
        GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(10)); b.setBackground(g); return b;
    }
    void base(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); setContentView(root);
    }
    void navigateTo(Runnable s){ if(s==null) return; navHistory.push(s); s.run(); }
    void page(String name){
        base();
        LinearLayout top=new LinearLayout(this); top.setOrientation(LinearLayout.HORIZONTAL); top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8),dp(6),dp(8),dp(6)); top.setBackgroundColor(WHITE); top.setElevation(dp(4));
        Button back=smallBtn("‹ رجوع",BLUE); top.addView(back,new LinearLayout.LayoutParams(dp(80),dp(48))); back.setOnClickListener(v->goBack());
        TextView title=tv(name,17,true); title.setGravity(Gravity.CENTER); top.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(top,new LinearLayout.LayoutParams(-1,dp(60)));
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(10),dp(8),dp(10),dp(30));
        ScrollView sc=new ScrollView(this); sc.setFillViewport(true); sc.addView(content);
        root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
    }
    void addCard(String title,String body){
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(14),dp(12),dp(14),dp(12));
        GradientDrawable g=new GradientDrawable(); g.setColor(WHITE); g.setCornerRadius(dp(12)); g.setStroke(1,Color.rgb(220,225,230)); c.setBackground(g);
        c.addView(tv(title,16,true),new LinearLayout.LayoutParams(-1,-2));
        c.addView(tv(body,14,false),new LinearLayout.LayoutParams(-1,-2));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(dp(3),dp(7),dp(3),dp(7)); content.addView(c,p);
    }

    // ============ تسجيل الدخول ============
    void showLogin(){
        navHistory.clear(); base();
        ScrollView sc=new ScrollView(this); sc.setFillViewport(true);
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(dp(24),dp(24),dp(24),dp(24)); sc.addView(box);
        root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        TextView logo=tv("🏫",55,true); logo.setGravity(Gravity.CENTER); box.addView(logo);
        TextView h=tv("مدرسة جعفر بن أبي طالب",22,true); h.setGravity(Gravity.CENTER); box.addView(h);
        TextView sub=tv("الجمهورية اليمنية - إب - مذيخرة - الأشعوب\nنظام الإدارة الذكي - بدون إنترنت",13,false); sub.setGravity(Gravity.CENTER); box.addView(sub);
        EditText user=new EditText(this); user.setHint("اسم المستخدم"); user.setTextSize(16);
        EditText pass=new EditText(this); pass.setHint("كلمة المرور"); pass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        Button go=btn("دخول");
        box.addView(user,new LinearLayout.LayoutParams(-1,dp(54))); box.addView(pass,new LinearLayout.LayoutParams(-1,dp(54)));
        LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-1,dp(52)); gp.setMargins(0,dp(12),0,0); box.addView(go,gp);
        TextView demo=tv("الافتراضي: admin / 1234",12,false); demo.setGravity(Gravity.CENTER); box.addView(demo);
        go.setOnClickListener(v->{
            String[] a=db.authenticate(user.getText().toString().trim(), pass.getText().toString());
            if(a==null){ Toast.makeText(this,"بيانات خاطئة",Toast.LENGTH_SHORT).show(); return; }
            currentUser=a[0]; role=a[1]; assignedGrade=a[2]; assignedClass=a[3]; currentUsername=a[4];
            navigateTo(role.equals("طالب")||role.equals("ولي أمر")?this::showPortal:this::showHome);
        });
    }
    boolean admin(){ return role.equals("القائم بأعمال المدير")||role.equals("مدير المدرسة")||role.equals("مدير النظام"); }
    boolean assignedTeacher(){ return role.equals("معلم") &&!assignedGrade.isEmpty() &&!assignedClass.isEmpty(); }
    void showPortal(){ showHome(); }

    // ============ الرئيسية ============
    void showHome(){
        base();
        LinearLayout head=new LinearLayout(this); head.setPadding(dp(10),dp(10),dp(10),dp(5));
        head.addView(tv("🏫 "+role+" : "+currentUser,15,true),new LinearLayout.LayoutParams(0,-2,1));
        Button out=btn("خروج"); head.addView(out,new LinearLayout.LayoutParams(dp(80),dp(44))); out.setOnClickListener(v->showLogin()); root.addView(head);
        ScrollView sv=new ScrollView(this); content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(10),dp(8),dp(10),dp(20)); sv.addView(content); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        // إحصائيات
        LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL);
        String[] a={"👨‍🎓\nالطلاب\n"+db.count("students"),"👨‍🏫\nالمعلمون\n"+db.count("teachers"),"🟢\nحاضر\n"+db.todayPresent(),"🔴\nغائب\n"+db.todayAbsent()};
        for(String x:a){ TextView t=tv(x,12,true); t.setGravity(Gravity.CENTER); t.setBackgroundColor(WHITE); t.setMinHeight(dp(70)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(70),1); p.setMargins(dp(3),dp(3),dp(3),dp(8)); r.addView(t,p); }
        content.addView(r);
        // شبكة
        ArrayList<String[]> list=new ArrayList<>();
        list.add(new String[]{"👨‍🎓 الطلاب","students"}); list.add(new String[]{"📊 الدرجات","grades"});
        list.add(new String[]{"✅ الحضور","attendance"}); list.add(new String[]{"📅 الجدول","timetable"});
        if(admin()){ list.add(new String[]{"👨‍🏫 المعلمون","teachers"}); list.add(new String[]{"🏫 الصفوف","classes"}); list.add(new String[]{"📈 التقارير","reports"}); list.add(new String[]{"🔔 الإعلانات","announcements"}); list.add(new String[]{"🔐 المستخدمون","users"}); list.add(new String[]{"📁 نسخ احتياطي","files"}); }
        LinearLayout row=null; int i=0;
        for(String[] item:list){ if(i%2==0){ row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); content.addView(row,new LinearLayout.LayoutParams(-1,dp(72))); } Button b=btn(item[0]); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(62),1); bp.setMargins(dp(4),dp(4),dp(4),dp(4)); row.addView(b,bp); String k=item[1]; b.setOnClickListener(v->open(k)); i++; }
        addCard("📢 آخر الإعلانات", db.latestAnnouncements());
    }
    void open(String k){
        if(k.equals("students")) navigateTo(this::students);
        else if(k.equals("grades")) navigateTo(this::grades);
        else if(k.equals("attendance")) navigateTo(this::attendance);
        else if(k.equals("timetable")) navigateTo(this::timetable);
        else if(k.equals("teachers")) navigateTo(this::teachers);
        else if(k.equals("classes")) navigateTo(this::classes);
        else if(k.equals("reports")) navigateTo(this::reports);
        else if(k.equals("announcements")) navigateTo(this::announcements);
        else if(k.equals("users")) navigateTo(this::users);
        else if(k.equals("files")) navigateTo(this::files);
    }

    // ============ الطلاب ============
    void students(){
        page("👨‍🎓 الطلاب والطالبات");
        EditText search=new EditText(this); search.setHint("بحث باسم الطالب أو الرقم..."); content.addView(search,new LinearLayout.LayoutParams(-1,dp(52)));
        if(admin()){ Button add=btn("＋ إضافة طالب"); content.addView(add,new LinearLayout.LayoutParams(-1,dp(54))); add.setOnClickListener(v->studentDialog(null)); }
        LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); content.addView(list);
        Runnable refresh=()->{
            list.removeAllViews(); String q=search.getText().toString().trim();
            for(String[] r: db.students(q)){ Button b=btn("👨‍🎓 "+r[0]+"\nالرقم: "+r[1]+" الصف: "+r[2]+" / "+r[3]); b.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(70)); bp.setMargins(0,dp(4),0,dp(4)); list.addView(b,bp); String id=r[1]; b.setOnClickListener(v->navigateTo(()->studentDetails(id))); }
            if(list.getChildCount()==0){ TextView e=tv("لا توجد بيانات",16,false); e.setGravity(Gravity.CENTER); list.addView(e); }
        };
        search.addTextChangedListener(new TextWatcher(){ public void beforeTextChanged(CharSequence s,int a,int b,int c){} public void onTextChanged(CharSequence s,int a,int b,int c){ refresh.run(); } public void afterTextChanged(Editable e){} });
        refresh.run();
    }
    void studentDialog(String id){
        boolean edit=id!=null; String[] old=edit?db.student(id):null;
        EditText n=new EditText(this); EditText sid=new EditText(this); EditText g=new EditText(this); EditText cl=new EditText(this); EditText pa=new EditText(this); EditText ph=new EditText(this);
        n.setHint("اسم الطالب"); sid.setHint("رقم الطالب"); g.setHint("الصف"); cl.setHint("الشعبة"); pa.setHint("ولي الأمر"); ph.setHint("الهاتف");
        if(old!=null){ n.setText(old[0]); sid.setText(old[1]); sid.setEnabled(false); g.setText(old[2]); cl.setText(old[3]); pa.setText(old[4]); ph.setText(old[5]); }
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(8),dp(5),dp(8),dp(5));
        for(EditText e: new EditText[]{n,sid,g,cl,pa,ph}) l.addView(e,new LinearLayout.LayoutParams(-1,dp(52)));
        new AlertDialog.Builder(this).setTitle(edit?"تعديل الطالب":"إضافة طالب").setView(l)
       .setPositiveButton("حفظ",(d,w)->{ db.saveStudent(n.getText().toString().trim(),sid.getText().toString().trim(),g.getText().toString().trim(),cl.getText().toString().trim(),pa.getText().toString().trim(),ph.getText().toString().trim()); students(); })
       .setNegativeButton("إلغاء",null).show();
    }
    void studentDetails(String id){
        page("👨‍🎓 ملف الطالب");
        String[] r=db.student(id); if(r==null){ addCard("خطأ","لم يتم العثور على الطالب"); return; }
        addCard("👨‍🎓 "+r[0], "الرقم: "+r[1]+"\nالصف: "+r[2]+" - "+r[3]+"\nولي الأمر: "+r[4]+"\nالهاتف: "+r[5]);
        // نتيجة سريعة
        StringBuilder sb=new StringBuilder();
        double totalAll=0; for(String sub: SUBJECTS_7){ double tot=db.annualTotal(id,sub); totalAll+=tot; sb.append(sub).append(": ").append((int)tot).append(" /100 - ").append(DB.gradeFor(tot)).append("\n"); }
        double pct=totalAll/(SUBJECTS_7.length*100.0)*100;
        sb.append("\nالمجموع: ").append((int)totalAll).append(" النسبة: ").append(String.format("%.1f",pct)).append("% التقدير: ").append(DB.gradeFor(pct)).append(" الحالة: ").append(pct>=50?"ناجح":"راسب");
        addCard("📊 النتيجة السنوية", sb.toString());
        Button pdf=btn("🎓 شهادة هذا الطالب PDF"); content.addView(pdf,new LinearLayout.LayoutParams(-1,dp(56))); pdf.setOnClickListener(v->generateCertificatePdf(id));
        Button pdf2=btn("📄 ملف الطالب PDF"); content.addView(pdf2,new LinearLayout.LayoutParams(-1,dp(56))); pdf2.setOnClickListener(v->generateStudentPdf(id));
    }

    // ============ الدرجات - النظام الجديد ============
    void grades(){
        page("📊 الدرجات");
        addCard("طريقة الإدخال الجديدة","اختر الصف والشعبة ثم الفصل.\nلكل مادة: محصلة /20 + اختبار /30.\nالفصل الأول: محصلة1 + اختبار نصف العام\nالفصل الثاني: محصلة2 + اختبار نهائي\nالمجموع السنوي لكل مادة /100");
        Button e=btn("✏️ إدخال درجات الطلاب"); content.addView(e,new LinearLayout.LayoutParams(-1,dp(60))); e.setOnClickListener(v->chooseClassForGrades());
    }
    ArrayList<String[]> allowedClasses(){
        ArrayList<String[]> all=db.classes(); if(admin()) return all;
        ArrayList<String[]> res=new ArrayList<>(); if(assignedTeacher()) for(String[] c: all) if(c[0].equals(assignedGrade)&&c[1].equals(assignedClass)) res.add(c); return res;
    }
    void chooseClassForGrades(){
        ArrayList<String[]> cs=allowedClasses(); if(cs.isEmpty()){ addCard("لا توجد صفوف","لا يوجد صف مسموح"); return; }
        String[] it=new String[cs.size()]; for(int i=0;i<cs.size();i++) it[i]="الصف "+cs.get(i)[0]+" - "+cs.get(i)[1];
        new AlertDialog.Builder(this).setTitle("اختر الصف").setItems(it,(d,w)->navigateTo(()->classGrades(cs.get(w)[0],cs.get(w)[1]))).show();
    }
    void classGrades(String grade,String classroom){
        page("📊 درجات "+grade+" / "+classroom);
        Spinner sem=new Spinner(this); sem.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"الفصل الأول","الفصل الثاني"}));
        content.addView(sem,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView info=tv("⬅ اسحب الجدول أفقيًا — اضغط على خانة المادة لإدخال الدرجة",13,true); info.setGravity(Gravity.CENTER); content.addView(info);
        HorizontalScrollView hsv=new HorizontalScrollView(this); LinearLayout table=new LinearLayout(this); table.setOrientation(LinearLayout.VERTICAL); hsv.addView(table); content.addView(hsv,new LinearLayout.LayoutParams(-1,-2));
        Runnable render=()->{
            table.removeAllViews();
            // رأس
            LinearLayout hr=new LinearLayout(this); hr.setOrientation(LinearLayout.HORIZONTAL);
            TextView hn=tv("الطالب",13,true); hr.addView(hn,new LinearLayout.LayoutParams(180,dp(54)));
            for(String sub: SUBJECTS_7){ TextView s=tv(sub,10,true); s.setGravity(Gravity.CENTER); hr.addView(s,new LinearLayout.LayoutParams(125,dp(54))); }
            TextView hb=tv("السلوك",11,true); hb.setGravity(Gravity.CENTER); hr.addView(hb,new LinearLayout.LayoutParams(100,dp(54)));
            table.addView(hr);
            int semester=sem.getSelectedItemPosition()+1;
            for(String[] st: db.studentsInClass(grade,classroom)){
                LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
                TextView name=tv(st[0]+"\n"+st[1],11,true); row.addView(name,new LinearLayout.LayoutParams(180,dp(64)));
                for(String sub: SUBJECTS_7){
                    double m=db.getMahsala(st[1],sub,semester); double e=db.getExam(st[1],sub,semester); double tot=m+e;
                    Button cell=smallBtn(tot==0?"✏ إدخال":formatNumber(tot)+" /50", tot==0?ORANGE:GREEN); cell.setTextSize(11);
                    LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(125,dp(60)); cp.setMargins(dp(3),dp(3),dp(3),dp(3)); row.addView(cell,cp);
                    cell.setOnClickListener(v->scoreDialog(st[1],st[0],sub,semester,cell));
                }
                double beh=db.getBehavior(st[1],semester);
                Button bc=smallBtn(beh==0?"سلوك":formatNumber(beh)+" /100", beh==0?ORANGE:BLUE); row.addView(bc,new LinearLayout.LayoutParams(100,dp(60)));
                bc.setOnClickListener(v->{ EditText e=scoreEdit("السلوك /100"); if(beh!=0) e.setText(formatNumber(beh)); new AlertDialog.Builder(this).setTitle(st[0]+" - السلوك").setView(e).setPositiveButton("حفظ",(d,w)->{ db.setBehavior(st[1],semester,limit(parse(e),0,100)); Toast.makeText(this,"تم حفظ السلوك",Toast.LENGTH_SHORT).show(); render.run(); }).setNegativeButton("إلغاء",null).show(); });
                table.addView(row);
            }
        };
        sem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){ public void onItemSelected(AdapterView<?> p,View v,int po,long id){ render.run(); } public void onNothingSelected(AdapterView<?> p){} });
        render.run();
        Button cards=btn("🖨️ طباعة بطاقات النتائج - 3 في صفحة"); content.addView(cards,new LinearLayout.LayoutParams(-1,dp(56))); cards.setOnClickListener(v->generateExamCardsPdf(grade,classroom,sem.getSelectedItemPosition()+1));
        Button sheet=btn("📋 كشف الصف الجماعي PDF - مرتب"); content.addView(sheet,new LinearLayout.LayoutParams(-1,dp(56))); sheet.setOnClickListener(v->generateClassSheetPdf(grade,classroom));
        Button examBtn=btn("📝 إدخال اختبارات الفصل"); content.addView(examBtn,new LinearLayout.LayoutParams(-1,dp(56))); examBtn.setOnClickListener(v->examEntry(grade,classroom,sem.getSelectedItemPosition()+1));
    }

    String formatNumber(double x){ if(x==Math.round(x)) return String.valueOf((int)x); return String.format(Locale.US,"%.1f",x); }
    EditText scoreEdit(String hint){ EditText e=new EditText(this); e.setHint(hint); e.setTextSize(15); e.setSingleLine(true); e.setGravity(Gravity.CENTER); e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL); e.setFocusableInTouchMode(true); return e; }
    double parse(EditText e){ try{ String s=e.getText().toString().trim(); if(s.isEmpty()) return 0; return Double.parseDouble(s); }catch(Exception ex){ return 0; } }
    double limit(double x,double min,double max){ if(x<min) return min; if(x>max) return max; return x; }

    // نافذة إدخال محصلة + اختبار - حفظ فعلي
    void scoreDialog(String sid,String sname,String subject,int sem,Button cell){
        EditText mah=scoreEdit("المحصلة /20"); EditText ex=scoreEdit("الاختبار /30");
        double oldM=db.getMahsala(sid,subject,sem); double oldE=db.getExam(sid,subject,sem);
        if(oldM!=0) mah.setText(formatNumber(oldM)); if(oldE!=0) ex.setText(formatNumber(oldE));
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(10),dp(5),dp(10),dp(5));
        box.addView(mah,new LinearLayout.LayoutParams(-1,dp(52))); box.addView(ex,new LinearLayout.LayoutParams(-1,dp(52)));
        AlertDialog d=new AlertDialog.Builder(this).setTitle(sname+"\n"+subject+" - الفصل "+(sem==1?"الأول":"الثاني")).setView(box).setPositiveButton("حفظ",null).setNegativeButton("إلغاء",null).create();
        d.setOnShowListener(ev->{ d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            double m=limit(parse(mah),0,20); double e=limit(parse(ex),0,30);
            db.setMahsala(sid,subject,sem,m); db.setExam(sid,subject,sem,e);
            double tot=m+e; cell.setText(formatNumber(tot)+" /50"); cell.setBackground(background(GREEN));
            Toast.makeText(this,"تم حفظ "+subject+" = "+tot+" /50",Toast.LENGTH_SHORT).show(); d.dismiss();
        }); });
        d.show();
    }
    void examEntry(String grade,String classroom,int semester){
        page("📝 اختبارات "+grade+" / "+classroom);
        for(String[] st: db.studentsInClass(grade,classroom)){
            addCard("👨‍🎓 "+st[0],"الرقم: "+st[1]);
            for(String subject: SUBJECTS_7){
                LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
                TextView name=tv(subject,12,true); row.addView(name,new LinearLayout.LayoutParams(0,dp(48),1));
                EditText e=scoreEdit("الاختبار /30"); double old=db.getExam(st[1],subject,semester); if(old!=0) e.setText(formatNumber(old)); row.addView(e,new LinearLayout.LayoutParams(120,dp(48)));
                Button save=smallBtn("حفظ",GREEN); row.addView(save,new LinearLayout.LayoutParams(dp(65),dp(46)));
                content.addView(row,new LinearLayout.LayoutParams(-1,dp(52)));
                save.setOnClickListener(v->{ double x=limit(parse(e),0,30); db.setExam(st[1],subject,semester,x); Toast.makeText(this,"تم حفظ "+subject,Toast.LENGTH_SHORT).show(); });
            }
        }
    }

    // ============ الشهادة ============
    void generateCertificatePdf(String sid){
        String[] st=db.student(sid); if(st==null) return;
        PdfDocument doc=new PdfDocument(); int W=595,H=842;
        PdfDocument.PageInfo pi=new PdfDocument.PageInfo.Builder(W,H,1).create(); PdfDocument.Page page=doc.startPage(pi); Canvas c=page.getCanvas();
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(Color.BLACK); p.setTextAlign(Paint.Align.CENTER);
        Paint line=new Paint(); line.setColor(Color.BLACK); line.setStyle(Paint.Style.STROKE);
        p.setTextSize(12); p.setFakeBoldText(true); c.drawText("الجمهورية اليمنية - وزارة التربية والتعليم والبحث العلمي",W/2,32,p);
        c.drawText("مدرسة جعفر بن أبي طالب - مديرية مذيخرة - إب",W/2,52,p);
        c.drawRect(15,15,W-15,H-15,line);
        p.setTextSize(16); c.drawText("شهادة نتيجة الطالب - العام 2026-2027",W/2,90,p);
        p.setTextSize(11); p.setFakeBoldText(false); c.drawText("اسم الطالب: "+st[0]+" الصف: "+st[2]+"/"+st[3]+" الرقم: "+st[1],W/2,115,p);
        int top=135; c.drawRect(20,top,W-20,top+20,line); p.setTextSize(9); p.setFakeBoldText(true);
        c.drawText("المادة",520,top+14,p); c.drawText("محصلة1 20",420,top+14,p); c.drawText("اختبار نصف 30",320,top+14,p); c.drawText("محصلة2 20",220,top+14,p); c.drawText("اختبار نهائي 30",120,top+14,p); c.drawText("المجموع 100",60,top+14,p); p.setFakeBoldText(false);
        double totalAll=0; int y=top+20; boolean allPass=true;
        for(String sub: SUBJECTS_7){
            y+=22; c.drawRect(20,y-16,W-20,y,line);
            double m1=db.getMahsala(sid,sub,1),e1=db.getExam(sid,sub,1),m2=db.getMahsala(sid,sub,2),e2=db.getExam(sid,sub,2);
            double tot=m1+e1+m2+e2; totalAll+=tot; if(tot<50) allPass=false;
            c.drawText(sub,520,y-5,p); c.drawText(String.valueOf((int)m1),420,y-5,p); c.drawText(String.valueOf((int)e1),320,y-5,p); c.drawText(String.valueOf((int)m2),220,y-5,p); c.drawText(String.valueOf((int)e2),120,y-5,p); c.drawText((int)tot+" "+DB.gradeFor(tot),60,y-5,p);
        }
        y+=22; c.drawRect(20,y-16,W-20,y,line); c.drawText("السلوك",520,y-5,p); c.drawText(String.valueOf((int)db.annualBehavior(sid)),60,y-5,p);
        double pct=totalAll/(SUBJECTS_7.length*100.0)*100; boolean pass=allPass && pct>=50;
        y+=38; p.setTextSize(12); p.setFakeBoldText(true); c.drawText("المجموع الكلي: "+(int)totalAll+" / "+(SUBJECTS_7.length*100),W/2,y,p);
        c.drawText("النسبة: "+String.format("%.1f",pct)+" % التقدير: "+DB.gradeFor(pct),W/2,y+22,p);
        p.setColor(pass?Color.rgb(0,120,0):Color.RED); p.setTextSize(18); c.drawText(pass?"ناجح ✓":"راسب ✗",W/2,y+55,p);
        p.setColor(Color.BLACK); p.setTextSize(10); p.setFakeBoldText(false); c.drawText("مدير المدرسة:.................... التوقيع والختم",W/2,y+90,p);
        doc.finishPage(page);
        try{ File f=new File(getExternalFilesDir(null),"cert_"+sid+".pdf"); FileOutputStream o=new FileOutputStream(f); doc.writeTo(o); doc.close(); o.close(); Uri uri=FileProvider.getUriForFile(this,getPackageName()+".provider",f); Intent it=new Intent(Intent.ACTION_VIEW); it.setDataAndType(uri,"application/pdf"); it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); startActivity(it); }catch(Exception e){ Toast.makeText(this,e.getMessage(),1).show(); }
    }

    // ============ كشف الصف الجماعي - مرتب + تعدد صفحات ============
    void generateClassSheetPdf(String grade,String classroom){
        ArrayList<String[]> students=db.studentsInClass(grade,classroom);
        ArrayList<Double> totals=new ArrayList<>(); for(String[] s: students){ double t=0; for(String sub: SUBJECTS_7) t+=db.annualTotal(s[1],sub); totals.add(t); }
        for(int i=0;i<students.size();i++) for(int j=i+1;j<students.size();j++) if(totals.get(j)>totals.get(i)){ Collections.swap(students,i,j); Collections.swap(totals,i,j); }
        PdfDocument doc=new PdfDocument(); int W=842,H=595, perPage=20, pageNum=0;
        for(int start=0; start<students.size(); start+=perPage){
            pageNum++; PdfDocument.PageInfo pi=new PdfDocument.PageInfo.Builder(W,H,pageNum).create(); PdfDocument.Page page=doc.startPage(pi); Canvas c=page.getCanvas();
            Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(Color.BLACK); p.setTextAlign(Paint.Align.CENTER); Paint line=new Paint(); line.setColor(Color.BLACK); line.setStyle(Paint.Style.STROKE);
            p.setTextSize(11); p.setFakeBoldText(true); c.drawText("كشف نتائج الصف "+grade+" / "+classroom+" - مدرسة جعفر بن أبي طالب - 2026-2027 - صفحة "+pageNum,W/2,30,p); p.setFakeBoldText(false);
            int top=45; c.drawRect(15,top,W-15,top+18,line); p.setTextSize(8);
            c.drawText("المركز",30,top+12,p); c.drawText("اسم الطالب",150,top+12,p);
            int x=250; for(String sub: SUBJECTS_7){ c.drawText(sub.substring(0,4),x,top+12,p); x+=70; }
            c.drawText("السلوك",x,top+12,p); c.drawText("المجموع",x+70,top+12,p); c.drawText("النسبة",x+130,top+12,p); c.drawText("النتيجة",x+185,top+12,p);
            int y=top+18;
            for(int i=start;i<Math.min(start+perPage,students.size());i++){
                y+=20; c.drawRect(15,y-14,W-15,y,line);
                String[] s=students.get(i); double tot=totals.get(i); double pct=tot/(SUBJECTS_7.length*100.0)*100;
                c.drawText(String.valueOf(i+1),30,y-4,p); c.drawText(s[0],150,y-4,p);
                int xx=250; for(String sub: SUBJECTS_7){ c.drawText(String.valueOf((int)db.annualTotal(s[1],sub)),xx,y-4,p); xx+=70; }
                c.drawText(String.valueOf((int)db.annualBehavior(s[1])),xx,y-4,p); c.drawText(String.valueOf((int)tot),xx+70,y-4,p); c.drawText(String.format("%.1f%%",pct),xx+130,y-4,p); c.drawText(pct>=50?"ناجح":"راسب",xx+185,y-4,p);
            }
            doc.finishPage(page);
        }
        try{ File f=new File(getExternalFilesDir(null),"sheet_"+grade+"_"+classroom+".pdf"); FileOutputStream o=new FileOutputStream(f); doc.writeTo(o); doc.close(); o.close(); Uri uri=FileProvider.getUriForFile(this,getPackageName()+".provider",f); Intent it=new Intent(Intent.ACTION_VIEW); it.setDataAndType(uri,"application/pdf"); it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); startActivity(it); }catch(Exception e){ Toast.makeText(this,e.getMessage(),1).show(); }
    }

    // ============ بطاقات 3 في صفحة - تصميم أصلي بدون تكرار ============
    void generateExamCardsPdf(String grade,String classroom,int semester){
        ArrayList<String[]> students=db.studentsInClass(grade,classroom);
        if(students.isEmpty()){ Toast.makeText(this,"لا يوجد طلاب",Toast.LENGTH_SHORT).show(); return; }
        PdfDocument doc=new PdfDocument(); int pageW=595,pageH=842,cardH=258;
        for(int idx=0;idx<students.size();idx+=3){
            PdfDocument.PageInfo pi=new PdfDocument.PageInfo.Builder(pageW,pageH,idx/3+1).create(); PdfDocument.Page page=doc.startPage(pi); Canvas c=page.getCanvas();
            Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); Paint line=new Paint(); line.setColor(Color.BLACK); line.setStyle(Paint.Style.STROKE); line.setStrokeWidth(1f);
            Paint fill=new Paint(Paint.ANTI_ALIAS_FLAG); fill.setColor(Color.BLACK); fill.setTextAlign(Paint.Align.RIGHT);
            for(int k=0;k<3 && idx+k<students.size();k++){
                String[] st=students.get(idx+k); int top=12+k*(cardH+10);
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.5f); p.setColor(Color.BLACK); c.drawRect(10,top,pageW-10,top+cardH,p);
                fill.setTextSize(7.5f); fill.setFakeBoldText(true); c.drawText("الجمهورية اليمنية",pageW-15,top+14,fill); fill.setFakeBoldText(false); c.drawText("وزارة التربية والتعليم",pageW-15,top+26,fill);
                c.drawText("مدرسة جعفر بن أبي طالب",15,top+14,fill); c.drawText("مديرية مذيخرة",15,top+26,fill);
                // عنوان واحد فقط
                c.drawRect(15,top+32,pageW-15,top+48,line); fill.setTextSize(9); fill.setFakeBoldText(true); fill.setTextAlign(Paint.Align.CENTER); c.drawText("نتيجة امتحان نهاية الفصل الدراسي الأول 2026 - 2027م",pageW/2,top+43,fill); fill.setTextAlign(Paint.Align.RIGHT); fill.setFakeBoldText(false);
                c.drawRect(15,top+50,pageW-15,top+66,line); fill.setTextSize(8.5f); fill.setFakeBoldText(true); c.drawText("اسم الطالب: "+st[0]+" الصف: "+grade+" / "+classroom,pageW-15,top+61,fill); fill.setFakeBoldText(false);
                int tableTop=top+70; c.drawRect(15,tableTop,pageW-15,tableTop+16,line); fill.setTextSize(7.5f); fill.setFakeBoldText(true);
                c.drawText("المادة",pageW-20,tableTop+11,fill); c.drawText("المحصلة (20)",390,tableTop+11,fill); c.drawText("الاختبار (30)",290,tableTop+11,fill); c.drawText("المجموع (50)",190,tableTop+11,fill); fill.setFakeBoldText(false);
                int y=tableTop+16; double totalAll=0;
                for(String sub: SUBJECTS_7){ y+=16; c.drawRect(15,y-12,pageW-15,y,line); double m=db.getMahsala(st[1],sub,semester); double e=db.getExam(st[1],sub,semester); double tot=m+e; totalAll+=tot; c.drawText(sub,pageW-20,y-3,fill); c.drawText(String.valueOf((int)m),390,y-3,fill); c.drawText(String.valueOf((int)e),290,y-3,fill); c.drawText(String.valueOf((int)tot),190,y-3,fill); }
                // ملخص بطاقة - 7 مواد مجموعها 350 للفصل
                y+=8; fill.setTextSize(7.5f); c.drawText("المجموع (350): "+(int)totalAll+" التقدير: ____ الترتيب: ____",pageW-15,y+8,fill); c.drawText("مدير المدرسة:....................................",pageW-15,y+22,fill);
                if(k<2){ Paint dash=new Paint(); dash.setColor(Color.BLACK); dash.setPathEffect(new DashPathEffect(new float[]{4,4},0)); c.drawLine(10,top+cardH+5,pageW-10,top+cardH+5,dash); fill.setTextSize(6); fill.setTextAlign(Paint.Align.CENTER); c.drawText("قص هنا",pageW/2,top+cardH+3,fill); fill.setTextAlign(Paint.Align.RIGHT); }
            }
            doc.finishPage(page);
        }
        try{ File f=new File(getExternalFilesDir(null),"cards_"+grade+"_"+classroom+".pdf"); FileOutputStream o=new FileOutputStream(f); doc.writeTo(o); doc.close(); o.close(); Uri uri=FileProvider.getUriForFile(this,getPackageName()+".provider",f); Intent it=new Intent(Intent.ACTION_VIEW); it.setDataAndType(uri,"application/pdf"); it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); startActivity(it); }catch(Exception e){ Toast.makeText(this,e.getMessage(),1).show(); }
    }

    // ============ ملف طالب PDF ============
    void generateStudentPdf(String id){ generateCertificatePdf(id); }

    // ============ بقية الشاشات (مختصرة) ============
    void attendance(){ page("✅ الحضور والغياب"); addCard("نظام الحضور","اختر الصف ثم الشهر. سيظهر جميع الطلاب وجميع أيام الدوام من السبت إلى الأربعاء."); Button b=btn("اختر الصف والشعبة"); content.addView(b,new LinearLayout.LayoutParams(-1,dp(56))); b.setOnClickListener(v->chooseAttendanceClass()); }
    void chooseAttendanceClass(){ ArrayList<String[]> cs=allowedClasses(); String[] it=new String[cs.size()]; for(int i=0;i<cs.size();i++) it[i]="الصف "+cs.get(i)[0]+" - "+cs.get(i)[1]; new AlertDialog.Builder(this).setTitle("اختر الصف").setItems(it,(d,w)->navigateTo(()->attendanceClass(cs.get(w)[0],cs.get(w)[1]))).show(); }
    void attendanceClass(String grade,String classroom){ page("✅ حضور "+grade+" / "+classroom); addCard("الطلاب","عدد الطلاب: "+db.studentsInClass(grade,classroom).size()); }
    void timetable(){ page("📅 الجدول"); addCard("الجدول الدراسي","السبت - الأربعاء"); }
    void teachers(){ page("👨‍🏫 المعلمون"); addCard("المعلمون","عدد المعلمين: "+db.count("teachers")); }
    void classes(){ page("🏫 الصفوف"); for(String[] c: db.classes()) addCard("الصف "+c[0]+" - "+c[1],"عدد الطلاب: "+db.studentsInClass(c[0],c[1]).size()); }
    void reports(){ page("📈 التقارير"); Button b=btn("كشف الصف الجماعي"); content.addView(b,new LinearLayout.LayoutParams(-1,dp(56))); b.setOnClickListener(v->chooseClassForGrades()); addCard("التقارير","إجمالي الطلاب: "+db.count("students")); }
    void announcements(){ page("🔔 الإعلانات"); addCard("الإعلانات",db.latestAnnouncements()); }
    void users(){ page("🔐 المستخدمون"); addCard("المستخدمون","مدير النظام: admin"); }
    void files(){ page("📁 نسخ احتياطي"); Button b=btn("إنشاء نسخة"); content.addView(b,new LinearLayout.LayoutParams(-1,dp(56))); b.setOnClickListener(v->Toast.makeText(this,"تم إنشاء النسخة",Toast.LENGTH_SHORT).show()); }
}
