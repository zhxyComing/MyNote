package com.app.xz.mynote.function.launch.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import com.app.xz.mynote.R;
import com.app.xz.mynote.function.appnote.activity.HomeActivity;
import com.app.xz.mynote.publics.core.component.BaseActivity;
import com.app.xz.mynote.publics.core.utils.handler.HandlerUtils;

public class LaunchActivity extends BaseActivity {

    private TextView launchTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void init() {

        HandlerUtils.executeDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(LaunchActivity.this, HomeActivity.class));

                        //延迟finish 避免动画出现问题
                        HandlerUtils.executeDelayed(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                });
                            }
                        }, 200);
                    }
                });
            }
        }, 1500);

    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        launchTitle = findViewById(R.id.launch_title);
    }
}
