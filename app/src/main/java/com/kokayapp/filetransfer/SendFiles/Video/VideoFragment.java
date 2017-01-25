package com.kokayapp.filetransfer.SendFiles.Video;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.kokayapp.filetransfer.R;


public class VideoFragment extends Fragment {
    private Cursor cursor;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_grid, container, false);
        ((GridView) view.findViewById(R.id.file_grid)).setAdapter(new VideoGridAdapter(getContext(), cursor));
        return view;
    }

    @Override
    public String toString() {
        return "Video";
    }
}
