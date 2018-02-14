package com.app.xz.mynote.publics.core.component;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.app.xz.mynote.R;

/**
 * Created by dixon.xu on 2018/2/1.
 */

public abstract class BaseActivity extends Activity {

    public enum EnterAnim {
        Default, FromBottom
    }

    private LayoutInflater mLayoutInflater;
    private FrameLayout mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        //去除标题
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        //去除状态栏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mLayoutInflater = LayoutInflater.from(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {
        //为了遵循自带函数顺序 这里先inflate base 添加完必要视图后 最后执行setContent
        mContentView = (FrameLayout) mLayoutInflater.inflate(R.layout.activity_base_view, null);
        mContentView.addView(mLayoutInflater.inflate(layoutResID, mContentView, false));
        super.setContentView(mContentView);

        init();
    }

    //setContentView结束后 即init
    protected abstract void init();


    /**
     * finish
     */
    //页面切换统一动画 也方便以后主题更换
    @Override
    public void finish() {
        super.finish();
        finish(EnterAnim.Default);
    }

    public void finish(EnterAnim anim) {
        super.finish();
        //注意：第一个参数就是进入页面的动画 第二个参数是退出页面的动画 start主要是指定进入页面 finish主要是指定退出页面
        switch (anim) {
            case Default:
                overridePendingTransition(R.anim.start_in, R.anim.start_out);
                break;
            case FromBottom:
                overridePendingTransition(0, R.anim.start_out_bottom);
                break;
        }
    }

    public void finishQuick() {
        super.finish();
    }

    /**
     * startActivity
     *
     * @param intent
     */
    @Override
    public void startActivity(Intent intent) {
        startActivity(intent, EnterAnim.Default);
    }

    public void startActivity(Intent intent, EnterAnim anim) {
        super.startActivity(intent);
        switch (anim) {
            case Default:
                overridePendingTransition(R.anim.start_in, R.anim.start_out);
                break;
            case FromBottom:
                overridePendingTransition(R.anim.start_in_bottom, 0);
                break;
        }
    }

    /**
     * ⭐️最终调用的父类的startActivity(a,b) 然后调用父类的startActivityForResult 而this.startAtyForResult 实际是子类重写过的 所以仍然会调用子类的startAtyForResult
     * 如下：
     * public class A {
     * <p>
     * public void log() {
     * Log.e("testkkk", "a");
     * this.log2();
     * }
     * <p>
     * public void log2() {
     * Log.e("testkkk", "a2");
     * }
     * }
     * <p>
     * public class B extends A{
     *
     * @param intent
     * @Override public void log() {
     * super.log();
     * }
     * @Override public void log2() {
     * super.log2();
     * Log.e("testkkk","b2");
     * }
     * }
     * <p>
     * A a = new B();
     * a.log();
     * <p>
     * 结果：a,a2,b2 this.log2如果被子类重写 则调用的是其实际子类的！
     * <p>
     * ❗️这里需要重构！
     */
    public void startActivityQuick(Intent intent) {
        super.startActivity(intent);
    }

    /**
     * startActivityForResult
     *
     * @param intent
     * @param requestCode
     */
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, EnterAnim.Default);
    }

    public void startActivityForResult(Intent intent, int requestCode, EnterAnim anim) {
        super.startActivityForResult(intent, requestCode);
        switch (anim) {
            case Default:
                overridePendingTransition(R.anim.start_in, R.anim.start_out);
                break;
            case FromBottom:
                overridePendingTransition(R.anim.start_in_bottom, 0);
                break;
        }
    }
}
