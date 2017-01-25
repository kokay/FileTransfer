package com.kokayapp.filetransfer.SendFiles.Photo;

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

public class PhotoGridAdapter extends CursorAdapter {
    private final int IMAGE_ID;
    private final int IMAGE_DATA;

    public PhotoGridAdapter(Context context, Cursor c) {
        super(context, c, 0);

        IMAGE_ID = c.getColumnIndex(MediaStore.Images.Media._ID);
        IMAGE_DATA = c.getColumnIndex(MediaStore.Images.Media.DATA);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.photo, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView uri = (TextView) view.findViewById(R.id.title);
        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

        uri.setText(cursor.getString(IMAGE_DATA));
        loadThumbnail(context, thumbnail, cursor.getLong(IMAGE_ID));
    }

    public void loadThumbnail(Context context, ImageView imageView, long id) {
        if (cancelPotentialWork(imageView, id)) {
            final ThumbnailImageWorkerTask task = new ThumbnailImageWorkerTask(context, imageView, 0);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), null, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(id);
        }
    }
}

