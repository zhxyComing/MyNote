package com.app.xz.mynote.function.appnote.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.xz.mynote.R;
import com.app.xz.mynote.function.appnote.model.Note;
import com.app.xz.mynote.publics.core.component.BaseActivity;
import com.app.xz.mynote.publics.core.utils.AnimUtils;
import com.app.xz.mynote.publics.core.utils.PictureUtils;
import com.app.xz.mynote.publics.core.utils.ToastUtils;
import com.app.xz.mynote.publics.views.EditTextPlus;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 默认页面为添加页面 但也充当修改和展示页面
 */

public class AddNoteActivity extends BaseActivity implements View.OnClickListener {

    private static final int FLAG_COVER_PICTURE = 0X001;
    private static final int FLAG_CONTENT_PICTURE = 0X002;

    private int pictureFlag = -1;

    private PictureUtils pictureUtils;

    private EditText title, tip;
    private EditTextPlus content;
    private TextView type, create;
    private ImageView coverImgSelect, contentImgSelect;
    private FrameLayout selectLayout;

    //允许为空 会使用默认图
    private String imgPath;

    private int typeFlag = Note.TYPE_INT_NORMAL;

    private int titleHeight = 0;
    private int tipHeight = 0;
    private int selectLayoutHeight = 0;

    //修改相关的参数
    private String from = "";
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        String from = getIntent().getStringExtra("from");
        if (!TextUtils.isEmpty(from)) {
            if ("update".equals(from)) {
                this.from = from;
                note = getIntent().getParcelableExtra("note");
            }
        }

        if (isFromUpdate()) {
            initUpdatePage();
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        title = findViewById(R.id.title);
        content = findViewById(R.id.content);
        tip = findViewById(R.id.tip);
        create = findViewById(R.id.create);
        coverImgSelect = findViewById(R.id.select);
        type = findViewById(R.id.type);
        selectLayout = findViewById(R.id.selectLayout);
        contentImgSelect = findViewById(R.id.content_img_select);
    }

    @Override
    protected void init() {

        setClickListener();

        //图片选择器
        pictureUtils = new PictureUtils(this);
        pictureUtils.setOnSelectionListener(new PictureUtils.OnSelectionListener() {
            @Override
            public void onPictureSelected(File pictureFile) {
                if (pictureFile != null) {
                    switch (pictureFlag) {
                        case FLAG_COVER_PICTURE:
                            setCover(pictureFile);
                            break;
                        case FLAG_CONTENT_PICTURE:
                            addContentPicture(pictureFile);
                            break;
                    }
                }
            }
        });

        setViewHeight();

    }

    private void setClickListener() {
        create.setOnClickListener(this);
        coverImgSelect.setOnClickListener(this);
        contentImgSelect.setOnClickListener(this);
        type.setOnClickListener(this);
    }

    /**
     * 获取组件高度 方便动画
     */
    private void setViewHeight() {
        title.post(new Runnable() {
            @Override
            public void run() {
                titleHeight = title.getHeight();
            }
        });

        tip.post(new Runnable() {
            @Override
            public void run() {
                tipHeight = tip.getHeight();
            }
        });

        selectLayout.post(new Runnable() {
            @Override
            public void run() {
                selectLayoutHeight = selectLayout.getHeight();
            }
        });
    }

    /**
     * 设置封面
     */
    private void setCover(File pictureFile) {
        imgPath = pictureFile.getAbsolutePath();
        coverImgSelect.setImageBitmap(BitmapFactory.decodeFile(pictureFile.getAbsolutePath()));
    }

    /**
     * 添加内容图
     * <p>
     * 组件实际可以添加多个图 但是目前pictureUtils只支持一个图
     */
    private void addContentPicture(File pictureFile) {
        List<String> list = new ArrayList<>();
        list.add(pictureFile.getAbsolutePath());
        content.addImage(list);
    }

    /**
     * 如果是修改页或查看页（查看页可修改） 则如下布局
     */
    private void initUpdatePage() {
        create.setText("修改");
        if (note != null) {
            title.setText(note.title);
            tip.setText(note.tip);
            content.setContent(note.content);
//            content.
//            content.scrollTo(0, 0); 这行代码没用 滑不到顶端
//            content.setText(note.content);
            String path = note.image;
            if (!TextUtils.isEmpty(path)) {
                coverImgSelect.setImageBitmap(BitmapFactory.decodeFile(path));
            } else {
                coverImgSelect.setImageResource(R.drawable.bg_note_normal);
            }

            selectLayout.post(new Runnable() {
                @Override
                public void run() {
                    setTypeWithTime(note.type, 0);
                }
            });

            imgPath = note.image;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pictureUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.type:
                if (typeFlag == Note.TYPE_INT_NORMAL) {
                    setType(Note.TYPE_INT_SIMPLE);
                } else {
                    setType(Note.TYPE_INT_NORMAL);
                }
                break;
            case R.id.create:
                createOrUpdateNote();
                break;
            case R.id.select:
                //设置picture的要求源-来自封面
                pictureFlag = FLAG_COVER_PICTURE;
                pictureUtils.selectFromGrllery();
                break;
            case R.id.content_img_select:
                //设置picture的要求源-来自封面
                pictureFlag = FLAG_CONTENT_PICTURE;
                pictureUtils.selectFromGrllery();
                break;
        }
    }

    /**
     * 创建或修改 普通或简易便签
     * <p>
     * 主要作用 判空 区分普通简易
     */
    private void createOrUpdateNote() {

        String strTitle = title.getText().toString();
        String strContent = content.getText().toString();
        String strTip = tip.getText().toString();

        if (typeFlag == Note.TYPE_INT_NORMAL) {

            //普通不允许题目和内容为空
            if (!TextUtils.isEmpty(strTitle) && !TextUtils.isEmpty(strContent)) {

                editToDB(strTitle, strContent, strTip, imgPath, typeFlag);

            } else {
                ToastUtils.toast(this, "题目或内容不能为空");
            }
        } else {

            //简易不允许内容为空
            if (!TextUtils.isEmpty(strContent)) {

                editToDB("", strContent, "", "", typeFlag);

            } else {
                ToastUtils.toast(this, "内容不能为空");
            }
        }
    }

    /**
     * 将Note添加或修改到DB 设置回调并关闭页面
     * <p>
     * 主要作用 添加到数据库 关闭页面
     */
    private void editToDB(String strTitle, String strContent, String strTip, String imgPath, int typeFlag) {
        Note note = new Note();
        note.title = strTitle;
        note.content = strContent;
        note.tip = strTip;
        note.createTime = new Date();
        note.image = imgPath;
        note.type = typeFlag;
        if (isFromUpdate()) {
            note.id = this.note.id;
            note.displayType = this.note.displayType;
            note.updateToDB(AddNoteActivity.this);
        } else {
            note.displayType = Note.DISPLAY_TYPE_NORMAL;
            note.addToDB(AddNoteActivity.this);
        }

        setResult(RESULT_OK);
        finish(EnterAnim.FromBottom);
    }

    /**
     * 设置Note类型是简易还是普通
     *
     * @param type
     */
    private void setType(int type) {
        setTypeWithTime(type, 600);
    }

    private void setTypeWithTime(int type, int time) {
        typeFlag = type;
        if (typeFlag == Note.TYPE_INT_NORMAL) {

            this.type.setText("详 记");

            AnimUtils.height(title, time, new DecelerateInterpolator(), null, 0, titleHeight);
            AnimUtils.height(tip, time, new DecelerateInterpolator(), null, 0, tipHeight);
            AnimUtils.height(selectLayout, time, new DecelerateInterpolator(), null, 0, selectLayoutHeight);

        } else if (typeFlag == Note.TYPE_INT_SIMPLE) {

            this.type.setText("速 写");

            AnimUtils.height(title, time, new DecelerateInterpolator(), null, titleHeight, 0);
            AnimUtils.height(tip, time, new DecelerateInterpolator(), null, tipHeight, 0);
            AnimUtils.height(selectLayout, time, new DecelerateInterpolator(), null, selectLayoutHeight, 0);
        }
    }

    /**
     * 是否来自修改或展示
     *
     * @return
     */
    private boolean isFromUpdate() {
        if ("update".equals(from)) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        //❗️这里的dialog需要整合
        //这里提醒：还没保存！
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("便签还没有保存 确定退出?")
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(EnterAnim.FromBottom);
                    }
                })
                .setNegativeButton("再想想", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        dialog.show();
    }
}
