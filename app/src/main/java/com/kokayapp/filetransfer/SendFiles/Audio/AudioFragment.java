package com.kokayapp.filetransfer.SendFiles.Audio;

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
import android.widget.CheckBox;
import android.widget.ListView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;
import com.kokayapp.filetransfer.SendFiles.FileSelectionActivity;


public class AudioFragment extends Fragment {
    private Cursor cursor;
    private AudioListAdapter audioListAdapter;

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

    private int dataIndex;
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

    @Override
    public String toString() {
        return "Audio";
    }
}
