package com.wangc.androidvideostudy;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class MySurfaceView extends GLSurfaceView {

    private MGlRender mGlRender;

    public MySurfaceView(Context context) {
        super(context);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGlRender = new MGlRender();
        setRenderer(mGlRender);
    }
}
