package com.app.xz.mynote.publics.core.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dixon.xu on 2018/2/1.
 */

public class NoteDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "my_note.db";
    public static final String NOTE_TABLE_NAME = "note";

    public NoteDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + NOTE_TABLE_NAME + " (id integer primary key, title text, content text, tip text, createTime integer,image text,type integer,displayType text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
//        db.execSQL(sql);
//        onCreate(db);
    }
}
