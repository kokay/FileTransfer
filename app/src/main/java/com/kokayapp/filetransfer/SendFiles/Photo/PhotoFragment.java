package com.kokayapp.filetransfer.SendFiles.Photo;

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


public class PhotoFragment extends Fragment {
    private Cursor cursor;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_grid, container, false);
        ((GridView) view.findViewById(R.id.file_grid)).setAdapter(new PhotoGridAdapter(getContext(), cursor));
        return view;
    }

    @Override
    public String toString() {
        return "Photo";
    }
}
