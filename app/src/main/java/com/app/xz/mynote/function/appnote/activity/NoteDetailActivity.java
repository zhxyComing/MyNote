package com.app.xz.mynote.function.appnote.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.app.xz.mynote.R;
import com.app.xz.mynote.function.appnote.model.Note;
import com.app.xz.mynote.publics.core.component.BaseActivity;
import com.app.xz.mynote.publics.core.db.NoteDB;
import com.app.xz.mynote.publics.core.utils.ToastUtils;
import com.app.xz.mynote.publics.views.EditTextPlus;

public class NoteDetailActivity extends BaseActivity {

    private EditTextPlus noteDetail;
    private Note note;
    private ImageView noteEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
    }

    @Override
    protected void init() {
        //获取透传的note
        note = getIntent().getParcelableExtra("note");

        noteDetail.setContent(note.content);
        //使用setSelection 重定位内容位置
        noteDetail.setSelection(0);

        //进入修改页面
        noteEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoteDetailActivity.this, AddNoteActivity.class);
                intent.putExtra("from", "update");
                intent.putExtra("note", note);
                startActivityForResult(intent, HomeActivity.OPEN_TO_UPDATE_NOTE, BaseActivity.EnterAnim.FromBottom);
            }
        });
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        noteDetail = findViewById(R.id.note_detail);
        noteEdit = findViewById(R.id.note_edit);
    }

    @Override
    public void onBackPressed() {
        finish(EnterAnim.FromBottom);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Note temp = NoteDB.getInstance(this).query(note.id);
            if (temp != null) {
                //如果有修改 重新获取内容 并且告知前一页内容有变化
                noteDetail.setContent(temp.content);
                setResult(RESULT_OK);
                this.note = temp;
            } else {
                ToastUtils.toast(NoteDetailActivity.this, "error");
            }
        }
    }

}
