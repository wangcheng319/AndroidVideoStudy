package com.wangc.androidvideostudy;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * 1、Camera2预览
 * 2、获取原始数据
 * 3、转为MP4保存
 */
public class Camera2Activity extends AppCompatActivity {
    private static final String TAG = "Camera2Activity";
    private CameraDevice mCameraDevice;
    private SurfaceView mSurfaceView;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader mImageReader;
    private ImageReader mImageReaderPreview;
    private CaptureRequest mPreviewRequest;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    ///为了使照片竖直显示
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        final ImageView imageView = findViewById(R.id.iv);

        //init surfaceview
        mSurfaceView = findViewById(R.id.sv);
        mSurfaceView.setKeepScreenOn(true);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCamera2();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
            }
        });

        //用于拍照获取图片
        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG,1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                // 拿到拍照照片数据
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                Log.e(TAG,"拍照数据："+bytes);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
                image.close();
            }
        }, mHandler);


        //用于预览获取原始数据
        mImageReaderPreview = ImageReader.newInstance(1080, 1920, ImageFormat.YUV_420_888,1);
        mImageReaderPreview.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {
                // 获取捕获的照片数据
                Image image = reader.acquireLatestImage();
                Log.i(TAG,"preview image format: " +image.getFormat());

                image.close();
            }
        }, null);


        findViewById(R.id.btn_take).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

    }

    /**
     * 初始化相机
     */
    private void initCamera2() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //获取摄像头数量
            String[] cameras = cameraManager.getCameraIdList();

            //获取摄像头参数
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameras[0]);

            //权限检查
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打开相机
            cameraManager.openCamera(cameras[0], stateCallback, mHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            //创建预览
            createCameraPreviewSession();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };


    private void createCameraPreviewSession() {
        try {
            mPreviewRequestBuilder  = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mSurfaceView.getHolder().getSurface());
            mPreviewRequestBuilder.addTarget(mImageReaderPreview.getSurface());
            //该摄像头的数据输出到以下目标
            List outPuts = Arrays.asList(mSurfaceView.getHolder().getSurface(),
                    mImageReaderPreview.getSurface(),mImageReader.getSurface());

            mCameraDevice.createCaptureSession(outPuts, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (null == mCameraDevice) {
                        return;
                    }
                    cameraCaptureSession = session;
                    try {
                        // 自动对焦应
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 创建预览Request
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        //发送请求,不停的请求最新一帧的图片，这样就达到了预览的效果
                        cameraCaptureSession.setRepeatingRequest(mPreviewRequest, null, mHandler);

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            },mHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (mCameraDevice == null) return;
        // 创建拍照需要的CaptureRequest.Builder
        final CaptureRequest.Builder captureRequestBuilder;
        try {
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取手机方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            //拍照
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            cameraCaptureSession.stopRepeating();
            cameraCaptureSession.capture(mCaptureRequest,captureCallback , mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            Log.e(TAG,"onCaptureStarted");
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            Log.e(TAG,"onCaptureProgressed");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.e(TAG,"onCaptureCompleted");
            //重启预览
            try {
                cameraCaptureSession.setRepeatingRequest(mPreviewRequest, null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.e(TAG,"onCaptureFailed");
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            Log.e(TAG,"onCaptureSequenceCompleted");
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            Log.e(TAG,"onCaptureSequenceAborted");
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            Log.e(TAG,"onCaptureBufferLost");
        }
    };


}