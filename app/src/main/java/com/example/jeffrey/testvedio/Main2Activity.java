package com.example.jeffrey.testvedio;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class Main2Activity extends Activity {
    private Camera mCamera;
    private CameraPreview mPreview;
    DrawView drawview;
    private MediaRecorder mMediaRecorder;   // 錄製視頻的類
    private ImageView startImage;
    private ImageButton changeButton;
    private ImageButton backButton;
    Chronometer timer;
    private AnimationDrawable animationDrawable;
    private boolean isRecording = false;
    int facing;
    int facingControll = 0;
    String timeStamp;
    String videoname;
    private Uri fileUri; // file url to store image/video
    //---------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // making it full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE); // requesting to turn the title OFF
        setContentView(R.layout.activity_main2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //限制應用程式只能直立，禁止橫向
        //------------------------------------------------------------------------------------------
        Snackbar.make(findViewById(R.id.activity_main2_layout), "按住按鈕進行錄製", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .show();
        //------------------------------------------------------------------------------------------
        startImage = (ImageView) findViewById(R.id.image_start);
        changeButton = (ImageButton) findViewById(R.id.button_change);
        backButton = (ImageButton) findViewById(R.id.button_back);
        timer = (Chronometer)findViewById(R.id.timer);
        //------------------------------------------------------------------------------------------
        Intent intent = getIntent();
        facing = intent.getIntExtra("FACING", Camera.CameraInfo.CAMERA_FACING_BACK);
        if (facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            changeButton.setImageResource(R.drawable.ic_camera_front_white_48dp);
            facingControll = 90;
        } else if(facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            changeButton.setImageResource(R.drawable.ic_camera_rear_white_48dp);
            facingControll = 270;
        }
        //------------------------------------------------------------------------------------------
        mCamera = getCameraInstance();   // 創建Camera實例
        mPreview = new CameraPreview(this, mCamera);   // 創建Preview view並將其設為activity中的內容

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        drawview = new DrawView(this);
        preview.addView(drawview);

        //------------------------------------------------------------------------------------------
        startImage.setOnTouchListener(new View.OnTouchListener() {
            long lastDown = 0L;
            long keyPressedDuration = 0L;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastDown = System.currentTimeMillis();
                        if (mMediaRecorder != null) { // 開始錄製按鈕
                            releaseMediaRecorder(); // 停止錄製並釋放MediaRecorder對象
                        }
                        startImage.setImageResource(R.drawable.animation);
                        animationDrawable = (AnimationDrawable) startImage.getDrawable();
                        animationDrawable.start();
                        if (prepareVideoRecorder()) { // Camera已可用並解鎖，MediaRecorder已就緒,現在可以開始錄像
                            isRecording = true;
                            mMediaRecorder.start(); // 開始錄製
                            timer.setBase(SystemClock.elapsedRealtime());
                            timer.start();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        keyPressedDuration = System.currentTimeMillis() - lastDown;
                        animationDrawable = (AnimationDrawable) startImage.getDrawable();
                        animationDrawable.stop();
                        timer.stop();
                        Toast.makeText(Main2Activity.this, "" + keyPressedDuration, Toast.LENGTH_SHORT).show();
                        if (isRecording) { // 停止錄像並釋放camera
                            releaseMediaRecorder(); // 停止錄製並釋放MediaRecorder對象
                            mCamera.lock();//將控制權從MediaRecorder交回camera。鎖定攝像頭，使得以後MediaRecorder session能夠使用它。自Android 4.0(API level 14)開始，不再需要本調用了，除非MediaRecorder.prepare()調用失敗。
                            isRecording = false;
                            if (keyPressedDuration > 7780) {
                                Intent intent = new Intent(); // 把videoname傳給Main3Activity讓他播放
                                intent.setClass(Main2Activity.this, Main3Activity.class);
                                intent.putExtra("videoname", videoname);
                                intent.putExtra("classFrom", 2); //為了讓Main3Activity知道呼叫者是Main2Activity 代號設為2
                                intent.putExtra("filePath", fileUri.getPath());
                                startActivity(intent);
                                finish();
                            } else {
                                Snackbar.make(findViewById(R.id.activity_main2_layout), "最少錄5秒", Snackbar.LENGTH_LONG)
                                        .setAction("OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                            }
                                        })
                                        .show();
                            }
                        }
                        break;
                }
                return true;
            }
        });
        //------------------------------------------------------------------------------------------
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording == false) {
                    if (facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    } else if (facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        facing = Camera.CameraInfo.CAMERA_FACING_BACK;
                    }
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                    Intent intent = new Intent();
                    intent.setClass(Main2Activity.this, Main2Activity.class); //重新啟動MainActivity
                    intent.putExtra("FACING", facing);
                    startActivity(intent);  //重啟
                    finish();   //關掉原本的Main2Activity
                } else if (isRecording == true) {
                    Toast.makeText(Main2Activity.this, "vedio is recording...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //------------------------------------------------------------------------------------------
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Main2Activity.this, MainActivity.class); //重新啟動MainActivity
                startActivity(intent);
                finish();
            }
        });
    }
    //---------------------------------------------------------------------------------------------------------------------------
    private boolean prepareVideoRecorder() { //如果mCamera存在，首先释放然後设置為null，不然在調用Camera.open()方法會無限報錯
        if (mCamera != null) {
            mCamera.release();  // 釋放資源
            mCamera = null;
        }
        mCamera = getCameraInstance();  // 創建Camera實例
        Camera.Parameters params = mCamera.getParameters(); //設置攝像時的角度，不然會轉90度
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);// set the focus mode
        params.setPreviewSize(1280, 720);
        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(90);
       // mCamera.autoFocus(AutoFocListener);
        //next codes is right for 2.3 and 4.0
        releaseMediaRecorder();
        mMediaRecorder = new MediaRecorder();   // 創建mediarecorder物件
        //開始錄制視頻 —— 嚴格按照以下順序執行才能成功錄制視頻
        mCamera.unlock(); //解鎖，便於MediaRecorder 使用攝像頭
        // 配置MediaRecorder —— 按照如下順序調用MediaRecorder 中的方法
        mMediaRecorder.setCamera(mCamera);  //用當前Camera實例將攝像頭用途設置為視頻捕捉 // 將攝像頭指向MediaRecorder
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER); //設置音频source
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);    //設置視頻source(視頻源為Camera(相機))
        mMediaRecorder.setProfile(CameraPreview.profile);//設置視頻輸出格式和編碼格式。對於Android 2.2 (API Level 8) 以上版本，使用MediaRecorder.setProfile 方法，並用CamcorderProfile.get()來獲取一個profile實例
        mMediaRecorder.setVideoSize(CameraPreview.profile.videoFrameWidth, CameraPreview.profile.videoFrameHeight);
        mMediaRecorder.setOrientationHint(facingControll);
        mMediaRecorder.setOutputFile(getOutputMediaFile().toString());  // 設置視頻檔輸出文件
        mMediaRecorder.setMaxDuration(60000);
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());//用上面連接預覽中設置的對象來指定應用程序的SurfaceView 預覽layout元素
        fileUri = Uri.fromFile(getOutputMediaFile());
        try {
            mMediaRecorder.prepare();   // 準備錄製
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    //---------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onPause() {
        super.onPause();
    }
    //---------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaRecorder();
        releaseCamera();
    }
    //---------------------------------------------------------------------------------------------------------------------------
    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            try{
                mMediaRecorder.stop();
            }catch(RuntimeException stopException){
                mMediaRecorder.release(); // 釋放recorder對象
            }
            mCamera.lock();           // 為後續使用鎖定攝像頭
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // 為其它應用釋放攝像頭 // 釋放資源 // 使用Camera.release()釋放攝像頭，使其它應用程序可以使用它
            mCamera = null;
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            Camera.CameraInfo info = new Camera.CameraInfo();
            int ncamera = Camera.getNumberOfCameras();
            for (int i = 0; i < ncamera; i++) {     //檢查手機所有的相機，若其facing(背面或前面)是使用者想要的就開起相機
                Camera.getCameraInfo(i, info);
                if (info.facing == facing) {
                    c = c.open(i);
                    break;
                }
            }
        } catch (Exception e) {
        }
        return c;
    }
    //---------------------------------------------------------------------------------------------------------------------------
    public File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "MyCameraVideo"); //在movie資料夾裡建資料夾
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // 創建媒體文件名
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        videoname = "VID_" + timeStamp + ".mp4"; // 把影片完整名稱存起來以傳給Main3Activity使用
        return mediaFile;
    }
    //---------------------------------------------------------------------------------------------------------------------------
  /*  private Camera.AutoFocusCallback AutoFocListener = new Camera.AutoFocusCallback(){
      public void onAutoFocus(boolean success, final Camera camera){
          if(success){

          }
      }
    };*/
    //---------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                onDestroy();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}