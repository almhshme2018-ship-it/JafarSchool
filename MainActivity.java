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
        Button b=new Button(this); b.setText(text); b.setTextSize(12
