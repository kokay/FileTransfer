package com.kokayapp.filetransfer.SendFiles.Document;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;

import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.fileList;

/**
 * Created by Koji on 12/26/2016.
 */

public class DocumentListAdapter extends CursorAdapter {
    public DocumentListAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.document, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView t = (TextView) view.findViewById(R.id.document_title);
        t.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE)));
        t = (TextView) view.findViewById(R.id.document_size);
        t.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)));
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.file_check_box);
        FileInfo fileInfo = new FileInfo(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
        if (!fileList.contains(fileInfo)) checkBox.setChecked(false);
        else checkBox.setChecked(true);
    }
}
