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
import android.widget.GridView;
import android.widget.ImageView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;

import static com.kokayapp.filetransfer.SendFiles.FileSelection.FileSelectionActivity.*;


public class VideoFragment extends Fragment {
    private Cursor cursor;
    private VideoGridAdapter videoGridAdapter;
    private int dataIndex;

    private Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private String[] projection = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.MINI_THUMB_MAGIC,
    };

    private String selection = null;
    private String[] selectionArgs = {};
    private String sortOrder = null;

    public static VideoFragment newInstance() {
        VideoFragment fragment = new VideoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cursor = new CursorLoader(getContext(), uri,
                projection, selection, selectionArgs, sortOrder).loadInBackground();
        videoGridAdapter = new VideoGridAdapter(getContext(), cursor);
        dataIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_grid, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.file_grid);
        gridView.setAdapter(videoGridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                FileInfo fileInfo = new FileInfo(cursor.getString(dataIndex));
                ((FileSelectionActivity)getActivity()).selectFile(fileInfo);
                videoGridAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    public class VideoGridAdapter extends CursorAdapter {
        private final int VIDEO_ID;
        private final int VIDEO_DATA;
        private final int VIDEO_TITLE;
        private final Bitmap videoImage;

        private FileSelectionActivity fileSelectionActivity;

        public VideoGridAdapter(Context context, Cursor c) {
            super(context, c, 0);

            fileSelectionActivity = (FileSelectionActivity) context;
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
            if (fileSelectionActivity.contains(new FileInfo(c.getString(VIDEO_DATA)))) {
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
                final ThumbnailImageWorkerTask task =
                        new ThumbnailImageWorkerTask(context, imageView, VIDEO_FRAGMENT);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(context.getResources(), videoImage, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(id);
            }
        }
    }

}
