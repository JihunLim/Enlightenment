package com.nouvelle.limjihun.enlightenment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity  implements TextToSpeech.OnInitListener {

    private static int INPUT_SIZE = 224;//224
    private static int IMAGE_MEAN = 117;//117
    private static float IMAGE_STD = 1;//1
    private static String INPUT_NAME = "input";//input
    private static String OUTPUT_NAME = "output";//output
    private static int MODE = 0; //Inception v3 model

    /*
    *MODEL_FILE, LABEL_FILE are GOOGLE Inception v3 image recognition model & label -> MODE 1
    *CUSTOM_MODEL_FILE, CUSTOM_LABEL_FILE  are Custom image recognition model & label about stuffs in the Korean house -> MODE 2
     */

    private static String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";

    private static final String CUSTOM_MODEL_FILE = "file:///android_asset/dreamcatcher_v3.pb";
    private static final String CUSTOM_LABEL_FILE = "file:///android_asset/dreamcatcher_v3_labels.txt";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject, btnToggleCamera;
    private ImageView imageViewResult;
    private CameraView cameraView;
    private TextView textLincense;
    private TextView textKRViewResult;

    private TextToSpeech myTTS;
    SharedPreferences pref;
    private Boolean isUseSound = true;
    private Boolean isUseNarration = true;
    private Boolean isUseWDSound = true;
    private int whichLanguage;
    private int whichMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        unpackZip("file:///android_asset/", "dreamcatcher_v3.zip");
        ActionBar actionBar = getSupportActionBar();

        // Custom Actionbar를 사용하기 위해 CustomEnabled을 true 시키고 필요 없는 것은 false 시킨다
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);            //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false);        //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false);            //홈 아이콘을 숨김처리합니다.

        //layout을 가지고 와서 actionbar에 포팅을 시킵니다.
        View mCustomView = LayoutInflater.from(this).inflate(R.layout.actionbar, null);
        actionBar.setCustomView(mCustomView);

        Toolbar parent = (Toolbar) mCustomView.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(255, 255, 255, 255)));

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(mCustomView, params);

        // 액션바에 백그라운드 이미지를 아래처럼 입힐 수 있습니다. (drawable 폴더에 img_action_background.png 파일이 있어야 겠죠?)
        //actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.img_action_background));

        //showNotiPannel = false;
        ImageButton btn_back = (ImageButton) findViewById(R.id.exit);
        ImageButton btn_setting = (ImageButton) findViewById(R.id.setting);

        btn_back.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //종료
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }
        );

        btn_setting.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //설정
                        Intent SettingActivity = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(SettingActivity);
                    }
                }
        );

        pref = getSharedPreferences("settings", 0);

        isUseSound = pref.getBoolean("useSound", true);
        isUseNarration = pref.getBoolean("useNarration", true);
        isUseWDSound = pref.getBoolean("useWDSound", true);
        whichLanguage = pref.getInt("language_preference", 0);
        whichMode = pref.getInt("mode_preference", 0);

        changeSetting(whichLanguage, whichMode); //초기 설정값에 따라 설정해줌

        cameraView = (CameraView) findViewById(R.id.cameraView);
        imageViewResult = (ImageView) findViewById(R.id.imageViewResult);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());
        //btnToggleCamera = (Button) findViewById(R.id.btnToggleCamera);
        btnDetectObject = (Button) findViewById(R.id.btnDetectObject);
        //textLincense = (TextView) findViewById(R.id.license);
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

                isUseWDSound = pref.getBoolean("useWDSound", true);
                if (results.isEmpty() != true) {
                    textKRViewResult.setText(results.get(0).toKRString());
                    if (isUseSound && isUseWDSound)
                        ttsForWord(results.get(0).toKRString());
                } else {
                    if (whichLanguage == 0) {
                        textKRViewResult.setText("다시 한번 찍어주세요.");
                        if (isUseSound && isUseWDSound)
                            ttsForWord("물체를 인식하지 못했어요. 다시한번 찍어주세요.");
                    } else {
                        textKRViewResult.setText("Please take a picture, again.");
                        if (isUseSound && isUseWDSound)
                            ttsForWord("I can not recognize object. Please take a picture, again.");
                    }
                }
            }
        });

        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
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

    private void initTensorFlowAndLoadModel() {
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
                } catch (final Exception e) {
                    throw new RuntimeException("Error", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }

    private Bitmap imgRotate(Bitmap bmp) {

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
        String spk_intro;
        if (isUseSound && isUseNarration) {
            if (whichLanguage == 0) {
                myTTS.setLanguage(Locale.KOREA);
                spk_intro = "안녕하세요. 물체의 사진을 찍으면 해당 물체의 단어를 알려주는 학습용 어플입니다.  촬영버튼은 스마트폰 맨 밑부분에 있으며, 물체와 적절한 거리를 유지한 상태로 촬영해 주시기 바랍니다. 해당 내레이션은 설정에서 해재할 수 있습니다.";
            } else {
                myTTS.setLanguage(Locale.US);
                spk_intro = "This is the application for studying word of object you want to know. Picture button is below the phone, and take a picture at proper distance for object. You can turn off the Narration mode in setting menu.";
            }

            myTTS.speak(spk_intro, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsForWord(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        if (whichLanguage == 0)
            myTTS.setLanguage(Locale.KOREA);
        else
            myTTS.setLanguage(Locale.US);
        myTTS.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    public static void changeLanguage(int i) {
        //0 -> Korean, 1 -> English
        if (i == 1) {
            if (MODE == 0)
                LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";
            else
                LABEL_FILE = CUSTOM_LABEL_FILE;

        } else if (i == 0) {
            if (MODE == 0)
                LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings_kr.txt";
            else
                LABEL_FILE = "file:///android_asset/dreamcatcher_v3_labels_kr.txt";
        }
    }

    public static void changeMode(int i) {
        //0 -> inception v3, 1 -> custom model
        if (i == 1) {
            MODE = 1;
            MODEL_FILE = CUSTOM_MODEL_FILE;
            LABEL_FILE = CUSTOM_LABEL_FILE;
            INPUT_SIZE = 299;
            IMAGE_MEAN = 128;
            IMAGE_STD = 128.0f;
            INPUT_NAME = "Mul";
            OUTPUT_NAME = "final_result";
            TensorFlowImageClassifier.THRESHOLD = 0.7f;
        } else if (i == 0) {
            MODE = 0;
            MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
            LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";
            INPUT_SIZE = 224;//224
            IMAGE_MEAN = 117;//117
            IMAGE_STD = 1;//1
            INPUT_NAME = "input";
            OUTPUT_NAME = "output";
            TensorFlowImageClassifier.THRESHOLD = 0.1f;
        }
    }

    private static void changeSetting(int lang, int mode) {
        Log.i("debug", "lang , mode -> " + lang + " , " + mode);
        changeMode(mode);
        changeLanguage(lang);
    }

    private boolean unpackZip(String path, String zipname)
    {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    Toast.makeText(this, "압축 위치 : "+path+" ,"+filename, Toast.LENGTH_LONG).show();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
