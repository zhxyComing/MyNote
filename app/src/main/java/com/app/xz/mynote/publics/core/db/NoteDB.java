package com.app.xz.mynote.publics.core.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.app.xz.mynote.function.appnote.model.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dixon.xu on 2018/2/1.
 * <p>
 * 查看旧代码
 */

public class NoteDB {

    private static NoteDB noteDB;

    private NoteDBHelper noteDBHelper;

    private NoteDB(Context context) {
        noteDBHelper = new NoteDBHelper(context);
    }

    public static NoteDB getInstance(Context context) {
        if (noteDB == null) {
            synchronized (NoteDB.class) {
                if (noteDB == null) {
                    noteDB = new NoteDB(context);
                }
            }
        }
        return noteDB;
    }

    /**
     * 添加Note
     *
     * @param title      题目
     * @param content    内容
     * @param tip        关键词
     * @param createTime 创建时间
     */
    public void insert(String title, String content, String tip, long createTime, String image, int type, String displayType) {
        SQLiteDatabase db = noteDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("tip", tip);
        values.put("createTime", createTime);//据说android sqlite integer可以存储long
        values.put("image", image);
        values.put("type", type);
        values.put("displayType", displayType);
        db.insert(NoteDBHelper.NOTE_TABLE_NAME, null, values);

        db.close();
    }

    public void insert(Note note) {
        SQLiteDatabase db = noteDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", note.title);
        values.put("content", note.content);
        values.put("tip", note.tip);
        values.put("createTime", note.createTime.getTime());//据说android sqlite integer可以存储long
        values.put("image", note.image);
        values.put("type", note.type);
        values.put("displayType", note.displayType);
        db.insert(NoteDBHelper.NOTE_TABLE_NAME, null, values);

        db.close();
    }

    /**
     * 更新note
     *
     * @param id
     * @param title
     * @param content
     * @param tip
     * @param createTime
     */
    public void update(int id, String title, String content, String tip, long createTime, String image, int type, String displayType) {
        SQLiteDatabase db = noteDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("tip", tip);
        values.put("createTime", createTime);//据说android sqlite integer可以存储long
        values.put("image", image);
        values.put("type", type);
        values.put("displayType", displayType);

        String whereClause = "id=?";
        String[] whereArgs = {String.valueOf(id)};
        db.update(NoteDBHelper.NOTE_TABLE_NAME, values, whereClause, whereArgs);

        db.close();
    }

    public void update(Note note) {
        SQLiteDatabase db = noteDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", note.title);
        values.put("content", note.content);
        values.put("tip", note.tip);
        values.put("createTime", note.createTime.getTime());//据说android sqlite integer可以存储long
        values.put("image", note.image);
        values.put("type", note.type);
        values.put("displayType", note.displayType);

        String whereClause = "id=?";
        String[] whereArgs = {String.valueOf(note.id)};
        db.update(NoteDBHelper.NOTE_TABLE_NAME, values, whereClause, whereArgs);

        db.close();
    }

    /**
     * 删除note
     *
     * @param id
     */
    public void delete(int id) {
        SQLiteDatabase db = noteDBHelper.getWritableDatabase();
        db.delete(NoteDBHelper.NOTE_TABLE_NAME, "id=?", new String[]{String.valueOf(id)});

        db.close();
    }

    public void delete(Note note) {
        SQLiteDatabase db = noteDBHelper.getWritableDatabase();
        db.delete(NoteDBHelper.NOTE_TABLE_NAME, "id=?", new String[]{String.valueOf(note.id)});

        db.close();
    }

    /**
     * 查询所有note数据
     * <p>
     * 注意:
     * 即使保存的是int 返回的也可以是String
     * AndroidSQLite即使保存的是integer 也能保存long型数据
     */
    public List<Note> query() {
        List<Note> list = new ArrayList<>();
        SQLiteDatabase db = noteDBHelper.getWritableDatabase();

        // 调用SQLiteDatabase对象的query方法进行查询
        // 返回一个Cursor对象：由数据库查询返回的结果集对象
        Cursor cursor = db.query(NoteDBHelper.NOTE_TABLE_NAME, null, null, null, null, null, null);

        //将光标移动到下一行，从而判断该结果集是否还有下一条数据
        //如果有则返回true，没有则返回false
        while (cursor.moveToNext()) {
            Note note = new Note(cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("content")),
                    cursor.getString(cursor.getColumnIndex("tip")),
                    new Date(cursor.getLong(cursor.getColumnIndex("createTime"))),
                    cursor.getString(cursor.getColumnIndex("image")),
                    cursor.getInt(cursor.getColumnIndex("type")),
                    cursor.getString(cursor.getColumnIndex("displayType")));
            //输出查询结果
            list.add(note);

        }
        //关闭数据库
        cursor.close();
        db.close();

        return list;
    }

    /**
     * 查询单个note数据
     */
    public Note query(int id) {
        Note note = null;
        SQLiteDatabase db = noteDBHelper.getWritableDatabase();
//        Cursor cursor = db.query( NoteDBHelper.NOTE_TABLE_NAME, new String[]{"id"},
//                null, new String[]{String.valueOf(id)},
//                null, null,
//                null, null);
        Cursor cursor = db.rawQuery("select * from " + NoteDBHelper.NOTE_TABLE_NAME + " where id=?", new String[]{String.valueOf(id)});
        if (cursor.moveToNext()) {
            note = new Note(cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("content")),
                    cursor.getString(cursor.getColumnIndex("tip")),
                    new Date(cursor.getLong(cursor.getColumnIndex("createTime"))),
                    cursor.getString(cursor.getColumnIndex("image")),
                    cursor.getInt(cursor.getColumnIndex("type")),
                    cursor.getString(cursor.getColumnIndex("displayType")));
        }
        cursor.close();
        db.close();
        return note;
    }
}
