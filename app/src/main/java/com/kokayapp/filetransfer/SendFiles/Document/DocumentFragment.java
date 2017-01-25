package com.kokayapp.filetransfer.SendFiles.Document;

import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;
import com.kokayapp.filetransfer.SendFiles.FileSelectionActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.fileList;

/**
 * Created by Koji on 12/26/2016.
 */

public class DocumentFragment extends Fragment {
    private Cursor cursor;
    private DocumentListAdapter documentListAdapter;
    private int dataIndex;

    private Uri uri = MediaStore.Files.getContentUri("external");
    private String[] projection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.SIZE
    };
    private String selection =
            MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
            MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
            MediaStore.Files.FileColumns.MIME_TYPE + "=?";
    private String[] selectionArgs = new String[]{
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf"),
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt"),
            MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc"),
    };
    private String sortOrder = null;


    public static DocumentFragment newInstance() {
        DocumentFragment fragment = new DocumentFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cursor = new CursorLoader(getContext(), uri, projection, selection,
                selectionArgs, sortOrder).loadInBackground();
        dataIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        documentListAdapter = new DocumentListAdapter(getContext(), cursor);
    }

    List<File> files = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_list, container, false);
        ListView listView = (ListView) view.findViewById(R.id.file_list);
        listView.setAdapter(documentListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                FileInfo fileInfo = new FileInfo(cursor.getString(dataIndex));
                ((FileSelectionActivity)getActivity()).selectFile(fileInfo);
                documentListAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    @Override
    public String toString() {
        return "Document";
    }
}
