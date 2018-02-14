package com.app.xz.mynote.function.appnote.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.xz.mynote.R;
import com.app.xz.mynote.function.appnote.model.Note;
import com.app.xz.mynote.publics.core.interfaces.OnListViewItemListener;
import com.app.xz.mynote.publics.core.utils.TimeUtils;

import java.util.List;

/**
 * Created by dixon.xu on 2018/2/1.
 * <p>
 * 便签列表的adapter
 */

public class NoteListAdapter extends BaseAdapter {

    private Context context;
    private List<Note> list;
    private OnListViewItemListener itemListener;//item的点击事件 可以随意扩展 如长按 双击等

    public NoteListAdapter(Context context, List<Note> list, OnListViewItemListener itemListener) {
        this.context = context;
        this.list = list;
        this.itemListener = itemListener;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_note_list, null);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        initItem(vh, position);
        return convertView;
    }

    public static class ViewHolder {
        public TextView title;
        TextView content;
        TextView tip;
        TextView createTime;
        LinearLayout itemContent;
        ImageView image;
        ImageView desktopFlag;

        public ViewHolder(View view) {
            title = view.findViewById(R.id.title);
            content = view.findViewById(R.id.content);
            tip = view.findViewById(R.id.tip);
            createTime = view.findViewById(R.id.createTime);
            itemContent = view.findViewById(R.id.itemContent);
            image = view.findViewById(R.id.image);
            desktopFlag = view.findViewById(R.id.flag_desktop_note);
        }
    }

    /**
     * 给item设置参数
     *
     * @param vh
     * @param position
     */
    private void initItem(ViewHolder vh, final int position) {
        Note note = list.get(position);
        //普通便签模式
        if (note.type == Note.TYPE_INT_NORMAL) {

            if (vh.title.getVisibility() == View.GONE) {
                vh.title.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(note.tip)) {
                vh.tip.setVisibility(View.VISIBLE);
            } else {
                vh.tip.setVisibility(View.GONE);
            }

            vh.title.setText(note.title);
            vh.tip.setText(note.tip);

            setBackground(vh.image, note.image);
        } else {
            //简洁便签模式
            vh.title.setVisibility(View.GONE);
            vh.tip.setVisibility(View.GONE);

            /**
             * 这里以后可以更换简洁模式下的默认图
             */
            vh.image.setImageResource(R.drawable.bg_note_normal  );
        }

        //通用
        vh.content.setText(note.content);
        vh.createTime.setText(TimeUtils.format(note.createTime));

        if (note.isDesktopNote()) {
            vh.desktopFlag.setVisibility(View.VISIBLE);
        } else {
            vh.desktopFlag.setVisibility(View.GONE);
        }

        vh.itemContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (itemListener != null) {
                    itemListener.onItemTouch(v, event, position);
                }
                return false;
            }
        });

        vh.itemContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemListener != null) {
                    itemListener.onItemLongClick(v, position);
                }
                return true;
            }
        });

        vh.itemContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemListener != null) {
                    itemListener.onItemClick(v, position);
                }
            }
        });

    }

    /**
     * 设置Note卡片的背景
     *
     * @param image
     * @param imagePath
     */
    private void setBackground(ImageView image, String imagePath) {
        if (!TextUtils.isEmpty(imagePath)) {
            //这个note.image已经是压缩存储后返回的图片 并非原图
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                image.setImageBitmap(bitmap);
            } catch (Exception e) {
                //图片转化失败 路径有问题 设置默认图
                image.setImageResource(R.drawable.bg_note_normal);
            }
        } else {
            //如果为空 设置默认图
            image.setImageResource(R.drawable.bg_note_normal);
        }
    }

}
