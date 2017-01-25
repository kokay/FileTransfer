package com.kokayapp.filetransfer.SendFiles.Video;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kokayapp.filetransfer.R;

import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.cancelPotentialWork;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.ThumbnailImageWorkerTask;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.AsyncDrawable;

/**
 * Created by Koji on 12/27/2016.
 */

public class VideoGridAdapter extends CursorAdapter {
    private final int VIDEO_ID;
    private final int VIDEO_DATA;

    public VideoGridAdapter(Context context, Cursor c) {
        super(context, c, 0);

        VIDEO_ID = c.getColumnIndex(MediaStore.Video.Media._ID);
        VIDEO_DATA = c.getColumnIndex(MediaStore.Video.Media.DATA);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.photo_video, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView uri = (TextView) view.findViewById(R.id.title);
        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

        uri.setText(cursor.getString(VIDEO_DATA));
        loadThumbnail(context, thumbnail, cursor.getLong(VIDEO_ID));
    }

    public void loadThumbnail(Context context, ImageView imageView, long id) {
        if (cancelPotentialWork(imageView, id)) {
            final ThumbnailImageWorkerTask task = new ThumbnailImageWorkerTask(context, imageView, 1);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), null, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(id);
        }
    }
}

