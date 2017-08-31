package com.nouvelle.limjihun.enlightenment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by LimJiHun on 2017-08-23.
 * Preferrence Settings
 */

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{


    SharedPreferences setting;
    SharedPreferences.Editor editor;

    private PreferenceScreen screen;
    private CheckBoxPreference bool_useSound;
    private  SwitchPreference bool_useNarration;
    private  SwitchPreference bool_useWDSound;
    private Preference bool_openSource;
    private Preference bool_appInfo;
    private ListPreference language_preference;
    private ListPreference mode_preference;

    public static boolean isUseSound = true;
    public static boolean isUseNarration = true;
    public static boolean isUseWDSound = true;
    public static String language;
    public static String mode;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
        screen = getPreferenceScreen();

        setting = getSharedPreferences("settings", 0);
        editor = setting.edit();

        bool_useSound = (CheckBoxPreference) screen.findPreference("useSound");
        bool_useNarration = (SwitchPreference) screen.findPreference("useNarration");
        bool_useWDSound = (SwitchPreference) screen.findPreference("useWDSound");
        bool_openSource = (Preference) findPreference("opensource");
        bool_appInfo = (Preference) findPreference("appInfo");
        language_preference = (ListPreference) screen.findPreference("language_preference");
        mode_preference = (ListPreference) screen.findPreference("mode_preference");

        bool_useSound.setOnPreferenceChangeListener(this);
        bool_useNarration.setOnPreferenceChangeListener(this);
        bool_useWDSound.setOnPreferenceChangeListener(this);
        language_preference.setOnPreferenceChangeListener(this);
        mode_preference.setOnPreferenceChangeListener(this);
//        bool_openSource.setOnPreferenceClickListener(SettingsActivity.this);



        bool_openSource.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.i("papa", "preference 값 : " + preference);
                if(preference == bool_openSource){
//                    Toast.makeText(SettingsActivity.this, "네번째꺼 변경됨", Toast.LENGTH_LONG).show();
                    Context mContext = getApplicationContext();
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

                    //R.layout.dialog는 xml 파일명이고  R.id.popup은 보여줄 레이아웃 아이디
                    View layout = inflater.inflate(R.layout.opensource_license, (ViewGroup) findViewById(R.id.popup));
                    AlertDialog.Builder aDialog = new AlertDialog.Builder(SettingsActivity.this);
                    aDialog.setTitle("오픈소스 라이선스");
                    aDialog.setView(layout);

                //그냥 닫기버튼을 위한 부분
                aDialog.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                    //팝업창 생성
                    AlertDialog ad = aDialog.create();
                    ad.show();
                }
                return true;
            }
        });

        bool_appInfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.i("papa", "preference 값 : " + preference);
                if(preference == bool_appInfo){
                    //Toast.makeText(SettingsActivity.this, "다섯번째꺼 변경됨", Toast.LENGTH_LONG).show();
                    Context mContext = getApplicationContext();
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

                    //R.layout.dialog는 xml 파일명이고  R.id.popup은 보여줄 레이아웃 아이디
                    View layout = inflater.inflate(R.layout.appinfo, (ViewGroup) findViewById(R.id.popup2));
                    AlertDialog.Builder aDialog = new AlertDialog.Builder(SettingsActivity.this);
                    aDialog.setTitle("어플리케이션 정보");
                    aDialog.setView(layout);

                    //그냥 닫기버튼을 위한 부분
                    aDialog.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    //팝업창 생성
                    AlertDialog ad = aDialog.create();
                    ad.show();
                }
                return true;
            }
        });


    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.i("kaka", "preference : " + preference + ", newValue : " + newValue);

        //String value = (String) newValue;
        if(preference == bool_useSound){
            isUseSound = (boolean)newValue;
            editor.putBoolean("useSound", isUseSound);
//            Toast.makeText(SettingsActivity.this, "첫번째꺼 변경됨" + isUseSound, Toast.LENGTH_LONG).show();
        }else if(preference == bool_useNarration){
            isUseNarration = (boolean)newValue;
            editor.putBoolean("useNarration", isUseNarration);
//            Toast.makeText(SettingsActivity.this, "두번째꺼 변경됨", Toast.LENGTH_LONG).show();
        }else if(preference == bool_useWDSound){
            isUseWDSound = (boolean)newValue;
            editor.putBoolean("useWDSound", isUseWDSound);
//            Toast.makeText(SettingsActivity.this, "세번째꺼 변경됨", Toast.LENGTH_LONG).show();
        }else if(preference == language_preference){
            language = (String)newValue;
            editor.putInt("language_preference", Integer.parseInt(language));
            //MainActivity.changeLanguage(Integer.parseInt(language));
            Toast.makeText(SettingsActivity.this, "다시 시작해야 설정이 적용됩니다.", Toast.LENGTH_LONG).show();
        }else if(preference == mode_preference){
            mode = (String)newValue;
            //MainActivity.changeMode(Integer.parseInt(mode));
            editor.putInt("mode_preference", Integer.parseInt(mode));
            Toast.makeText(SettingsActivity.this, "다시 시작해야 설정이 적용됩니다.", Toast.LENGTH_LONG).show();
        }
        editor.commit();
        return true;
    }

//    public static void updateSettings(){
//        isUseSound = bool_useSound.isChecked();
//    }



}
