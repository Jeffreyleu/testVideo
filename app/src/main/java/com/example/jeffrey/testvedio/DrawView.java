package com.example.jeffrey.testvedio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceView;

public class DrawView extends SurfaceView {
    private Paint textPaint = new Paint();
    Bitmap bp;
    int x=50,y=100;  //貼圖在螢幕上的 x,y 座標
    public DrawView(Context context) {
        super(context);
        bp = BitmapFactory.decodeResource(getResources(), R.drawable.floating110); //設定圖片來源(此處使用預設的圖片)
//        /* This call is necessary, or else the
//         * draw method will not be called.
//         */
        setWillNotDraw(false);
    }
    public void onDraw(Canvas canvas){
        canvas.drawBitmap(bp, x, y, textPaint);
    }
}
