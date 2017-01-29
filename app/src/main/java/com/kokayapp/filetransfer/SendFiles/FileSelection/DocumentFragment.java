package com.kokayapp.filetransfer.SendFiles.FileSelection;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.R;

import static com.kokayapp.filetransfer.FileInfo.*;


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

    public class DocumentListAdapter extends CursorAdapter {
        private final int DOCUMENT_TITLE;
        private final int DOCUMENT_SIZE;
        private final int DOCUMENT_DATA;
        private final int DOCUMENT_TYPE;

        private final String PDF = "pdf";
        private final String TXT = "text";
        private final String DOC = "doc";
        private final Bitmap pdfImage;
        private final Bitmap docImage;
        private final Bitmap txtImage;
        private FileSelectionActivity fileSelectionActivity;

        public DocumentListAdapter(Context context, Cursor c) {
            super(context, c, 0);

            fileSelectionActivity = (FileSelectionActivity) context;
            DOCUMENT_TITLE = c.getColumnIndex(MediaStore.Files.FileColumns.TITLE);
            DOCUMENT_SIZE = c.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
            DOCUMENT_DATA = c.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            DOCUMENT_TYPE = c.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);

            pdfImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.pdf);
            docImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.doc);
            txtImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.txt);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.document, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor c) {
            ((TextView) view.findViewById(R.id.document_title)).setText(c.getString(DOCUMENT_TITLE));
            ((TextView) view.findViewById(R.id.document_size)).setText(getSizeText(c.getInt(DOCUMENT_SIZE)));
            ((ImageView) view.findViewById(R.id.file_image)).setImageBitmap(getImage(c.getString(DOCUMENT_TYPE)));
            if (fileSelectionActivity.contains(new FileInfo(c.getString(DOCUMENT_DATA)))) {
                ((CheckBox) view.findViewById(R.id.file_check_box)).setChecked(true);
            } else {
                ((CheckBox) view.findViewById(R.id.file_check_box)).setChecked(false);
            }
        }

        private Bitmap getImage(String mineType) {
            if (mineType.contains(PDF)) return pdfImage;
            if (mineType.contains(TXT)) return txtImage;
            if (mineType.contains(DOC)) return docImage;
            return null;
        }

        private String getSizeText(long size) {
            if(size >= GIGABYTE)
                return String.format("%.2f" + GIGABYTE_STRING, size / (float) GIGABYTE);
            else if (size >= MEGABYTE)
                return String.format("%.2f" + MEGABYTE_STRING, size / (float) MEGABYTE);
            else if (size >= KILOBYTE)
                return String.format("%.2f" + KILOBYTE_STRING, size / (float) KILOBYTE);
            return String.format("%.2f" + BYTE_STRING, size / (float) BYTE);
        }
    }
}
