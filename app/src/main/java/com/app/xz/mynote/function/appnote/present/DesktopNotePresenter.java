package com.app.xz.mynote.function.appnote.present;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.xz.mynote.R;
import com.app.xz.mynote.function.appnote.model.Note;
import com.app.xz.mynote.publics.core.interfaces.Presenter;
import com.app.xz.mynote.publics.core.utils.AnimUtils;
import com.app.xz.mynote.publics.core.utils.DeviceUtils;
import com.app.xz.mynote.publics.core.utils.handler.HandlerUtils;
import com.app.xz.mynote.publics.views.EditTextPlus;

/**
 * Created by dixon.xu on 2018/2/2.
 */

public class DesktopNotePresenter implements Presenter, GestureDetector.OnGestureListener {

    private Activity context;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayout;

    private View desktopLayout;

    //DP
    private static int DESKTOP_WIDTH = 0;
    private static int DESKTOP_HEIGHT = 0;

    private Note desktopNote;

    private GestureDetectorCompat mDetector;

    private float downX, downY;
    private boolean isLongPress;

    //桌签组件 destroy时记得销毁
    private LinearLayout desktopTopBar;
    private EditTextPlus desktopContent;
    private CardView desktopCardLayout;

    public DesktopNotePresenter(Activity context) {
        this.context = context;
        initDesktopParams();
    }

    private void initDesktopParams() {
        //1 取得系统窗体
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        //2 设置窗体的布局样式
        mLayout = new WindowManager.LayoutParams();

        //设置窗体焦点及触摸
        mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //type要求悬浮窗权限
        mLayout.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        //设置显示的模式
        mLayout.format = PixelFormat.RGBA_8888;

        //设置对齐的边
        //★注意：这里是对齐不是位置，设置left和top，会让视图位移时以屏幕左上角为00点
        mLayout.gravity = Gravity.LEFT + Gravity.TOP;

        DESKTOP_WIDTH = DeviceUtils.getScreenWidth(context) - DeviceUtils.dp2px(context, 30) - DeviceUtils.dp2px(context, 50);//分别减去margin（两个边各10 另listView right 10）和timeLayout
        DESKTOP_HEIGHT = DeviceUtils.dp2px(context, 150);

        //桌签移动动画的手势操作 如长按
        mDetector = new GestureDetectorCompat(context, this);

        //设置长按事件的监听 这里设置监听时虽然其desktopContent是空的 但是由于调用到其listener时，desktopContent一定不为空 一定被赋值了 所以没有问题
        //❗另外可以看出 desktopContent是个成员变量 是个引用 其后的值随引用的变化而变化 即不是设置时为空就是空 以后设置也可以！ 这里不是方法中参数的值传递！
        initDesktopLongPressListener();
    }

    public void create(float x, float y, Note note) {

        mLayout.x = (int) x + DeviceUtils.dp2px(context, 50);
        mLayout.y = (int) y;

        int type = note.type;

        desktopLayout = LayoutInflater.from(context).inflate(R.layout.item_note_desktop, null);
        TextView title = desktopLayout.findViewById(R.id.title);
        TextView tip = desktopLayout.findViewById(R.id.tip);

        if (type == Note.TYPE_INT_NORMAL) {

            if (title.getVisibility() == View.GONE) {
                title.setVisibility(View.VISIBLE);
            }
            if (tip.getVisibility() == View.GONE || !TextUtils.isEmpty(note.tip)) {
                tip.setVisibility(View.VISIBLE);
            } else {
                tip.setVisibility(View.GONE);
            }

            title.setText(note.title);
            tip.setText(note.tip);

            if (!TextUtils.isEmpty(note.image)) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(note.image);
                    ((ImageView) desktopLayout.findViewById(R.id.image)).setImageBitmap(bitmap);
                } catch (Exception e) {
                    //图片转化失败 路径有问题 设置默认图
                    ((ImageView) desktopLayout.findViewById(R.id.image)).setImageResource(R.drawable.bg_note_normal);
                }
            } else {
                //设置默认图
                ((ImageView) desktopLayout.findViewById(R.id.image)).setImageResource(R.drawable.bg_note_normal);
            }
        } else {
            title.setVisibility(View.GONE);
            tip.setVisibility(View.GONE);

            //设置默认图
            ((ImageView) desktopLayout.findViewById(R.id.image)).setImageResource(R.drawable.bg_note_normal);
        }

        EditTextPlus editTextPlus = desktopLayout.findViewById(R.id.content);
        editTextPlus.setContent(note.content);
        editTextPlus.setSelection(0);
        editTextPlus.setReadOnly();

        desktopLayout.findViewById(R.id.layoutTime).setVisibility(View.GONE);
        desktopLayout.findViewById(R.id.flag_desktop_note).setVisibility(View.GONE);

        //设置窗体的宽度和高度 单位px
        mLayout.width = DESKTOP_WIDTH;
        mLayout.height = DESKTOP_HEIGHT;

        mWindowManager.addView(desktopLayout, mLayout);
    }

    public void updateLocation(int x, int y) {
        if (desktopLayout != null) {
            mLayout.x = x;
            //LayoutParams.y是不包括通知栏的，RawY包括
            mLayout.y = y;
            mWindowManager.updateViewLayout(desktopLayout, mLayout);
        }
    }

    public void destroy() {
        if (desktopLayout != null) {
            mWindowManager.removeView(desktopLayout);
            desktopLayout = null;
        }
    }

    public int getX() {
        return mLayout.x;
    }

    public int getY() {
        return mLayout.y;
    }

    public int getDesktopLayoutWidth() {
        return desktopLayout.getMeasuredWidth();
    }

    public int getDesktopLayoutHeight() {
        return desktopLayout.getMeasuredHeight();
    }

    @Override
    public void onActivityDestroy() {

    }

    /**
     * 初始化桌面便签
     * <p>
     * 这个方法只有在真正成为桌签后才被调用 create后不一定成为桌签 故不一定调用
     */
    public void init(Note note) {

        if (desktopLayout != null) {

            //以下的参数和监听 都是需要更换新的桌签时 二次设置的

            initDesktopViews(note);

            //布局的变化
            startHideAnim();

            initDesktopMoveEvent();

//            initDesktopLongPressListener();
        }
    }

    /**
     * 初始化桌面便签特有的组件和参数
     *
     * @param note
     */
    private void initDesktopViews(Note note) {

        this.desktopNote = note;

        desktopTopBar = desktopLayout.findViewById(R.id.top_bar);
        desktopContent = desktopLayout.findViewById(R.id.content);
        desktopCardLayout = desktopLayout.findViewById(R.id.card_content);
    }

    /**
     * 如果是桌签 还要销毁这些参数
     */
    private void destroyDesktopViews() {

        this.desktopNote = null;

        desktopTopBar = null;
        desktopContent = null;
        desktopCardLayout = null;

//        longPressListener = null;
    }

    /**
     * 长按滑动事件
     */
    private void initDesktopMoveEvent() {
        //因为需要将事件设置给新的组件 所以新组件不能为空 所以不能提前设置 必须获取到以后再设置事件
        desktopContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                DesktopNotePresenter.this.mDetector.onTouchEvent(event);

                if (isLongPress) {
                    move(event);
                }
                return false;
            }
        });
    }

    /**
     * 设置长按事件的监听
     */
    private void initDesktopLongPressListener() {
        longPressListener = new OnLongPressListener() {
            @Override
            public void onLongPress(boolean b) {
                isLongPress = b;
                desktopContent.setMoveStatus(b);

                //给出一个桌签可移动时的动效 尝试放大缩小不成功或者效果不好
                if (isLongPress) {
                    desktopCardLayout.setCardBackgroundColor(context.getResources().getColor(R.color.white));
                } else {
                    desktopCardLayout.setCardBackgroundColor(context.getResources().getColor(R.color.black));
                }
            }
        };
    }

    /**
     * title和tip的隐藏动画 桌签只显示content
     */
    private void startHideAnim() {
        //top_bar滑出
        AnimUtils.tranX(desktopTopBar, 600, new DecelerateInterpolator(), null, 0, -desktopTopBar.getMeasuredWidth());

        //延迟0.8s 执行content上移动画 实际是通过top_bar高度变化实现的
        HandlerUtils.executeDelayed(new Runnable() {
            @Override
            public void run() {

                HandlerUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AnimUtils.height(desktopTopBar, 600, new DecelerateInterpolator(), new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                desktopTopBar.setVisibility(View.GONE);
                                desktopContent.setTextColor(Color.WHITE);
                            }
                        }, desktopTopBar.getMeasuredHeight(), 0);
                    }
                });
            }
        }, 800);
    }


    /**
     * 桌签的move事件
     *
     * @param event
     */
    private void move(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //init
                if (downX == 0f && downY == 0f) {
                    downX = event.getX();
                    downY = event.getY();
                    return;
                }

                int x = DeviceUtils.clamp((int) (event.getRawX() - downX),
                        0,
                        DeviceUtils.getScreenWidth(context) - getDesktopLayoutWidth());//多减了时间layout的宽度 应该补回来
                //LayoutParams.y是不包括通知栏的，RawY包括
                int y = DeviceUtils.clamp((int) (event.getRawY() - downY - DeviceUtils.getStatusBarHeight(context) - DeviceUtils.dp2px(context, 5)),
                        0,
                        DeviceUtils.getScreenHeight(context) - getDesktopLayoutHeight());
                updateLocation(x, y);//x位置应该向右偏移一个时间layout的宽度 另外写这里的原因是：不影响其作为桌签的正常滑动

                break;
            case MotionEvent.ACTION_UP:

                downX = 0;
                downY = 0;

                if (longPressListener != null) {
                    longPressListener.onLongPress(false);
                }

                break;
        }

    }

    /**
     * 长按事件的监听 用于更换某些状态
     */
    private interface OnLongPressListener {
        void onLongPress(boolean b);
    }

    private OnLongPressListener longPressListener = null;

    @Override
    public void onLongPress(MotionEvent e) {
        if (longPressListener != null) {
            longPressListener.onLongPress(true);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
