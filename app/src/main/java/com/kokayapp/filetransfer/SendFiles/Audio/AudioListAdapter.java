package com.kokayapp.filetransfer.SendFiles.Audio;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
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
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.getBitmapFromCache;

/**
 * Created by Koji on 12/26/2016.
 */

public class AudioListAdapter extends CursorAdapter{
    private final int AUDIO_TITLE;
    private final int AUDIO_ARTIST;
    private final int AUDIO_DATA;
    private final int AUDIO_ALBUM_ID;
    private final Bitmap audioImage;

    public AudioListAdapter(Context context, Cursor c) {
        super(context, c, 0);
        AUDIO_TITLE = c.getColumnIndex(MediaStore.Audio.Media.TITLE);
        AUDIO_ARTIST = c.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        AUDIO_DATA = c.getColumnIndex(MediaStore.Audio.Media.DATA);
        AUDIO_ALBUM_ID = c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        audioImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.audio);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.audio, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor c) {
        ((TextView) view.findViewById(R.id.audio_title)).setText(c.getString(AUDIO_TITLE));
        ((TextView) view.findViewById(R.id.audio_artist)).setText(c.getString(AUDIO_ARTIST));
        if (fileList.contains(new FileInfo(c.getString(AUDIO_DATA)))) {
            ((CheckBox) view.findViewById(R.id.device_check_box)).setChecked(true);
        } else {
            ((CheckBox) view.findViewById(R.id.device_check_box)).setChecked(false);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail);
        long id = c. getLong(AUDIO_ALBUM_ID);
        Bitmap bitmap = getBitmapFromCache(id);

        if (bitmap != null) imageView.setImageBitmap(bitmap);
        else loadThumbnail(context, imageView, id);
    }

    public void loadThumbnail(Context context, ImageView imageView, long id) {
        if (cancelPotentialWork(imageView, id)) {
            final ThumbnailImageWorkerTask task = new FileSelectionActivity.ThumbnailImageWorkerTask(context, imageView, 2);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), audioImage, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(id);
        }
    }


}
