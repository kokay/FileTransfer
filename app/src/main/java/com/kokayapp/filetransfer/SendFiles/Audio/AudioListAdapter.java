package com.kokayapp.filetransfer.SendFiles.Audio;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;
import com.kokayapp.filetransfer.SendFiles.FileSelectionActivity;

import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.cancelPotentialWork;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.ThumbnailImageWorkerTask;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.AsyncDrawable;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.fileList;

/**
 * Created by Koji on 12/26/2016.
 */

public class AudioListAdapter extends CursorAdapter{
    public AudioListAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.audio, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView t = (TextView) view.findViewById(R.id.audio_title);
        t.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
        t = (TextView) view.findViewById(R.id.audio_artist);
        t.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
        ImageView imageView = (ImageView) view.findViewById(R.id.file_image);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.device_check_box);
        FileInfo fileInfo = new FileInfo(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
        if (!fileList.contains(fileInfo)) checkBox.setChecked(false);
        else checkBox.setChecked(true);
        loadThumbnail(context, imageView, cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
    }

    public void loadThumbnail(Context context, ImageView imageView, long id) {
        if (cancelPotentialWork(imageView, id)) {
            final ThumbnailImageWorkerTask task = new FileSelectionActivity.ThumbnailImageWorkerTask(context, imageView, 2);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), null, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(id);
        }
    }


}
