package com.wangc.androidvideostudy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *1、采集Camera原始数据，并预览
 *2、将数据编码为MP4格式保存
*/
public class CameraActivity extends AppCompatActivity implements View.OnClickListener, Camera.PreviewCallback {
    private Button btn_start;
    private Button btn_stop;
    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        checkPermissions();

        initView();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},101);
        }

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},102);
        }
    }

    private void initView() {
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        surfaceView = findViewById(R.id.sv);


        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new MyCallBack());
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
    }

    public class MyCallBack implements  SurfaceHolder.Callback{

       @Override
       public void surfaceCreated(SurfaceHolder holder) {
           openCamera(holder);

       }

       @Override
       public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

       }

       @Override
       public void surfaceDestroyed(SurfaceHolder holder) {

       }
   }

    private void openCamera(SurfaceHolder holder) {
        Camera camera = Camera.open();
        if (camera == null){
            Log.e("+++","打开相机失败");
            return;
        }

        try {

            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewFormat(ImageFormat.NV21);

//            Camera.Size PreviewSize = calculatePerfectSize(parameters.getSupportedPreviewSizes(), 1920, 1080);
//            parameters.setPreviewSize(PreviewSize.width, PreviewSize.height);
//
//            Camera.Size size = calculatePerfectSize(parameters.getSupportedPictureSizes(),
//                    1920, 1080);
//            parameters.setPictureSize(size.width, size.height);



            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.setPreviewCallback(this);
            camera.setParameters(parameters);

            camera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Camera.Size calculatePerfectSize(List<Camera.Size> supportedPreviewSizes, int expectWidth, int expectHeight) {
        sortList(supportedPreviewSizes); // 根据宽度进行排序
        Camera.Size result = supportedPreviewSizes.get(0);
        boolean widthOrHeight = false; // 判断存在宽或高相等的Size
        // 辗转计算宽高最接近的值
        for (Camera.Size size: supportedPreviewSizes) {
            // 如果宽高相等，则直接返回
            if (size.width == expectWidth && size.height == expectHeight) {
                result = size;
                break;
            }
            // 仅仅是宽度相等，计算高度最接近的size
            if (size.width == expectWidth) {
                widthOrHeight = true;
                if (Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
            // 高度相等，则计算宽度最接近的Size
            else if (size.height == expectHeight) {
                widthOrHeight = true;
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)) {
                    result = size;
                }
            }
            // 如果之前的查找不存在宽或高相等的情况，则计算宽度和高度都最接近的期望值的Size
            else if (!widthOrHeight) {
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)
                        && Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
        }
        return result;
    }

    private void sortList(List<Camera.Size> supportedPreviewSizes) {
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size pre, Camera.Size after) {
                if (pre.width > after.width) {
                    return 1;
                } else if (pre.width < after.width) {
                    return -1;
                }
                return 0;
            }
        });
    }
}
