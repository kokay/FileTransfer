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
import android.widget.AdapterView;
import android.widget.GridView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;
import com.kokayapp.filetransfer.SendFiles.FileSelectionActivity;


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

    @Override
    public String toString() {
        return "Video";
    }
}
