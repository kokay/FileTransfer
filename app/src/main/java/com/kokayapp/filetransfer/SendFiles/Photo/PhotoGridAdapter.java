package com.kokayapp.filetransfer.SendFiles.Photo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;
import com.kokayapp.filetransfer.SendFiles.FileSelectionActivity;

import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.ThumbnailImageWorkerTask;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.cancelPotentialWork;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.AsyncDrawable;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.fileList;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.getBitmapFromCache;

/**
 * Created by Koji on 12/27/2016.
 */

public class PhotoGridAdapter extends CursorAdapter {
    private final int IMAGE_ID;
    private final int IMAGE_DATA;
    private final int IMAGE_TITLE;
    private final Bitmap photoImage;

    private FileSelectionActivity fileSelectionActivity;
    private LruCache<Long, Bitmap> memoryCache;


    public PhotoGridAdapter(Context context, Cursor c) {
        super(context, c, 0);

        IMAGE_ID = c.getColumnIndex(MediaStore.Images.Media._ID);
        IMAGE_DATA = c.getColumnIndex(MediaStore.Images.Media.DATA);
        IMAGE_TITLE = c.getColumnIndex(MediaStore.Images.Media.TITLE);
        photoImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.photo);

        fileSelectionActivity = (FileSelectionActivity) context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.photo_video, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor c) {
        if (fileList.contains(new FileInfo(c.getString(IMAGE_DATA)))) {
            view.findViewById(R.id.check_image).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.check_image).setVisibility(View.INVISIBLE);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail);
        long id = c. getLong(IMAGE_ID);
        Bitmap bitmap = getBitmapFromCache(id);

        if (bitmap != null) imageView.setImageBitmap(bitmap);
        else loadThumbnail(context, imageView, id);
    }

    public void loadThumbnail(Context context, ImageView imageView, long id) {
        if (cancelPotentialWork(imageView, id)) {
            final ThumbnailImageWorkerTask task = new ThumbnailImageWorkerTask(context, imageView, 0);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), photoImage, task);
            imageView.setImageDrawable(asyncDrawable);
            //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id);
            task.execute(id);
        }
    }
}

