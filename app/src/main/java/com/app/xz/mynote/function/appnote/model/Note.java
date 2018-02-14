package com.app.xz.mynote.function.appnote.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.app.xz.mynote.publics.core.db.NoteDB;

import java.util.Date;

/**
 * Created by dixon.xu on 2018/2/1.
 * <p>
 * Note model 包括对note的 增 删 改
 */

public class Note implements Parcelable {

    public static final int TYPE_INT_NORMAL = 0;//正常便签
    public static final int TYPE_INT_SIMPLE = 1;//简洁便签

    public static final String DISPLAY_TYPE_NORMAL = "normal";//应用内普通便签
    public static final String DISPLAY_TYPE_DESKTOP = "desktop";//应用外桌面便签

    public int id;
    public String title;
    public String content;
    public String tip;
    public Date createTime;
    public String image;
    public int type;
    public String displayType;

    public Note() {

    }

    public Note(String title, String content, String tip, Date createtime, String image, int type, String displayType) {
        this.title = title;
        this.content = content;
        this.tip = tip;
        this.createTime = createtime;
        this.image = image;
        this.type = type;
        this.displayType = displayType;
    }

    public Note(int id, String title, String content, String tip, Date createtime, String image, int type, String displayType) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.tip = tip;
        this.createTime = createtime;
        this.image = image;
        this.type = type;
        this.displayType = displayType;
    }

    protected Note(Parcel in) {
        id = in.readInt();
        title = in.readString();
        content = in.readString();
        tip = in.readString();
        image = in.readString();
        type = in.readInt();
        displayType = in.readString();
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    /**
     * 通过以下三个方法 可以实现对NoteDB很方便的单条操作 不用考虑DB 只关注Note本身
     *
     * @param context
     */
    public void addToDB(Context context) {
        if (id != 0) {
            //如果id不为0 说明数据库有记录 或者是人为的设置了id 这时是不允许添加到数据库的
            return;
        }
        NoteDB.getInstance(context).insert(this);
    }

    public void deleteFromDB(Context context) {
        if (id == 0) {
            // 如果id为0 说明数据库无记录 或者是人为改错了id 这时是不允许删除数据库数据的
            return;
        }
        NoteDB.getInstance(context).delete(this);
    }

    public void updateToDB(Context context) {
        if (id == 0) {
            // 如果id为0 说明数据库无记录 或者是人为改错了id 这时是不允许修改数据库的
            return;
        }
        NoteDB.getInstance(context).update(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(tip);
        dest.writeString(image);
        dest.writeInt(type);
        dest.writeString(displayType);
    }

    /**
     * 是否是桌签
     *
     * @return
     */
    public boolean isDesktopNote() {
        if (DISPLAY_TYPE_DESKTOP.equals(displayType)) {
            return true;
        }
        return false;
    }

    /**
     * 设置为桌签
     */
    public void setDesktopNote(Context context) {
        this.displayType = DISPLAY_TYPE_DESKTOP;
        updateToDB(context);
    }
}
