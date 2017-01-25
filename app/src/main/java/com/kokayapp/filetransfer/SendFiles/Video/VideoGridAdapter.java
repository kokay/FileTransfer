package com.kokayapp.filetransfer.SendFiles.Video;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;

import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.ThumbnailImageWorkerTask;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.AsyncDrawable;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.cancelPotentialWork;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.fileList;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.getBitmapFromCache;

/**
 * Created by Koji on 12/27/2016.
 */

public class VideoGridAdapter extends CursorAdapter {
    private final int VIDEO_ID;
    private final int VIDEO_DATA;
    private final int VIDEO_TITLE;
    private final Bitmap videoImage;

    public VideoGridAdapter(Context context, Cursor c) {
        super(context, c, 0);

        VIDEO_ID = c.getColumnIndex(MediaStore.Video.Media._ID);
        VIDEO_DATA = c.getColumnIndex(MediaStore.Video.Media.DATA);
        VIDEO_TITLE = c.getColumnIndex(MediaStore.Video.Media.TITLE);
        videoImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.video);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.photo_video, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor c) {
        if (fileList.contains(new FileInfo(c.getString(VIDEO_DATA)))) {
            view.findViewById(R.id.check_image).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.check_image).setVisibility(View.INVISIBLE);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail);
        long id = c. getLong(VIDEO_ID);
        Bitmap bitmap = getBitmapFromCache(id);

        if (bitmap != null) imageView.setImageBitmap(bitmap);
        else loadThumbnail(context, imageView, id);
    }

    public void loadThumbnail(Context context, ImageView imageView, long id) {
        if (cancelPotentialWork(imageView, id)) {
            final ThumbnailImageWorkerTask task = new ThumbnailImageWorkerTask(context, imageView, 1);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), videoImage, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(id);
        }
    }
}

