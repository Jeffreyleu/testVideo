package com.example.jeffrey.testvedio;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
//為了方便拍照或攝像，用戶必須能看到攝像頭所拍攝的畫面。攝像頭預覽類就是一種能夠顯示攝像頭實時數據的SurfaceView，用戶可以調整並捕獲圖片和視頻。
//為了捕捉view創建和銷毀時的回調事件，此類實現了SurfaceHolder.Callback，這在指定攝像頭預覽的輸入時需要用到。

/** 基本的攝像頭預覽類 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    static CamcorderProfile profile;

    /**CameraPreview 建構子*/
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;   // 安裝一個SurfaceHolder.Callback，這樣創建和銷毀底層surface時能夠獲得通知。
        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // set the focus mode
        params.setPreviewSize(profile.videoFrameWidth,profile.videoFrameHeight);
        mCamera.setParameters(params);// set Camera parameters
        mCamera.setDisplayOrientation(90);
        // Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
        mHolder = getHolder();  //  SurfaceView顯示畫面，需透過SurfaceHolder來存取  // 呼叫getHolder()方法來取得SurfaceHolder,並指給mHolder
        mHolder.addCallback(this);   // 把這個class本身(extends SurfaceView)透過mHolder的 Callback()方法連結起來，也可寫成 getHolder().addCallback(this)
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);   // setType必須設置，要不出錯  // 已過期的設置，但版本低於3.0的Android還需要
    }

    /**以下為與 SurfaceHolder.Callback 相關聯的三個程序*/
    public void surfaceCreated(SurfaceHolder holder) { // (surface 建立時)surface被創建，現在把預覽畫面的位置通知攝像頭
        try {
            mCamera.setPreviewDisplay(holder); // 將camera連接到一個SurfaceView，準備實時預覽
            mCamera.startPreview(); //開始顯示實時攝像畫面
        } catch (IOException e) {
        }
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {    //(surface 改變時)如果預覽無法更改或旋轉，注意此處的事件，確保在縮放或重排時停止預覽
        if (mHolder.getSurface() == null){   // 預覽surface不存在
            return;
        }
        // 更改時停止預覽
        try {
            mCamera.stopPreview(); //  activity使用完攝像頭後，用Camera.stopPreview()停止預覽。
        } catch (Exception e){
            // 忽略：試圖停止不存在的預覽
        }
        // 在此進行縮放、旋轉和重新組織格式，以新的設置啟動預覽
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e){
        }
    }
    public void surfaceDestroyed(SurfaceHolder holder) {    //(surface 結束時)
        // 空代碼。注意在activity中釋放攝像頭預覽對象
    }
}