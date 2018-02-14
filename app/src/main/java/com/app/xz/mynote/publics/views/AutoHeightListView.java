package com.app.xz.mynote.publics.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import com.app.xz.mynote.publics.core.model.Status;

/**
 * Created by dixon.xu on 2018/2/1.
 *
 * scrollView内嵌listView时 使用该listView自适应高度
 */

public class AutoHeightListView extends ListView {

    public AutoHeightListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoHeightListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoHeightListView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (Status.isNoteItemLongClick){
            return false;
        }
        return super.onInterceptTouchEvent(event);
    }
}
