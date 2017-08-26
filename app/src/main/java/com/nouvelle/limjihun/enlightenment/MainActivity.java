package com.nouvelle.limjihun.enlightenment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity  implements TextToSpeech.OnInitListener{

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject, btnToggleCamera;
    private ImageView imageViewResult;
    private CameraView cameraView;
    private TextView textLincense;
    private TextView textKRViewResult;

    private TextToSpeech myTTS;

    private Boolean isUseSound = true;
    private Boolean isUseNarration = true;
    private Boolean isUseWDSound = true;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_setting) {
            Intent SettingActivity = new Intent(this, SettingsActivity.class);
            startActivity(SettingActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref = getSharedPreferences("settings", 0);

        isUseSound = pref.getBoolean("useSound", true);
        isUseNarration = pref.getBoolean("useNarration", true);
        isUseWDSound = pref.getBoolean("useWDSound", true);

//        Toast.makeText(MainActivity.this, "isUseSound : "+ isUseSound, Toast.LENGTH_LONG).show();

        getSupportActionBar().setElevation(200);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.pastel);


        cameraView = (CameraView) findViewById(R.id.cameraView);
        imageViewResult = (ImageView) findViewById(R.id.imageViewResult);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());
        //btnToggleCamera = (Button) findViewById(R.id.btnToggleCamera);
        btnDetectObject = (Button) findViewById(R.id.btnDetectObject);
        textLincense = (TextView) findViewById(R.id.license);
        textKRViewResult = (TextView) findViewById(R.id.textKRViewResult);
        myTTS = new TextToSpeech(this, this);

        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                super.onPictureTaken(picture);
                String tempstr = "";
                //찍은 사진을 비트맵으로 변환
                Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                bitmap = imgRotate(bitmap);
                Log.i("debug_jh", "회전 지점" + bitmap.getWidth() + " / " + bitmap.getHeight());
                //224 x 224 크기로 변경
                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
                //크기가 변경된 사진을 설정

                imageViewResult.setImageBitmap(bitmap);
                //이해가 안감 --
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                //Log.i("debug_jh", "result 결과값 : " + results.toString());
                int k = 0;
                for (Classifier.Recognition temp : results) {
                    tempstr += temp.toEnString(k++);
                    tempstr += "\n";
                }
                textViewResult.setText((tempstr));

                    if (results.isEmpty() != true) {
                        textKRViewResult.setText(results.get(0).toKRString());
                        if (isUseSound && isUseWDSound)
                            ttsForWord(results.get(0).toKRString());
                    } else {
                        textKRViewResult.setText("다시 한번 찍어주세요.");
                        if (isUseSound && isUseWDSound)
                            ttsForWord("물체를 인식하지 못했어요. 다시한번 찍어주세요.");
                    }

            }
        });

        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
                Log.i("kaka", "0번 지점");
            }
        });

//        btnToggleCamera.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                cameraView.toggleFacing();
//                Log.i("kaka", "00번 지점");
//            }
//        });

        textLincense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context mContext = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

                //R.layout.dialog는 xml 파일명이고  R.id.popup은 보여줄 레이아웃 아이디
                View layout = inflater.inflate(R.layout.opensource_license, (ViewGroup) findViewById(R.id.popup));
                AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);

                aDialog.setTitle("오픈소스 라이선스");
                aDialog.setView(layout);

//                //그냥 닫기버튼을 위한 부분
//                aDialog.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                    }
//                });
                //팝업창 생성
                AlertDialog ad = aDialog.create();
                ad.show();

            }
        });

        initTensorFlowAndLoadModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
        myTTS.shutdown();
    }

    private void initTensorFlowAndLoadModel(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                    makeButtonVisible();
                } catch (final Exception e){
                    throw new RuntimeException("Error", e);
                }
            }
        });
    }

    private void makeButtonVisible(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }

    private Bitmap imgRotate(Bitmap bmp){

        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        bmp.recycle();

        return resizedBitmap;
    }

    /*
     * tts override method
     */
    @Override
    public void onInit(int status) {
        if (isUseSound && isUseNarration) {
            String spk_intro = "안녕하세요. 물체의 사진을 찍으면 해당 물체의 단어를 알려주는 학습용 어플입니다. 해당 내레이션은 왼쪽 메뉴 탭에서 설정할 수 있습니다.";
            myTTS.speak(spk_intro, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsForWord(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        myTTS.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }






}
