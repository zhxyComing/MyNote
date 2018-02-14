package com.app.xz.mynote.function.appnote.present;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.widget.ListView;

import com.app.xz.mynote.R;
import com.app.xz.mynote.function.appnote.activity.AddNoteActivity;
import com.app.xz.mynote.function.appnote.activity.HomeActivity;
import com.app.xz.mynote.function.appnote.activity.NoteDetailActivity;
import com.app.xz.mynote.function.appnote.adapter.NoteListAdapter;
import com.app.xz.mynote.function.appnote.model.Note;
import com.app.xz.mynote.publics.core.component.BaseActivity;
import com.app.xz.mynote.publics.core.db.NoteDB;
import com.app.xz.mynote.publics.core.interfaces.OnListViewItemListener;
import com.app.xz.mynote.publics.core.interfaces.Presenter;
import com.app.xz.mynote.publics.core.model.Status;
import com.app.xz.mynote.publics.core.utils.AlertPermissionHelper;
import com.app.xz.mynote.publics.core.utils.AnimUtils;
import com.app.xz.mynote.publics.core.utils.DeviceUtils;
import com.app.xz.mynote.publics.core.utils.ToastUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by dixon.xu on 2018/2/1.
 */

public class HomeNotePresenter implements Presenter, OnListViewItemListener {

    private Activity context;

    private ListView noteListView;
    private NoteListAdapter adapter;
    private List<Note> list;

    private View itemMoveTip;
    private View itemDeleteTip;

    private DesktopNotePresenter desktopNotePresenter;

    //用于控制桌面便签的移动 因为item的长按位移是能让desktopLayout位移的 所以需要将位置传给desktopPresenter
    private float downX = 0f;
    private float downY = 0f;

    public HomeNotePresenter(Activity context, ListView listView, View itemMoveTip, View itemDeleteTip, DesktopNotePresenter desktopNotePresenter) {
        this.context = context;
        this.noteListView = listView;
        this.desktopNotePresenter = desktopNotePresenter;
        this.itemMoveTip = itemMoveTip;
        this.itemDeleteTip = itemDeleteTip;
    }

    public void init() {
        //从数据库获取参数
        list = NoteDB.getInstance(context).query();
        if (list.size() == 0) {
            NoteDB.getInstance(context).insert("小便签欢迎你", "这里能看到便签的具体内容\n全新改版的小便签，增强了交互性的同时，更加关注效率与安全\n\n说明\n1.长按拖动到提示区域，即可设置为桌面便签\n2.长按拖动到下边红色区域，即可删除这条便签\n3.便签还可以为您保存图片哦", "引导说明", new Date().getTime(), "", Note.TYPE_INT_NORMAL, Note.DISPLAY_TYPE_NORMAL);
            list.addAll(NoteDB.getInstance(context).query());
        }
        adapter = new NoteListAdapter(context, list, this);
        noteListView.setAdapter(adapter);

        LayoutAnimationController controller = new LayoutAnimationController(
                AnimationUtils.loadAnimation(context, R.anim.anim_note_list_layout));
        controller.setDelay(0.5f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        noteListView.setLayoutAnimation(controller);
    }

    public void update() {
        list.clear();
        list.addAll(NoteDB.getInstance(context).query());
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onActivityDestroy() {

    }

    /**
     * item的touch事件 这里用来控制desktopLayout移动
     *
     * @param view
     * @param event
     * @param position
     */
    @Override
    public void onItemTouch(View view, MotionEvent event, int position) {
        //进入长按状态 item的touch事件才会被监听 以将位移传给desktopLayout
        if (Status.isNoteItemLongClick) {
            itemMove(event, view, position);
        }
    }

    /**
     * item的long click事件 这里用来启动itemMove动画
     *
     * @param view
     * @param position
     */
    @Override
    public void onItemLongClick(View view, int position) {

        //创建一个原位置的desktopLayout
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        //这里很奇怪 华为即使不减这个72 高度也是正常的 小米就不正常；然而减去72 两款手机都正常 也就是说减去不减去72对华为没影响...wtf
        desktopNotePresenter.create(location[0], location[1] - DeviceUtils.getStatusBarHeight(context), list.get(position));

        //原位置的item隐藏
        view.setVisibility(View.INVISIBLE);

        //设置为长按状态
        setLongClickStatus(true);
    }

    @Override
    public void onItemClick(View view, int position) {
//        Intent intent = new Intent(context, AddNoteActivity.class);
        Intent intent = new Intent(context, NoteDetailActivity.class);
        intent.putExtra("from", "update");
        intent.putExtra("note", list.get(position));
        ((HomeActivity) context).startActivityForResult(intent, HomeActivity.OPEN_TO_UPDATE_NOTE, BaseActivity.EnterAnim.FromBottom);
    }

    /**
     * 每个Item长按时的随意拖动
     *
     * @param event
     * @param view
     */
    private void itemMove(MotionEvent event, View view, int position) {

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
                        -DeviceUtils.dp2px(context, 50),//x位置的偏移 因为少了时间layout
                        DeviceUtils.getScreenWidth(context) - desktopNotePresenter.getDesktopLayoutWidth() + DeviceUtils.dp2px(context, 50));//多减了时间layout的宽度 应该补回来
                //LayoutParams.y是不包括通知栏的，RawY包括
                int y = DeviceUtils.clamp((int) (event.getRawY() - downY - DeviceUtils.getStatusBarHeight(context)),
                        0,
                        DeviceUtils.getScreenHeight(context) - desktopNotePresenter.getDesktopLayoutHeight());
                desktopNotePresenter.updateLocation(x + DeviceUtils.dp2px(context, 50), y);//x位置应该向右偏移一个时间layout的宽度 另外写这里的原因是：不影响其作为桌签的正常滑动

                break;
            case MotionEvent.ACTION_UP:

                downX = 0;
                downY = 0;

                desktopLayoutGo((int) event.getRawY(), position);

                deleteItemGo((int) event.getRawY(), position);

                //无论结果如何 状态要回归
                view.setVisibility(View.VISIBLE);
                setLongClickStatus(false);
                break;
        }

    }

    private void deleteItemGo(int y, final int position) {
        if (y > DeviceUtils.getScreenHeight(context) - DeviceUtils.dp2px(context, 100)) {

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage("确定删除这条便签吗?")
                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            list.get(position).deleteFromDB(context);
                            update();
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

    /**
     * 根据最后结果 控制临时desktopLayout是成为桌签 还是回归
     * <p>
     * 目前设定为距离顶栏120dp就成为桌签
     */
    private void desktopLayoutGo(int y, int position) {
        if (AlertPermissionHelper.checkAlertPermission(context) && y < DeviceUtils.dp2px(context, 120)) {
            //成为桌签
            context.moveTaskToBack(true);
            //更新数据库标记
            list.get(position).setDesktopNote(context);
            //init桌面便签
            desktopNotePresenter.init(list.get(position));
        } else {
            //销毁临时桌签
            desktopNotePresenter.destroy();
        }
    }


    /**
     * 设置长按的状态 以及状态改变时 tip提示窗口的变化
     *
     * @param b
     */
    private void setLongClickStatus(boolean b) {
        Status.isNoteItemLongClick = b;
        if (b) {
            itemMoveTip.setVisibility(View.VISIBLE);
            AnimUtils.alpha(itemMoveTip, 600, new DecelerateInterpolator(), null, 0, 1);

            //删除tip显现动画
            itemDeleteTip.setVisibility(View.VISIBLE);
            AnimUtils.alpha(itemDeleteTip, 600, new DecelerateInterpolator(), null, 0, 1);
            AnimUtils.tranY(itemDeleteTip, 600, new DecelerateInterpolator(), null, 300, 0);
        } else {

            AnimUtils.alpha(itemMoveTip, 600, new DecelerateInterpolator(), new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    itemMoveTip.setVisibility(View.GONE);
                }
            }, 1, 0);

            //删除tip消失动画 以后会添加组合动画
            AnimUtils.alpha(itemDeleteTip, 600, new DecelerateInterpolator(), new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    itemDeleteTip.setVisibility(View.GONE);
                }
            }, 1, 0);
//            AnimUtils.tranY(itemDeleteTip, 600, new DecelerateInterpolator(), null, 0, 300);
        }
    }
}
