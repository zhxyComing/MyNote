package com.app.xz.mynote.publics.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.app.xz.mynote.publics.core.model.Status;

/**
 * Created by dixon.xu on 2018/2/2.
 */

public class NoteListScrollView extends ScrollView {

    public NoteListScrollView(Context context) {
        super(context);
    }

    public NoteListScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoteListScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (Status.isNoteItemLongClick) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
