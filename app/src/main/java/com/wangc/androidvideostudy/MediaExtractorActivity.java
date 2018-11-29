package com.wangc.androidvideostudy;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

/**
 * MediaExtractor使用实例
 * 修改视频中的声音
 */
public class MediaExtractorActivity extends AppCompatActivity {

    private MediaMuxer mediaMuxer;//用于合成
    private MediaExtractor videoExtractor;
    private MediaExtractor audioExtractor;
    private int videoTrackIndex = -1;
    private int audioTrackIndex = -1;
    private File file;

    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private String url1 = "http://v.yinyuetai.com/video/736496.mp4";
    private String url2 = "http://v.yinyuetai.com/video/3260807";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_extractor);
        surfaceView = findViewById(R.id.sv);
        mediaPlayer = new MediaPlayer();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mediaPlayer.setDisplay(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        //合成
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Executors.newFixedThreadPool(3).submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            beginMuxer();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });

        //播放
        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mediaPlayer.setDataSource(file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                    }
                });
            }
        });
    }

    /**
     * 合成视频
     */
    private void beginMuxer() throws IOException {

        //最终合成的视频
        file = new File(Environment.getExternalStorageDirectory()+"/wuyuetian3.mp4");
        mediaMuxer = new MediaMuxer(file.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);


        //获取视频数据
        videoExtractor = new MediaExtractor();
        try {
            videoExtractor.setDataSource(Environment.getExternalStorageDirectory()+"/wuyuetian.mp4");
            for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
                MediaFormat mediaFormat = videoExtractor.getTrackFormat(i);
                if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("video/")){
                    videoExtractor.selectTrack(i);
                    videoTrackIndex = mediaMuxer.addTrack(mediaFormat);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //获取音频数据
        audioExtractor = new MediaExtractor();
        try {
            audioExtractor.setDataSource(Environment.getExternalStorageDirectory()+"/wuyuetian2.mp4");
            for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
                MediaFormat audioFormat = audioExtractor.getTrackFormat(i);
                if (audioFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")){
                    audioExtractor.selectTrack(i);
                    audioTrackIndex = mediaMuxer.addTrack(audioFormat);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 添加完所有轨道后开始合成
        mediaMuxer.start();

        // 封装视频track
        if (-1 != videoTrackIndex) {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            info.presentationTimeUs = 0;
            ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
            while (true) {
                int sampleSize = videoExtractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    break;
                }

                info.offset = 0;
                info.size = sampleSize;
                info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                info.presentationTimeUs = videoExtractor.getSampleTime();
                mediaMuxer.writeSampleData(videoTrackIndex, buffer, info);

                videoExtractor.advance();
            }
        }


        // 封装音频track
        if (-1 != audioTrackIndex) {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            info.presentationTimeUs = 0;
            ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
            while (true) {
                int sampleSize = audioExtractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    break;
                }

                info.offset = 0;
                info.size = sampleSize;
                info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                info.presentationTimeUs = audioExtractor.getSampleTime();
                mediaMuxer.writeSampleData(audioTrackIndex, buffer, info);

                audioExtractor.advance();
            }
        }

        videoExtractor.release();
        audioExtractor.release();

        mediaMuxer.stop();
        mediaMuxer.release();

        Log.e("+++","合成结束路径："+file.getAbsolutePath());
        Log.e("+++","合成结束大小："+file.length());
        Toast.makeText(this,"合成结束，可以播放！",Toast.LENGTH_LONG).show();

    }
}
