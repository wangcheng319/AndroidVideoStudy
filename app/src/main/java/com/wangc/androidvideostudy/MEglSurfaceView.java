package com.wangc.androidvideostudy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by wangc on 2018/11/15
 * E-MAIL:274281610@QQ.COM
 * 自定义EglSurface
 */
public abstract class MEglSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private Surface surface;
    private EGLContext eglContext;
    private MEglThread mEglThread;

    public MEglSurfaceView(Context context) {
        this(context,null);
    }

    public MEglSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MEglSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (surface == null){
            surface = holder.getSurface();
        }

        mEglThread = new MEglThread(new WeakReference<MEglSurfaceView>(this));
        mEglThread.isCreate = true;
        mEglThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void setSurfaceAndContext(Surface surface,EGLContext context){
        this.surface = surface;
        this.eglContext = context;
    }

    static class MEglThread extends Thread{

        private EglHelper eglHelper;
        private WeakReference<MEglSurfaceView> mEglSurfaceViewWeakReference;
        private boolean isExit = false;
        private boolean isCreate = false;


        public MEglThread(WeakReference<MEglSurfaceView> mEglSurfaceViewWeakReference) {
            this.mEglSurfaceViewWeakReference = mEglSurfaceViewWeakReference;
        }

        @Override
        public void run() {
            super.run();
            eglHelper = new EglHelper();
            eglHelper.initEgl(mEglSurfaceViewWeakReference.get().surface,mEglSurfaceViewWeakReference.get().eglContext);

            while (true){

                if (isExit){
                    break;
                }

            }
        }
    }
}
