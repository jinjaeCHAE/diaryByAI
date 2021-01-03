package com.example.icandoit.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.icandoit.R;
import com.example.icandoit.adapter.DragData;
import com.example.icandoit.adapter.DrawingAdapter;
import com.example.icandoit.model.Diary;
import com.example.icandoit.model.drawing;
import com.munon.turboimageview.MultiTouchObject;
import com.munon.turboimageview.TurboImageView;
import com.munon.turboimageview.TurboImageViewListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DrawActivity extends AppCompatActivity implements TurboImageViewListener {
    final String[] draws = {"항공모함", "비행기", "자명종 시계", "구급차", "천사", "개미", "모루", "사과", "팔", "아스파라거스",
            "도끼", "배낭", "바나나", "붕대", "헛간", "야구공", "야구 배트", "바구니", "농구공", "박쥐", "욕조", "해변",
            "곰", "수염", "침대", "꿀벌", "허리띠","벤치","자전거","쌍안경", "새", "케이크", "블랙베리", "블루베리", "책", "부메랑", "병뚜껑", "넥타이",
            "팔찌", "뇌", "빵", "다리", "브로콜리", "빗자루", "양동이", "불도저", "버스", "수풀", "나비", "선인장", "계산기", "달력", "낙타",
            "카메라", "캠프파이어", "촛불", "대포", "카누", "자동차", "당근", "성곽", "고양이", "선풍기", "첼로", "휴대폰", "의자", "샹들리에", "교회", "원",
            "클라리넷", "시계", "구름", "커피잔", "나침반", "컴퓨터", "쿠키", "쿨러", "카우치", "소", "게", "크레파스", "크레용", "악어", "왕관", "유람선", "컵",
            "다이아몬드", "식기세척기", "다이빙대", "개", "돌고래", "도넛", "문", "용", "옷", "드릴", "북", "오리", "아령", "귀", "팔꿈치", "코끼리", "봉투",
            "지우개", "눈", "안경", "얼굴", "부채", "깃털", "울타리", "손가락", "소화전", "벽난로", "소방차", "물고기", "플라밍고", "손전등", "램프",
            "꽃", "발", "갈림길", "개구리", "프라이팬", "정원", "호스", "기린", "골프 클럽", "포도", "풀밭", "기타", "햄버거", "망치", "손", "하프", "모자",
            "헤드폰", "고슴도치", "헬리콥터", "헬멧", "육각형", "말", "병원", "열기구", "핫도그", "욕조", "모래시계", "집", "화초", "허리케인", "아이스크림",
            "재킷", "교도소", "캥거루", "키", "키보드", "무릎", "칼", "등불", "노트북", "잎", "다리", "전구", "등대", "번개", "사자", "립스틱", "가재", "막대사탕", "우편함", "지도",
            "마커", "성냥", "메가폰", "인어공주", "마이크", "전자레인지", "원숭이", "달", "모기", "오토바이", "산",
            "쥐","너구리","무전기", "비","레인보우","갈퀴","리모콘","코뿔소","총","강","롤러코스터","롤러스케이트","배","샌드위치","실","색소폰","버스","가위","전갈","드라이버",
            "거북","시소","상어","양","신발","삽","반바지","싱크대","보드","해골","건물","침낭","웃는얼굴","댈팽이","뱀","스노쿨링","눈송이","눈사람","축구공","양말",
            "제트보트","거미","숟가락","스프레드시트","네모","다람쥐","계단","물결","별","스테이크","스테레오","청진기","흉터","정지","난로","딸기","가로등","완두콩",
            "잠수함","캐리어","태양","백조","스웨터","그네","칼","주사기","식탁","주전자","곰인형","전화기","텔레비전","라켓","텐트","에펠탑","만리장성","모나리자","호랑이",
            "토스터","발가락","변기","칫솔","치약","태풍","트랙터","신호등","기차","나무","삼각형","트럼펫","트럭","티셔츠","우산","속옷","꽃병","바이올린","세탁기",
            "수박","미끄럼틀","고래","풍차","바퀴","와인잔","손목시계","요가","얼룩말","지그재그","콧수염","입","머그잔","버섯","못","목걸이","코","바다","팔각형","문어","양파",
            "오븐","올빼미","붓","페인트통" ,"야자수","팬더","바지","클립","낙하산","앵무새","여권","땅콩","펭귄","먹는 배","연필","피아노","작은트럭","액자","돼지","베개","파인애플",
            "피자","뻰치","경찰차","못","엽서","당구","감자","전원","지갑","토끼"
    };
    private static final String TAG = "DrawActivity";
    private ArrayList<String> titles = new ArrayList<>();
    Diary diary = Diary.getinstance();
    int index = 0;
    private RelativeLayout dragView;
    private RecyclerView recyclerView;
    private List<drawing> listModalList;
    private DrawingAdapter drawingAdapter;
    private ImageView imageView;
    private RequestQueue queue;
    private Context context = this;
    private TurboImageView turboImageView;
    private LinearLayout ll = null;
    Toolbar toolbar;
    int cnt=0;
//    int X,Y,Height,Width;
//    private movingUnit MU;

    int standardSize_X, standardSize_Y;
    float density;

    final String url = "http://3.35.104.72:5000/draw/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        ll = (LinearLayout)findViewById(R.id.draw);
//        dragView.getLayoutParams().height = (int)(ll.getLayoutParams().height/2);
        titles.add("사람");
        turboImageView = (TurboImageView)findViewById(R.id.turboImageView);
        turboImageView.setListener(this);
        toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        queue = Volley.newRequestQueue(this);
        addTitles(diary.getText());
        for (; index < titles.size(); index++) {
            getImage(titles.get(index), index);
        }
        getStandardSize();
        initComponents();
    }

    public Point getScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return  size;
    }

    public void getStandardSize() {
        Point ScreenSize = getScreenSize(this);
//        density  = getResources().getDisplayMetrics().density;
        density = 1;
        standardSize_X = (int) (ScreenSize.x / density);
        standardSize_Y = (int) (ScreenSize.y / density);
    }

    //액션버튼 메뉴 액션바에 집어 넣기
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_draw, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;

            }
            case R.id.deselect:{
                turboImageView.deselectAll();
                return true;
            }
            case R.id.flip:{
                System.out.println("flip");
                turboImageView.toggleFlippedHorizontallySelectedObject();
                return true;
            }
            case R.id.menu_back: {
                System.out.println("back");
                turboImageView.removeSelectedObject();
                return true;
            }
            case R.id.menu_save: {
                String folder = "Test_Directory"; // 폴더 이름
                try {
                    dragView.setDrawingCacheEnabled(true);
                    dragView.buildDrawingCache(true);

                    Bitmap captureView = Bitmap.createBitmap(dragView.getMeasuredWidth(), dragView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                    Canvas screenShotCanvas = new Canvas(captureView );
                    dragView.draw(screenShotCanvas);

                    // 현재 날짜로 파일을 저장하기
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                    // 년월일시분초
                    Date currentTime_1 = new Date();
                    String dateString = formatter.format(currentTime_1);

                    System.out.println(dragView.getId());
                    System.out.println(captureView);
                    MediaStore.Images.Media.insertImage(getContentResolver(), captureView, dateString, "hello");
                    Toast.makeText(getApplicationContext(), dateString + ".jpg 저장",
                            Toast.LENGTH_SHORT).show();
                    dragView.setDrawingCacheEnabled(false);
                    Intent intent = new Intent(context, DeepArtActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("Screen", "" + e.toString());
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void addTitles(String text) {
        for (String draw : draws) {
            if (text.contains(draw)) {
                titles.add(draw);
            }
        }
    }

    private void initComponents() {
        dragView = findViewById(R.id.imgcontainer);
        recyclerView = findViewById(R.id.listing);
        imageView = findViewById(R.id.drawView);
        listModalList = new ArrayList<>();

        dragView.getLayoutParams().height = (int)(standardSize_Y/2);
        recyclerView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);
        initContainer();
    }

    private void setDataToAdapter(int index, Bitmap bitmap) {
        drawing listModal = new drawing();
        listModal.setVehiclename(titles.get(index));
        listModal.setVehicleimage(bitmap);
        listModalList.add(listModal);
        cnt++;
        if (cnt == titles.size()) {
            drawingAdapter = new DrawingAdapter(listModalList);
            recyclerView.setAdapter(drawingAdapter);
        }
    }

    private void initContainer() {
        dragView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {

                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        dragView.setBackgroundColor(Color.GREEN);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        dragView.setBackgroundColor(Color.RED);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        dragView.setBackgroundColor(Color.WHITE);
                        break;
                    case DragEvent.ACTION_DROP:
                        final float dropX = dragEvent.getX();
                        final float dropY = dragEvent.getY();
                        final DragData state = (DragData) dragEvent.getLocalState();

                        turboImageView.addObject(context,state.item.getVehicleimage());

//                        setImgIntoContainer(state.item,imageView);
//                        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                        param.leftMargin = (int)(dropX - state.width / 2); //XCOORD
//                        param.topMargin = (int)(dropY - state.height / 2); //YCOORD
//                        imageView.setLayoutParams(param);
                        turboImageView.bringToFront();
//                        imageView.getLayoutParams().width = state.width;
//                        imageView.getLayoutParams().height = state.height;
//                        dragView.addView(imageView);
//                        setSelectedImage(imageView);
                        break;

                    default:
                        break;
                }
                return true;
            }
        });
    }



    private void setImgIntoContainer(drawing listModal,ImageView shape) {
        shape.setImageBitmap(listModal.getVehicleimage());
    }


    private void getImage(String text, int index) {
        try {
            String encode = URLEncoder.encode(text, "UTF-8");
            System.out.println(url+encode);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url+encode,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("LOG_RESPONSE", response);
                            JSONObject jObject = null;
                            JSONArray jarray = null;
                            ArrayList<Double> days = null;
                            try {
                                jObject = new JSONObject(response);
                                String base64 = jObject.getString("request");
                                byte[] bImage = Base64.decode(base64, 0);
                                ByteArrayInputStream bais = new ByteArrayInputStream(bImage);
                                Bitmap bm = BitmapFactory.decodeStream(bais);
                                setDataToAdapter(index, bm);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageObjectSelected(MultiTouchObject multiTouchObject) {
        Log.d(TAG, "image object selected");
    }

    @Override
    public void onImageObjectDropped() {
        Log.d(TAG, "image object dropped");
    }

    @Override
    public void onCanvasTouched() {
        turboImageView.deselectAll();
        Log.d(TAG, "canvas touched");
    }

}
