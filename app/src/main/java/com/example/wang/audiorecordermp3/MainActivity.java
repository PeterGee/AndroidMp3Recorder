package com.example.wang.audiorecordermp3;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.buihha.audiorecorder.Mp3Recorder;
import com.example.wang.audiorecordermp3.util.FileUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements Mp3Recorder.RecorderListener {

    Mp3Recorder mp3Recorder;

    private String path = Environment.getExternalStorageDirectory()
            + "/AudioRecorderMp3/recorder/";

    String[] permiss = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};
    int REQUEST_CODE = 1002;

    String filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!checkMyPermission(permiss)){
            ActivityCompat.requestPermissions(this,permiss,REQUEST_CODE);
        }
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(mp3Recorder == null){
                        filePath = path + FileUtils.getFileNameByTime() + ".mp3";
                        mp3Recorder = new Mp3Recorder(filePath);
                        mp3Recorder.setListener(MainActivity.this);
                    }
                    if(mp3Recorder.isRecording()){
                        mp3Recorder.pasueRecording();
                        ((Button)v).setText("Start Recorder");
                        Toast.makeText(MainActivity.this,"暂停录音!",Toast.LENGTH_SHORT).show();
                    }else{
                        mp3Recorder.startRecording();
                        ((Button)v).setText("Pasue Recorder");
                        Toast.makeText(MainActivity.this,"开始录音!",Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (mp3Recorder == null) {
                        Toast.makeText(MainActivity.this,"您还未开始录音呢!",Toast.LENGTH_SHORT).show();
                    } else {
                        mp3Recorder.stopRecording();
                        Toast.makeText(MainActivity.this,"结束录音!",Toast.LENGTH_SHORT).show();
                        ((Button)findViewById(R.id.start)).setText("Start Recorder");
                        mp3Recorder = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean checkMyPermission(String[] permiss){
        if(permiss !=null && permiss.length > 0 ){
            for(String per : permiss) {
                if (ContextCompat.checkSelfPermission(this,per) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == REQUEST_CODE) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                openAppDetails();
            }
        }
    }



    /**
     * 打开 APP 的详情设置
     */
    private void openAppDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("录音需要访问 “外部存储器”，请到 “应用信息 -> 权限” 中授予！");
        builder.setPositiveButton("去手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 完成录制触发的函数,可进行文件上传操作
     */
    @Override
    public void stop() {
        if (filePath==null){
            return;
        }
        Log.e("peter","filePath=>"+filePath);
        File file=new File(filePath);
        if (file.isFile()){
            Log.e("peter","fileLength=>"+file.length());
        }
        // storage/emulated/0/AudioRecorderMp3/recorder/20190112170111.mp3
        Toast.makeText(MainActivity.this,"录音文件保存完毕,文件保存在:" + filePath,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if (mp3Recorder != null && mp3Recorder.isRecording()) {
                new AlertDialog.Builder(this).setTitle("提示").setMessage("正在录音中，是否保存正在录制的音频?").setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mp3Recorder.stopRecording();
                            Toast.makeText(MainActivity.this, "结束录音!", Toast.LENGTH_SHORT).show();
                            mp3Recorder = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        MainActivity.this.finish();
                        System.exit(0);
                    }
                }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mp3Recorder.stopRecording();
                            Toast.makeText(MainActivity.this, "结束录音!", Toast.LENGTH_SHORT).show();
                            mp3Recorder = null;
                            FileUtils.deleteFile(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        MainActivity.this.finish();
                        System.exit(0);
                    }
                }).create().show();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
