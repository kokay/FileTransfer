package com.kokayapp.filetransfer.SendFiles.FileSelection;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;

import static com.kokayapp.filetransfer.SendFiles.FileSelection.FileSelectionActivity.*;

public class AudioFragment extends Fragment {
    private Cursor cursor;
    private AudioListAdapter audioListAdapter;
    private int dataIndex;

    private Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private String[] projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
    };
    private String selection = null;
    private String[] selectionArgs = null;
    private String sortOrder = null;

    public static AudioFragment newInstance() {
        AudioFragment fragment = new AudioFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cursor = new CursorLoader(getContext(), uri,
                projection, selection, selectionArgs, sortOrder).loadInBackground();
        dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        audioListAdapter = new AudioListAdapter(getContext(), cursor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_list, container, false);
        ListView listView = ((ListView) view.findViewById(R.id.file_list));
        listView.setAdapter(audioListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                FileInfo fileInfo = new FileInfo(cursor.getString(dataIndex));
                ((FileSelectionActivity)getActivity()).selectFile(fileInfo);
                audioListAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    public class AudioListAdapter extends CursorAdapter {
        private final int AUDIO_TITLE;
        private final int AUDIO_ARTIST;
        private final int AUDIO_DATA;
        private final int AUDIO_ALBUM_ID;
        private final Bitmap audioImage;

        private FileSelectionActivity fileSelectionActivity;

        public AudioListAdapter(Context context, Cursor c) {
            super(context, c, 0);
            AUDIO_TITLE = c.getColumnIndex(MediaStore.Audio.Media.TITLE);
            AUDIO_ARTIST = c.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            AUDIO_DATA = c.getColumnIndex(MediaStore.Audio.Media.DATA);
            AUDIO_ALBUM_ID = c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            audioImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.audio);
            fileSelectionActivity = (FileSelectionActivity) context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.audio, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor c) {
            ((TextView) view.findViewById(R.id.audio_title)).setText(c.getString(AUDIO_TITLE));
            ((TextView) view.findViewById(R.id.audio_artist)).setText(c.getString(AUDIO_ARTIST));
            if (fileSelectionActivity.contains(new FileInfo(c.getString(AUDIO_DATA)))) {
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
}
