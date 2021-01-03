package com.example.icandoit.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.icandoit.R;
import com.example.icandoit.model.Diary;
import com.example.icandoit.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    CalendarView calendarView;
    Toolbar toolbar;
    FloatingActionButton writeDiary;
    Context context = this;
    Calendar cal = Calendar.getInstance();
    List<EventDay> events = new ArrayList<>();
    RequestQueue queue;
    Gson parser = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getHashKey();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        toolbar = findViewById(R.id.main_toolbar);

        writeDiary = findViewById(R.id.writeDiaryButton);
        writeDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                startActivity(intent);
            }
        });

        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnPreviousPageChangeListener(new OnCalendarPageChangeListener() {
            @Override
            public void onChange() {
                cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)-1, 1);
                addEvent();
            }
        });
        //-----------------------------------------------| Forward Month
        calendarView.setOnForwardPageChangeListener(new OnCalendarPageChangeListener() {
            @Override
            public void onChange() {
                cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, 1);
                addEvent();
            }
        });
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                System.out.println(eventDay.getCalendar().get(Calendar.DATE));
                boolean is_diary = false;
                for (int i = 0; i < events.size(); i++) {
                    if (events.get(i) == eventDay) {
                        is_diary = true;
                        break;
                    }
                }
                if (!is_diary) {
                    Log.i("return", "return");
                    return;
                }
                Calendar calendar = eventDay.getCalendar();
                String date = String.format("%04d",calendar.get(Calendar.YEAR))+"-"+
                        String.format("%02d", calendar.get(Calendar.MONTH)+1)+"-"+
                        String.format("%02d", calendar.get(Calendar.DATE));
                String user = User.getInstance().getId();
                String url = ChatActivity.url+"api/diary?"+"dateTime="+date+"&"+"user_id="+user;

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("LOG_RESPONSE", response);
                                JSONObject jObject = null;
                                ArrayList<Double> days = null;
                                try {
                                    jObject = new JSONObject(response);
                                    String date = jObject.getString("dateTime");
                                    String content = jObject.getString("content");
                                    String image = jObject.getString("image");
                                    Date d = new Date();
                                    int year = Integer.parseInt(date.substring(0,4));
                                    int month = Integer.parseInt(date.substring(5,7));
                                    int day = Integer.parseInt(date.substring(8,10));
                                    Diary diary = Diary.getinstance();
                                    diary.clear();
                                    diary.setText(content);
                                    diary.setIntent(jObject.getString("intent"));
                                    diary.setWhat(jObject.getString("what"));
                                    diary.setWhere(jObject.getString("where"));
                                    diary.setHow(jObject.getString("how"));
                                    diary.setWho(jObject.getString("who"));
                                    diary.setWhy(jObject.getString("why"));
                                    diary.setWhen(jObject.getString("when"));
                                    diary.setDate(year, month, day+1);
                                    diary.setImage(image);
                                    Intent intent = new Intent(context, ResultActivity.class);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Log.e("date error", e.getMessage());
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("request", "error");
                    }
                });
                queue.add(stringRequest);
            }
        });
        queue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        events = new ArrayList<>();
        addEvent();
    }

    private void addEvent() {
        JSONObject jsonObject = new JSONObject();
        String user = User.getInstance().getId();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        String userParam = "user_id="+user;
        String yearParam = "year="+String.format("%04d",year);
        String monthParam = "month="+String.format("%02d", month);
        Log.i("year", String.format("%04d",year));
        Log.i("month", String.format("%02d", month));
        String url = ChatActivity.url+"api/date?"+userParam+"&"+yearParam+"&"+monthParam;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("LOG_RESPONSE", response);
                        JSONObject jObject = null;
                        JSONArray jarray = null;
                        ArrayList<Double> days = null;
                        try {
                            jObject = new JSONObject(response);
                            days = parser.fromJson(jObject.getString("dates"), ArrayList.class);
                            for (double day : days) {
                                Calendar c = Calendar.getInstance();
                                c.set(year, month-1, (int)day);
                                events.add(new EventDay(c, R.drawable.diary));
                            }
                            calendarView.setEvents(events);
                        } catch (Exception e) {
                            Log.e("date error", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("request", "error");
            }
        });
        queue.add(stringRequest);
    }

    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }
}