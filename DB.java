package com.jafar.school;
import android.content.*;
import android.database.sqlite.*;
import android.database.Cursor;
import java.util.*;

public class DB extends SQLiteOpenHelper {
    public DB(Context c){ super(c,"school.db",null,3); }
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE students(id TEXT PRIMARY KEY, name TEXT, grade TEXT, class TEXT, parent TEXT, phone TEXT)");
        db.execSQL("CREATE TABLE scores(student_id TEXT, subject TEXT, semester INTEGER, mahsala REAL, exam REAL, PRIMARY KEY(student_id,subject,semester))");
        db.execSQL("CREATE TABLE behavior(student_id TEXT, semester INTEGER, score REAL, PRIMARY KEY(student_id,semester))");
        db.execSQL("CREATE TABLE users(name TEXT, username TEXT PRIMARY KEY, pass TEXT, role TEXT, grade TEXT, class TEXT)");
        db.execSQL("INSERT INTO users VALUES('المدير','admin','1234','القائم بأعمال المدير','','')");
    }
    public void onUpgrade(SQLiteDatabase db,int o,int n){ db.execSQL("DROP TABLE IF EXISTS scores"); onCreate(db); }
    public void setMahsala(String sid,String sub,int sem,double v){
    SQLiteDatabase db=getWritableDatabase();
    Cursor c=db.rawQuery("SELECT exam FROM scores WHERE student_id=? AND subject=? AND semester=?",new String[]{sid,sub,String.valueOf(sem)});
    double oldExam=0; boolean exists=c.moveToFirst(); if(exists &&!c.isNull(0)) oldExam=c.getDouble(0); c.close();
    ContentValues cv=new ContentValues(); cv.put("student_id",sid); cv.put("subject",sub); cv.put("semester",sem); cv.put("mahsala",v); cv.put("exam",oldExam);
    db.insertWithOnConflict("scores",null,cv,SQLiteDatabase.CONFLICT_REPLACE);
}
public void setExam(String sid,String sub,int sem,double v){
    SQLiteDatabase db=getWritableDatabase();
    Cursor c=db.rawQuery("SELECT mahsala FROM scores WHERE student_id=? AND subject=? AND semester=?",new String[]{sid,sub,String.valueOf(sem)});
    double oldMah=0; boolean exists=c.moveToFirst(); if(exists &&!c.isNull(0)) oldMah=c.getDouble(0); c.close();
    ContentValues cv=new ContentValues(); cv.put("student_id",sid); cv.put("subject",sub); cv.put("semester",sem); cv.put("mahsala",oldMah); cv.put("exam",v);
    db.insertWithOnConflict("scores",null,cv,SQLiteDatabase.CONFLICT_REPLACE);
}
    public double getMahsala(String sid,String sub,int sem){ Cursor c=getReadableDatabase().rawQuery("SELECT mahsala FROM scores WHERE student_id=? AND subject=? AND semester=?",new String[]{sid,sub,String.valueOf(sem)}); double v=0; if(c.moveToFirst()) v=c.getDouble(0); c.close(); return v; }
    public double getExam(String sid,String sub,int sem){ Cursor c=getReadableDatabase().rawQuery("SELECT exam FROM scores WHERE student_id=? AND subject=? AND semester=?",new String[]{sid,sub,String.valueOf(sem)}); double v=0; if(c.moveToFirst()) v=c.getDouble(0); c.close(); return v; }
    public double annualTotal(String sid,String sub){ return getMahsala(sid,sub,1)+getExam(sid,sub,1)+getMahsala(sid,sub,2)+getExam(sid,sub,2); }
    public void setBehavior(String sid,int sem,double v){ SQLiteDatabase db=getWritableDatabase(); ContentValues cv=new ContentValues(); cv.put("student_id",sid); cv.put("semester",sem); cv.put("score",v); db.insertWithOnConflict("behavior",null,cv,SQLiteDatabase.CONFLICT_REPLACE); }
    public double getBehavior(String sid,int sem){ Cursor c=getReadableDatabase().rawQuery("SELECT score FROM behavior WHERE student_id=? AND semester=?",new String[]{sid,String.valueOf(sem)}); double v=0; if(c.moveToFirst()) v=c.getDouble(0); c.close(); return v; }
    public double annualBehavior(String sid){ return (getBehavior(sid,1)+getBehavior(sid,2))/2; }
    public static String gradeFor(double total){ if(total>=90) return "ممتاز"; if(total>=80) return "جيد جداً"; if(total>=70) return "جيد"; if(total>=60) return "مقبول"; if(total>=50) return "ضعيف"; return "راسب"; }
    public String[] authenticate(String u,String p){ Cursor c=getReadableDatabase().rawQuery("SELECT name,role,grade,class,username FROM users WHERE username=? AND pass=?",new String[]{u,p}); if(!c.moveToFirst()){c.close();return null;} String[] a={c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4)}; c.close(); return a; }
    public int count(String t){ Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM "+t,null); c.moveToFirst(); int n=c.getInt(0); c.close(); return n; }
    public int todayPresent(){return 0;} public int todayAbsent(){return 0;} public String latestAnnouncements(){return "لا توجد إعلانات"; }
    public String[] student(String id){ Cursor c=getReadableDatabase().rawQuery("SELECT name,id,grade,class,parent,phone FROM students WHERE id=?",new String[]{id}); if(!c.moveToFirst()){c.close();return null;} String[] a=new String[6]; for(int i=0;i<6;i++) a[i]=c.getString(i); c.close(); return a; }
    public ArrayList<String[]> students(String q){ ArrayList<String[]> r=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT name,id,grade,class FROM students WHERE name LIKE? OR id LIKE?",new String[]{"%"+q+"%","%"+q+"%"}); while(c.moveToNext()) r.add(new String[]{c.getString(0),c.getString(1),c.getString(2),c.getString(3)}); c.close(); return r; }
    public ArrayList<String[]> studentsInClass(String g,String cl){ ArrayList<String[]> r=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT name,id FROM students WHERE grade=? AND class=? ORDER BY name",new String[]{g,cl}); while(c.moveToNext()) r.add(new String[]{c.getString(0),c.getString(1)}); c.close(); return r; }
    public ArrayList<String[]> classes(){ ArrayList<String[]> r=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT DISTINCT grade,class FROM students",null); while(c.moveToNext()) r.add(new String[]{c.getString(0),c.getString(1)}); c.close(); if(r.isEmpty()){r.add(new String[]{"1","أ"});} return r; }
    public void saveStudent(String n,String id,String g,String cl,String pa,String ph){ SQLiteDatabase db=getWritableDatabase(); ContentValues cv=new ContentValues(); cv.put("id",id); cv.put("name",n); cv.put("grade",g); cv.put("class",cl); cv.put("parent",pa); cv.put("phone",ph); db.insertWithOnConflict("students",null,cv,SQLiteDatabase.CONFLICT_REPLACE); }
    public String resultLine(String id,int sem){ return ""; } public String studentAttendanceSummary(String id){ return ""; }
    public String[] subjects(){ return new String[]{"القرآن الكريم","التربية الإسلامية","اللغة العربية","الرياضيات","العلوم","الاجتماعيات","اللغة الإنجليزية"}; }
    // توافق قديم
    public double[] monthlyValues(String sid,String sub,int sem,int mon){ return new double[]{getMahsala(sid,sub,sem),0,0,0}; }
    public double exam(String sid,String sub,int sem){ return getExam(sid,sub,sem); }
    public void setMonthlyScore(String sid,String sub,int sem,int mon,double a,double o,double h,double w){ setMahsala(sid,sub,sem,a+o+h+w); }
}
