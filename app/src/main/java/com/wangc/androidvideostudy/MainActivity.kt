package com.wangc.androidvideostudy

import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * GlSurfaceView显示
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var mediaPlayer = MediaPlayer.create(this,R.drawable.abc_ab_share_pack_mtrl_alpha)
        var holder = sv.holder

        var total = mediaPlayer.duration
    }
}
