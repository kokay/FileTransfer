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


public class PhotoFragment extends Fragment {
    private Cursor cursor;
    private PhotoGridAdapter photoGridAdapter;
    private int dataIndex;

    private Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DATA
    };

    private String selection = null;
    private String[] selectionArgs = {};
    private String sortOrder = null;


    public static PhotoFragment newInstance() {
        PhotoFragment fragment = new PhotoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cursor = new CursorLoader(getContext(), uri,
                projection, selection, selectionArgs, sortOrder).loadInBackground();
        photoGridAdapter = new PhotoGridAdapter(getContext(), cursor);
        dataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_grid, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.file_grid);
        gridView.setAdapter(photoGridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                FileInfo fileInfo = new FileInfo(cursor.getString(dataIndex));
                ((FileSelectionActivity)getActivity()).selectFile(fileInfo);
                photoGridAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    public class PhotoGridAdapter extends CursorAdapter {
        private final int IMAGE_ID;
        private final int IMAGE_DATA;
        private final int IMAGE_TITLE;
        private final Bitmap photoImage;

        private FileSelectionActivity fileSelectionActivity;

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
            if (fileSelectionActivity.contains(new FileInfo(c.getString(IMAGE_DATA)))) {
                view.findViewById(R.id.check_image).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.check_image).setVisibility(View.INVISIBLE);
            }

            ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail);
            long id = c.getLong(IMAGE_ID);
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
}
