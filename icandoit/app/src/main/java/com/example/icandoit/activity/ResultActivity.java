package com.example.icandoit.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.icandoit.R;
import com.example.icandoit.model.Diary;
import com.example.icandoit.model.User;
import com.herok.doodle.Manuscript;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;


import static com.example.icandoit.R.id.manuscript;
import static java.lang.Thread.sleep;

public class ResultActivity extends AppCompatActivity {
    LinearLayout container;
    Toolbar toolbar;
    private AppCompatActivity activity = this;
    private static final int REQUEST_GALLERYY = 200;
    private ImageView selectImg;
    private Manuscript content;
    private Diary diary = Diary.getinstance();
    RequestQueue queue;
    private LinearLayout linearLayout;
    private String prevImage;
    private String prevText;
    private int diaryNum = -1;
    private int diaryCnt = 0;
    private String diaryText = "";
    private ConstraintLayout constraintLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reusult);
        queue = Volley.newRequestQueue(this);
        container= findViewById(R.id.container);
        content = findViewById(manuscript);
        selectImg = (ImageView) findViewById(R.id.selectImg);

        prevText = diary.getText();
        prevImage = diary.getImage();

        String text = diary.getText();
        int length = text.length();
        StringBuilder sb = new StringBuilder();
        sb.append(text);
        while(length++ < 60) {
            sb.append(" ");
        }
        text = sb.toString();
        //뷰 수정
        content.setText(text);
        if (diary.getImage() != null) {
            selectImg.setPadding(0,0,0,0);
            selectImg.setImageBitmap(diary.getBitmap());

        } else {
            selectImg.setPadding(150,150,150,150);
            selectImg.setImageResource(R.drawable.add_image);
        }

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("사진 모드 선택");
                builder.setItems(R.array.items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which)
                        {
                            case 0:
                                Intent intent = new Intent(ResultActivity.this,DeepArtActivity.class);
                                startActivity(intent);
                                break;

                            case 1:
                                Intent intent2 = new Intent(ResultActivity.this, DrawActivity.class);
                                startActivity(intent2);
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
//            public void onClick(View v) {
//                Intent intent = new Intent(ResultActivity.this,DeepArtActivity.class);
//                startActivity(intent);
//            }
//        });

        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edittext = new EditText(activity);
                edittext.setText(diary.getText());
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("일기 수정");
                builder.setView(edittext);
                builder.setPositiveButton("입력",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                diary.setText(edittext.getText().toString());
                                content.setText(diary.getText());
                            }
                        });
                builder.setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (diary.getImage() != null) {
            selectImg.setPadding(0,0,0,0);
            selectImg.setImageBitmap(diary.getBitmap());

        } else {
            selectImg.setPadding(150,150,150,150);
            selectImg.setImageResource(R.drawable.add_image);
        }
    }

    //액션버튼 메뉴 액션바에 집어 넣기
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        String pImage = prevImage == null ? "":prevImage;
        String dImage = diary.getImage() == null ? "":diary.getImage();
        if (!pImage.equals(dImage) || !prevText.equals(diary.getText())) {
            Log.i("dif", "달라");
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("바꾼 내용을 저장 안했어요!");
            builder.setMessage("종료할까요?");
            builder.setPositiveButton("예",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.setNegativeButton("아니요",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
            builder.show();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                String pImage = prevImage == null ? "":prevImage;
                String dImage = diary.getImage() == null ? "":diary.getImage();
                if (!pImage.equals(dImage) || !prevText.equals(diary.getText())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("바꾼 내용을 저장 안했어요!");
                    builder.setMessage("종료할까요?");
                    builder.setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    builder.setNegativeButton("아니요",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.show();
                } else {
                    finish();
                }
                return true;

            }
            case R.id.menu_save: {
                JSONObject jsonObject = new JSONObject();
                String user = User.getInstance().getId();

                try {
                    jsonObject.put("user_id", user);
                    jsonObject.put("year", diary.getDate().substring(0,4));
                    jsonObject.put("month", diary.getDate().substring(5,7));
                    jsonObject.put("date", diary.getDate().substring(8,10));
                    jsonObject.put("image", diary.getImage());
                    jsonObject.put("content", diary.getText());
                } catch (Exception e) {
                    Log.e("json error", e.getMessage());
                }

                String rBody = jsonObject.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, ChatActivity.url+"api/update",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                prevImage = diary.getImage();
                                prevText = diary.getText();
                                Toast.makeText(getApplicationContext(),"저장했습니다!", Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"저장 실패", Toast.LENGTH_SHORT).show();
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
                return true;
            }
            case R.id.menu_capture: {
                String folder = "Test_Directory"; // 폴더 이름
                try {
                    container.setDrawingCacheEnabled(true);
                    container.buildDrawingCache(true);

                    Bitmap captureView = Bitmap.createBitmap(container.getMeasuredWidth(), container.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                    Canvas screenShotCanvas = new Canvas(captureView );
                    container.draw(screenShotCanvas);

                    // 현재 날짜로 파일을 저장하기
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                    // 년월일시분초
                    Date currentTime_1 = new Date();
                    String dateString = formatter.format(currentTime_1);

                    System.out.println(container.getId());
                    System.out.println(captureView);
                    MediaStore.Images.Media.insertImage(getContentResolver(), captureView, dateString, "hello");
                    Toast.makeText(getApplicationContext(), dateString + ".jpg 저장",
                            Toast.LENGTH_SHORT).show();
                    container.setDrawingCacheEnabled(false);

                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("Screen", "" + e.toString());
                }
                return true;
            }
            case R.id.menu_delete: {
                JSONObject jsonObject = new JSONObject();
                String user = User.getInstance().getId();

                try {
                    jsonObject.put("user_id", user);
                    jsonObject.put("year", diary.getDate().substring(0,4));
                    jsonObject.put("month", diary.getDate().substring(5,7));
                    jsonObject.put("date", diary.getDate().substring(8,10));
                } catch (Exception e) {
                    Log.e("json error", e.getMessage());
                }

                String rBody = jsonObject.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, ChatActivity.url+"api/delete",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(getApplicationContext(),"삭제 했습니다!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"삭제 실패", Toast.LENGTH_SHORT).show();
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
                return true;
            }
            case R.id.menu_reload: {
                String[] list = diary.getQuery();
                diaryNum = list.length;
                diaryCnt = 0;
                diaryText = "";
                Toast.makeText(getApplicationContext(),"기다려요", Toast.LENGTH_LONG).show();
                for (String text : list) {
                    System.out.println(text);
                    getDiary(text);
                    try
                    {
                        sleep(1500);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                return true;
            }
            case R.id.background: {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("일기 배경 선택");
                builder.setItems(R.array.background_items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                linearLayout=findViewById(R.id.container);
                                linearLayout.setBackgroundResource(R.drawable.background_mint);
                                toolbar.setBackgroundResource(R.drawable.background_mint);
                                break;

                            case 1:
                                linearLayout=findViewById(R.id.container);
                                linearLayout.setBackgroundResource(R.drawable.background_brown);
                                toolbar.setBackgroundResource(R.drawable.background_brown);
                                break;
                            case 2:
                                linearLayout=findViewById(R.id.container);
                                linearLayout.setBackgroundResource(R.drawable.background_gradation);
                                toolbar.setBackgroundResource(R.drawable.background_gradation);
                                break;
                            case 3:
                                linearLayout=findViewById(R.id.container);
                                linearLayout.setBackgroundResource(R.drawable.background_blue);
                                toolbar.setBackgroundResource(R.drawable.background_blue);

                                break;
                            case 4:
                                linearLayout=findViewById(R.id.container);
                                linearLayout.setBackgroundResource(R.drawable.background_gray);
                                toolbar.setBackgroundResource(R.drawable.background_gray);


                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public void makeDiary(String text) {
        diaryText += " "+text+".";
        diaryCnt++;
        System.out.println(diaryCnt+" "+diaryNum);
        System.out.println(diaryText);
        if (diaryCnt == diaryNum) {
            diary.setText(diaryText);
            content.setText(""+diary.getText());
            content.setText(""+diary.getText());
            Handler timerHandler = new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Log.i("handerl","2");
                    content.setText(diary.getText());
                    onStart();
                }
            };

            timerHandler.postDelayed(r, 2000);
            timerHandler.postDelayed(r, 1000);
        }
        onStart();
    }

    public void getDiary(String text) {
        final String url = "http://3.35.104.72:5000/diary/";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url+text,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                            String str = jsonObject.getString("request");
                            System.out.println(str);
                            makeDiary(str);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_GALLERYY && resultCode == RESULT_OK && data != null && data.getData() != null) {
//
//            Uri selectedImageUri = data.getData();
//            selectImg.setImageURI(selectedImageUri);
//
//        }
//    }
}