package com.app.xz.mynote.publics.core.interfaces;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by dixon.xu on 2018/2/2.
 */

public interface OnListViewItemListener {

    void onItemTouch(View view, MotionEvent event, int position);

    void onItemLongClick(View view, int position);

    void onItemClick(View view, int position);
}
