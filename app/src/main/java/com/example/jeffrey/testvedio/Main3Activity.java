package com.example.jeffrey.testvedio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

public class Main3Activity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = MainActivity.class.getSimpleName();
    MediaPlayer player;
    SurfaceView surface;
    SurfaceHolder surfaceHolder;
    String vedioname="";//獲得錄完影片的日期(名稱)
    ImageButton button_send;
    ImageButton button_yes;
    ImageButton button_play;
    TextView txtPercentage;
    private ProgressBar progressBar;
    ImageButton button_upload;
    long totalSize = 0;
    private String filePath = null;
    //---------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        surface=(SurfaceView)findViewById(R.id.surface);
        button_send = (ImageButton) findViewById(R.id.button_send);
        button_yes = (ImageButton) findViewById(R.id.button_yes);
        button_play = (ImageButton) findViewById(R.id.button_play);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        button_upload = (ImageButton) findViewById(R.id.button_upload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Intent intent = this.getIntent(); //接收Main2Activity傳來的videoname
        vedioname = intent.getStringExtra("videoname");
        filePath = intent.getStringExtra("filePath");
       if(intent.getIntExtra("classFrom",0) ==2) { // 如果是由Main2Activity呼叫的
            Toast.makeText(Main3Activity.this, vedioname + " saved", Toast.LENGTH_SHORT).show(); //顯示已儲存影片
       }else if(intent.getIntExtra("classFrom",0) ==1){ // 如果是由MainActivity呼叫的
           button_send.setVisibility(View.INVISIBLE); // 隱藏button_send
           button_yes.setVisibility(View.VISIBLE);    // 顯示button_yes
       }
        surfaceHolder=surface.getHolder();//SurfaceHolder是SurfaceView的控制接口
        surfaceHolder.addCallback(this); //因为这个类实现了SurfaceHolder.Callback接口，所以回调参数直接this
        surfaceHolder.setFixedSize(320, 220);//显示的分辨率,不设置为视频默认
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//必須在 surfaceCreated 之前設定完成，不然只會聽到聲音看不到影像。
                                                                           // setType必須設定成SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS 否則播放會出錯。
        //------------------------------------------------------------------------------------------
        button_send.setOnClickListener(new View.OnClickListener() { //如果由Main2Activity呼叫的 ， 按下傳送按鈕開啟MainActivity
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Main3Activity.this, MainActivity.class);
                intent.putExtra("vedioname2", vedioname); // 把vedioname傳給MainActivity以產生thumbnail
                startActivity(intent);
                finish();
            }
        });
        button_yes.setOnClickListener(new View.OnClickListener() { //如果是由MainActivity呼叫的 ， 按下確認按鈕開啟MainActivity
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Main3Activity.this, MainActivity.class);
                intent.putExtra("vedioname2", vedioname);// 把vedioname傳給MainActivity以產生thumbnail
                startActivity(intent);
                finish();
            }
        });
        button_play.setOnClickListener(new View.OnClickListener() { //播放影片
            @Override
            public void onClick(View v) {
                if(player.isPlaying() == false){
                    player.start();
                }
                button_play.setVisibility(View.INVISIBLE); //開始播放後隱藏play button
            }
        });
        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UploadFileToServer().execute();
            }
        });
        //------------------------------------------------------------------------------------------
    }
    //----------------------------------------------------------------------------------------------------------------------------
    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);
            // updating progress bar value
            progressBar.setProgress(progress[0]);
            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }
        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }
        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);
            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(new AndroidMultiPartEntity.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                File sourceFile = new File(filePath);
                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));
                // Extra parameters if you want to pass to server
                entity.addPart("website", new StringBody("www.androidhive.info"));
                entity.addPart("email", new StringBody("abc@gmail.com"));
                totalSize = entity.getContentLength();
                httppost.setEntity(entity);
                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: " + statusCode;
                }
            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }
            return responseString;
        }
        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);
            // showing the server response in an alert dialog
            showAlert(result);
            super.onPostExecute(result);
        }
    }
    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }
    //----------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event) { //觸碰螢幕暫停播放並顯示play button
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if(player.isPlaying() == true){
                player.pause();
            }
            button_play.setVisibility(View.VISIBLE);
        }
        return super.onTouchEvent(event);
    }
    //---------------------------------------------------------------------------------------------------------------------------
    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }
    //---------------------------------------------------------------------------------------------------------------------------
    @Override
    public void surfaceCreated(SurfaceHolder arg0) { //必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
        player=new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDisplay(surfaceHolder);  //设置显示视频显示在SurfaceView上 //必須在 surfaceCreated 之後才能執行，不然只會聽到聲音看不到影像。
        try {
            player.setDataSource("/sdcard/Movies/MyCameraVideo/" + vedioname); // 可以輸入網址或是檔案路徑。
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { // 當播放完畢時顯示play button
            @Override
            public void onCompletion(MediaPlayer mp) {
                button_play.setVisibility(View.VISIBLE);
            }
        });
    }
    //---------------------------------------------------------------------------------------------------------------------------
    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
    }
    //---------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onResume() { //按下home鍵再回來會執行，把play button隱藏
        super.onResume();
        button_play.setVisibility(View.INVISIBLE);
    }
    //---------------------------------------------------------------------------------------------------------------------------
     @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player.isPlaying()){
            player.stop();
        }
        player.release(); //Activity销毁时停止播放，释放资源。不做这个操作，即使退出还是能听到视频播放的声音
    }
    //---------------------------------------------------------------------------------------------------------------------------
    final Context context = this;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case  KeyEvent.KEYCODE_BACK:
                //產生一個Builder物件，初始化Dialog物件
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                //設定Dialog的標題
                alertDialogBuilder.setTitle("不傳出去?");
                alertDialogBuilder.setMessage("Click yes to exit!") //設定Dialog的內容
                        .setIcon(R.drawable.ic_stop)//設定dialog 的ICON
                        .setCancelable(false)//關閉 Android 系統的主要功能鍵(menu,home等...)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {//設定Positive按鈕資料
                            public void onClick(DialogInterface dialog, int id) {
                                // 按下"收到"以後要做的事情， if yes is clicked, close current activity
                                Intent intent = new Intent();
                                intent.setClass(Main3Activity.this, Main2Activity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {//設定Negative按鈕資料
                            public void onClick(DialogInterface dialog, int id) {
                                // if no is clicked, just close the dialog box and do nothing
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create(); //利用Builder物件建立AlertDialog
                alertDialog.show(); //顯示對話框
                //以下必須寫在alertDialog.show()顯示對話框的下面
                Button positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);//取得BUTTON_POSITIVE的按鈕，並插入圖片在左邊
                Drawable icon1= this.getResources().getDrawable(R.drawable.ic_yes);
                positiveBtn.setCompoundDrawablesWithIntrinsicBounds(icon1, null, null, null);

                Button negativeBtn = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);//取得BUTTON_NEGATIVE的按鈕，並插入圖片在左邊
                Drawable icon2= this.getResources().getDrawable(R.drawable.ic_no);
                negativeBtn.setCompoundDrawablesWithIntrinsicBounds(icon2, null, null, null);
                return true;
            case KeyEvent.KEYCODE_HOME:
                onDestroy();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}