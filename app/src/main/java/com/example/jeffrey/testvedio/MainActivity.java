package com.example.jeffrey.testvedio;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.app.Activity;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;


public class MainActivity extends Activity {
    private ImageButton vedioButton;
    private ImageButton button_play;
    private ImageView iv_thumbnail;
    String vedioname2;
    //------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = this.getIntent(); // 取得Main3Activity傳來的videoname2
        vedioname2 = intent.getStringExtra("vedioname2");
        vedioButton = (ImageButton) findViewById(R.id.button_vedio);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //限制應用程式只能直立，禁止橫向

        vedioButton.setOnClickListener(new View.OnClickListener() { //開始Main2Activity錄影
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });
        //產生" vedioname2 "的thumbnail
        iv_thumbnail = (ImageView)findViewById(R.id.iv_thumbnail);
        Bitmap bmThumbnail;
        // MICRO_KIND: 96 x 96 thumbnail
        String filePath = "/sdcard/Movies/MyCameraVideo/" + vedioname2;
        bmThumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MICRO_KIND);
        iv_thumbnail.setImageBitmap(bmThumbnail);
        button_play = (ImageButton) findViewById(R.id.button_play);
        if(vedioname2 != null) { // 當Main3Activity尚未傳來videoname2 時不顯示播放按鈕
            button_play.setVisibility(View.VISIBLE);
        }
        button_play.setOnClickListener(new View.OnClickListener() { //播放影片
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Main3Activity.class);
                intent.putExtra("videoname", vedioname2); //Main3Activity必須接收videoname
                intent.putExtra("classFrom", 1); //為了讓Main3Activity知道呼叫者是MainActivity 代號設為1
                startActivity(intent);
            }
        });
    }
    //---------------------------------------------------------------------------------------------------------------------
}