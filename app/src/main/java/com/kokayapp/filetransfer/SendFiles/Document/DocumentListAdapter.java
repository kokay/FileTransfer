package com.kokayapp.filetransfer.SendFiles.Document;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import static com.kokayapp.filetransfer.FileInfo.BYTE;
import static com.kokayapp.filetransfer.FileInfo.BYTE_STRING;
import static com.kokayapp.filetransfer.FileInfo.GIGABYTE;
import static com.kokayapp.filetransfer.FileInfo.GIGABYTE_STRING;
import static com.kokayapp.filetransfer.FileInfo.KILOBYTE;
import static com.kokayapp.filetransfer.FileInfo.KILOBYTE_STRING;
import static com.kokayapp.filetransfer.FileInfo.MEGABYTE;
import static com.kokayapp.filetransfer.FileInfo.MEGABYTE_STRING;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.fileList;

/**
 * Created by Koji on 12/26/2016.
 */

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

    public DocumentListAdapter(Context context, Cursor c) {
        super(context, c, 0);
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
        if (!fileList.contains(new FileInfo(c.getString(DOCUMENT_DATA)))) {
            ((CheckBox) view.findViewById(R.id.file_check_box)).setChecked(false);
        } else {
            ((CheckBox) view.findViewById(R.id.file_check_box)).setChecked(true);
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
            return String.format("%.2f" + GIGABYTE_STRING, (size) / (float) GIGABYTE);
        else if (size >= MEGABYTE)
            return String.format("%.2f" + MEGABYTE_STRING, (size) / (float) MEGABYTE);
        else if (size >= KILOBYTE)
            return String.format("%.2f" + KILOBYTE_STRING, (size) / (float) KILOBYTE);
        return String.format("%.2f" + BYTE_STRING, (size) / (float) BYTE);
    }
}
