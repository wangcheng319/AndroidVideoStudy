package com.wangc.androidvideostudy

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.wangc.myplayer.Demo
import kotlinx.android.synthetic.main.activity_main.*

/**
 * GlSurfaceView显示
 */
class MainActivity : AppCompatActivity(),View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onClick(v: View?) {
        when (v){
            btn_audio->startActivity(Intent(this,AudioRecordActivity::class.java))
            btn_camera->startActivity(Intent(this,CameraActivity::class.java))
            btn_camera2->startActivity(Intent(this,Camera2Activity::class.java))
            btn_extractor->startActivity(Intent(this,MediaExtractorActivity::class.java))
            btn_jni->startActivity(Intent(this,JniTestActivity::class.java))
            btn_get_ffmpeg->{getConfig()}
        }
    }

    /**
     * 获取FFmpeg配置，验证添加成功
     */
    private fun getConfig() {
        var demo = Demo()
        demo.getFFmpegConfig()
    }

}
