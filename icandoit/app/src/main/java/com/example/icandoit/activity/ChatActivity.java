package com.example.icandoit.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.icandoit.R;
import com.example.icandoit.model.Diary;
import com.example.icandoit.model.SendMessage;
import com.example.icandoit.model.User;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

public class ChatActivity extends AppCompatActivity {
    String sessionId;
    //여기에 node ip를 써줘요
    public final static String url = "http://13.124.136.171:3000/";
    RequestQueue queue;
    Gson gson = new Gson();
    ChatView chatView;
    Toolbar toolbar;
    Context context = this;
    boolean finish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        queue = Volley.newRequestQueue(this);

        chatView = (ChatView) findViewById(R.id.chat_view);
        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener() {
            @Override
            public boolean sendMessage(ChatMessage chatMessage) {
                Log.d("hello",chatMessage.getMessage());
                sendToBot(false, chatMessage.getMessage());
                return true;
            }
        });

        getSessionId();

        chatView.setTypingListener(new ChatView.TypingListener() {
            @Override
            public void userStartedTyping() {

            }

            @Override
            public void userStoppedTyping() {

            }
        });

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    //액션버튼 메뉴 액션바에 집어 넣기
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;

            }
            case R.id.menu_end: {
                if (!finish) {
                    Toast.makeText(getApplicationContext(),"채팅을 더 해주세요!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),"일기 만들게요", Toast.LENGTH_SHORT).show();
                    finishChat();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected void sendToBot(boolean first, String message) {
        SendMessage send = new SendMessage(sessionId, first, message);
        Log.i("id", send.getSession_id());
        String rBody = gson.toJson(send).toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url + "api/message", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("LOG_RESPONSE", response);
                JSONObject jObject = null;
                JSONArray jarray = null;
                try {
                    jObject = new JSONObject(response);
                    finish = jObject.getBoolean("is_full");
                    jObject = jObject.getJSONObject("output");
                    jObject =  jObject.getJSONArray("generic").getJSONObject(0);
                    Log.i("json", jObject.toString());
                    chatView.addMessage(new ChatMessage(jObject.getString("text"), System.currentTimeMillis(), ChatMessage.Type.RECEIVED, "챗봇"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("LOG_RESPONSE", error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return rBody == null ? null : rBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", rBody, "utf-8");
                    return null;
                }
            }
        };

        queue.add(stringRequest);
    }


    protected void getSessionId() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url+"api/session",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("response", response);

                        JSONObject jObject = null;
                        try {
                            jObject = new JSONObject(response);
                            JSONObject result = jObject.getJSONObject("result");
                            sessionId = result.getString("session_id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("response", sessionId);
                        sendToBot(true, "");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("request", error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    protected void finishChat(){
        JSONObject jsonObject = new JSONObject();
        String user = User.getInstance().getId();
        String content;
        try {
            jsonObject.put("user_id", user);
            jsonObject.put("session_id", sessionId);
        } catch (Exception e) {
            Log.e("json error", e.getMessage());
        }

        String rBody = jsonObject.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"api/finish",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jObject = null;
                        ArrayList<Double> days = null;
                        Diary diary = Diary.getinstance();
                        diary.clear();
                        try {
                            jObject = new JSONObject(response);
                            diary.setText(jObject.getString("text"));
                            diary.setIntent(jObject.getString("intent"));
                            diary.setWhat(jObject.getString("what"));
                            diary.setWhere(jObject.getString("where"));
                            diary.setHow(jObject.getString("how"));
                            diary.setWho(jObject.getString("who"));
                            diary.setWhy(jObject.getString("why"));
                            diary.setWhen(jObject.getString("when"));
                            Date d = new Date();
                            diary.setDate(d.getYear()+1900, d.getMonth()+1, d.getDate());
                            Intent intent = new Intent(context, ResultActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Log.e("date error", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("request", "finishError");
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return rBody == null ? null : rBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", rBody, "utf-8");
                    return null;
                }
            }
        };
        queue.add(stringRequest);
    }
}
