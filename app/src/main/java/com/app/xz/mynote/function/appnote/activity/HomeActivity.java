package com.app.xz.mynote.function.appnote.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.app.xz.mynote.R;
import com.app.xz.mynote.function.appnote.present.DesktopNotePresenter;
import com.app.xz.mynote.function.appnote.present.HomeNotePresenter;
import com.app.xz.mynote.publics.core.component.BaseActivity;
import com.app.xz.mynote.publics.core.utils.AlertPermissionHelper;
import com.app.xz.mynote.publics.views.AutoHeightListView;

/**
 * 要做的点：
 * 统一dialog
 * ->完善内容编辑页面（自定义view 与 保存格式）
 * 桌面Note 与 标记
 * 启动
 * ⭐网络封装
 */

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    public static final int OPEN_TO_ADD_NOTE = 0X001;
    public static final int OPEN_TO_UPDATE_NOTE = 0X002;

    private AutoHeightListView noteListView;
    private View itemMoveTip;
    private View itemDeleteTip;

    //主页Note列表的P层 用于管理数据变更时的页面变化
    private HomeNotePresenter homeNotePresenter;
    //桌面Note的P层 用于管理桌面Note的状态与数据更新
    private DesktopNotePresenter desktopNotePresenter;

    private ImageView addNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        noteListView = findViewById(R.id.note_list_view);
        itemMoveTip = findViewById(R.id.item_move_tip);
        itemDeleteTip = findViewById(R.id.item_delete_tip);
        addNote = findViewById(R.id.add_note);
    }

    @Override
    protected void init() {
        //申请权限
        AlertPermissionHelper.checkAlertPermission(this);

        //初始化便签列表
        desktopNotePresenter = new DesktopNotePresenter(this);
        homeNotePresenter = new HomeNotePresenter(this, noteListView, itemMoveTip, itemDeleteTip, desktopNotePresenter);
        homeNotePresenter.init();
        ((ScrollView) findViewById(R.id.scrollView)).smoothScrollTo(0, 0);

        //设置组件的点击事件
        addNote.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_note:
                startActivityForResult(new Intent(HomeActivity.this, AddNoteActivity.class), OPEN_TO_ADD_NOTE, EnterAnim.FromBottom);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case OPEN_TO_ADD_NOTE:
            case OPEN_TO_UPDATE_NOTE:
                if (RESULT_OK == resultCode) {
                    homeNotePresenter.update();
                }
                break;
        }
    }
}
