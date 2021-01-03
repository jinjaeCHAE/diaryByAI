package com.example.icandoit.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.bumptech.glide.Glide;
import com.deeparteffects.sdk.android.DeepArtEffectsClient;
import com.deeparteffects.sdk.android.model.Result;
import com.deeparteffects.sdk.android.model.Styles;
import com.deeparteffects.sdk.android.model.UploadRequest;
import com.deeparteffects.sdk.android.model.UploadResponse;
import com.example.icandoit.R;
import com.example.icandoit.adapter.StyleAdapter;
import com.example.icandoit.model.Diary;
import com.example.icandoit.utils.ImageHelper;
import com.example.icandoit.utils.SubmissionStatus;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.amazonaws.regions.Regions.EU_WEST_1;

public class DeepArtActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String API_KEY = "DkE1Gszcx96TFkj9afQBt35x56l1ACnq94XWtETT";
    private static final String ACCESS_KEY = "AKIA3XE3HF7SW6ZVGTO6";
    private static final String SECRET_KEY = "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";

    private static final int REQUEST_GALLERY = 100;
    private static final int CHECK_RESULT_INTERVAL_IN_MS = 2500;
    private static final int IMAGE_MAX_SIDE_LENGTH = 768;

    private AppCompatActivity mActivity;
    private Bitmap mImageBitmap;
    private TextView mStatusText;
    private ImageView mImageView;
    private ProgressBar mProgressbarView;
    private ImageButton saveButton;
    private ImageButton rotateButton;
    private boolean isProcessing = false;
    private Bitmap b;
    private RecyclerView recyclerView;
    private DeepArtEffectsClient deepArtEffectsClient;

    private Diary diary = Diary.getinstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art);

        mActivity = this;

        ApiClientFactory factory = new ApiClientFactory()
                .apiKey(API_KEY)
                .credentialsProvider(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
                    }

                    @Override
                    public void refresh() {
                    }
                }).region(EU_WEST_1.getName());
        deepArtEffectsClient = factory.build(DeepArtEffectsClient.class);

        mStatusText = findViewById(R.id.statusText);
        mProgressbarView = findViewById(R.id.progressBar);
        mImageView = findViewById(R.id.drawView);
        saveButton = findViewById(R.id.save_button);
        rotateButton = findViewById(R.id.rotate_button);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Button btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("click", "click");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivityForResult(intent, REQUEST_GALLERY);
//                }
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("click", "click");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                // 년월일시분초
                Date currentTime_1 = new Date();
                String dateString = formatter.format(currentTime_1);
                mImageView.setDrawingCacheEnabled(true);
                Bitmap b = mImageView.getDrawingCache();
                diary.setImage(b);
                MediaStore.Images.Media.insertImage(getContentResolver(), b, dateString, "hello");
                Toast.makeText(getApplicationContext(),"저장되었습니다!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (b == null) {
                    mImageView.setDrawingCacheEnabled(true);
                    b = mImageView.getDrawingCache();
                }
                Matrix rotate = new Matrix();
                rotate.postRotate(90);
                b = Bitmap.createBitmap(b, 0, 0,
                        b.getWidth(), b.getHeight(), rotate, false);
                mImageView.setImageBitmap(b);
                mImageView.setDrawingCacheEnabled(false);
            }
        });

        loadingStyles();
    }

    private void loadingStyles() {
        mStatusText.setText(R.string.loading);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Styles styles = deepArtEffectsClient.stylesGet();
                final StyleAdapter styleAdapter = new StyleAdapter(
                        getApplicationContext(),
                        styles,
                        new StyleAdapter.ClickListener() {
                            @Override
                            public void onClick(String styleId) {
                                if (!isProcessing) {
                                    if (mImageBitmap != null) {
                                        Log.d(TAG, String.format("Style with ID %s clicked.", styleId));
                                        isProcessing = true;
                                        mProgressbarView.setVisibility(View.VISIBLE);
                                        uploadImage(styleId);
                                    } else {
                                        Toast.makeText(mActivity, "Please choose a picture first",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(styleAdapter);
                        mProgressbarView.setVisibility(View.GONE);
                        mStatusText.setText("");
                    }
                });
            }
        }).start();
    }

    private class ImageReadyCheckTimer extends TimerTask {
        private String mSubmissionId;

        ImageReadyCheckTimer(String submissionId) {
            mSubmissionId = String.valueOf(submissionId);
        }

        @Override
        public void run() {
            try {
                final Result result = deepArtEffectsClient.resultGet(mSubmissionId);
                String submissionStatus = result.getStatus();
                Log.d(TAG, String.format("Submission status is %s", submissionStatus));
                if (submissionStatus.equals(SubmissionStatus.FINISHED)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(mActivity).load(result.getUrl()).centerCrop().crossFade().into(mImageView);
                            mProgressbarView.setVisibility(View.GONE);
                            mImageView.setDrawingCacheEnabled(true);
//                            Matrix rotate = new Matrix();
//                            rotate.postRotate(-90);
//                            b = Bitmap.createBitmap(b, 0, 0,
//                                    b.getWidth(), b.getHeight(), rotate, false);
                            mImageView.setVisibility(View.VISIBLE);
                            saveButton.setVisibility(View.VISIBLE);
                            rotateButton.setVisibility(View.VISIBLE);
                            mStatusText.setText("");
                        }
                    });
                    isProcessing = false;
                    cancel();
                }
            } catch (Exception e) {
                cancel();
            }
        }
    }

    private void uploadImage(final String styleId) {
        mStatusText.setText(R.string.uploading);
        Log.d(TAG, String.format("Upload image with style id %s", styleId));
        new Thread(new Runnable() {
            @Override
            public void run() {
                UploadRequest uploadRequest = new UploadRequest();
                uploadRequest.setStyleId(styleId);
                uploadRequest.setImageBase64Encoded(convertBitmapToBase64(mImageBitmap));
                UploadResponse response = deepArtEffectsClient.uploadPost(uploadRequest);
                String submissionId = response.getSubmissionId();
                Log.d(TAG, String.format("Upload complete. Got submissionId %s", response.getSubmissionId()));
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new ImageReadyCheckTimer(submissionId),
                        CHECK_RESULT_INTERVAL_IN_MS, CHECK_RESULT_INTERVAL_IN_MS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusText.setText(R.string.processing);
                    }
                });
            }
        }).start();
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        //Handle own activity result
        if (requestCode == REQUEST_GALLERY) {
            if (resultCode == RESULT_OK) {
                mImageBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(),
                        this.getContentResolver(), IMAGE_MAX_SIDE_LENGTH);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
