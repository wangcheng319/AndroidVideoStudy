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
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG;

/**
 * 1、Camera2预览
 * 2、获取原始数据--YUV
 * 3、显示YUV数据
 * 4、转为MP4保存
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


    private int width = 1280;
    private int height = 720;
    private int framerate = 30; //一秒30帧
    private H264Encoder h264Encoder;


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

    static {
        System.loadLibrary("native-lib");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        final ImageView imageView = findViewById(R.id.iv);

        if (supportH264Codec()){ //查询手机是否支持AVC编码
            Log.e("TAG" , "support H264 hard codec");
        }else {
            Log.e("TAG" , "not support H264 hard codec");
        }


        //init surfaceview
        mSurfaceView = findViewById(R.id.sv);
        mSurfaceView.setKeepScreenOn(true);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCamera2();
                h264Encoder = new H264Encoder(width,height,framerate);
                h264Encoder.startEncoder();
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
                //c++将图片变为灰色
                tranBitmap(bitmap);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }

                //保存图片
                try {
                    File myDir = new File(Environment.getExternalStorageDirectory() + "/saved_images");
                    myDir.mkdirs();
                    String fname = "camera2.jpg";
                    File file = new File (myDir, fname);
                    if (file.exists ()) file.delete ();
                    file.createNewFile();

                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(bytes);
                    outputStream.close();
                    Log.e(TAG,"保存成功："+file.getAbsolutePath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                image.close();


            }
        }, mHandler);


        //用于预览获取原始数据
        mImageReaderPreview = ImageReader.newInstance(1080, 1920, ImageFormat.YUV_420_888,1);
        mImageReaderPreview.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {
                // 最后一帧数据
                Image image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);

//                try {
//                    startCodec(bytes);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

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
     * c++层处理图片，将图片变为灰色
     * @param bitmap
     */
    private native void tranBitmap(Bitmap bitmap);

    private boolean supportH264Codec() {
        if (Build.VERSION.SDK_INT >= 18){
            int number = MediaCodecList.getCodecCount();
            for (int i=number-1 ; i >0 ; i--){
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
                String[] types = codecInfo.getSupportedTypes();
                //是否支持H.264(avc)的编码
                for (int j = 0 ; j < types.length ; j++){
                    if (types[j].equalsIgnoreCase("video/avc")){
                        return true;
                    }
                }
            }
        }
        return false;
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


    /**
     * 开启编码
     * @param bytes
     */
    private void startCodec(final byte[] bytes) throws IOException {

        File outFile = new File(Environment.getExternalStorageDirectory()+"/mycamera2.mp4");
        final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile));

        //MediaCodec初始化
        MediaCodec mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 1080, 1920);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 441000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();

        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();


        //向mediaCodec存数据
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            //存放新采集的数据
            inputBuffer.put(bytes, 0, bytes.length);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, inputBuffers[inputBufferIndex].position(),  System.nanoTime() / 1000, 0);

            //从mediaCodec取数据
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0); //10
            //循环解码，直到数据全部解码完成
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                try {
                    outputStream.write(outData, 0, outData.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }

        }

        mediaCodec.stop();
        mediaCodec.release();
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


    /**
     * 开启预览
     */
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

    /**
     * 拍照回调
     */
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
    };


}