package com.kokayapp.filetransfer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import static com.kokayapp.filetransfer.FileInfo.TYPE_AUDIO;
import static com.kokayapp.filetransfer.FileInfo.TYPE_DOC;
import static com.kokayapp.filetransfer.FileInfo.TYPE_PDF;
import static com.kokayapp.filetransfer.FileInfo.TYPE_PHOTO;
import static com.kokayapp.filetransfer.FileInfo.TYPE_TXT;
import static com.kokayapp.filetransfer.FileInfo.TYPE_VIDEO;

/**
 * Created by Koji on 12/26/2016.
 */

public class FileListAdapter extends ArrayAdapter {
    private final Bitmap photoImage;
    private final Bitmap videoImage;
    private final Bitmap audioImage;
    private final Bitmap pdfImage;
    private final Bitmap docImage;
    private final Bitmap txtImage;

    private List<FileInfo> fileList;
    public FileListAdapter(Context context, List<FileInfo> fileList) {
        super(context, 0, fileList);
        this.fileList = fileList;

        photoImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.photo);
        videoImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.video);
        audioImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.audio);
        pdfImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.pdf);
        docImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.doc);
        txtImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.txt);
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public FileInfo getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.processing_file, parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.processing_file_title);
        title.setText(fileList.get(position).getName());

        TextView progressTv = (TextView) convertView.findViewById(R.id.processing_file_progress_tv);
        progressTv.setText(fileList.get(position).getStringProgress());

        ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.processing_file_progress_bar);
        progressBar.setProgress(fileList.get(position).getIntProgress());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.processing_file_image);
        imageView.setImageBitmap(getImage(fileList.get(position).getFileType()));

        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.processing_file_check_box);
        checkBox.setChecked(fileList.get(position).isChecked());

        return convertView;
    }

    private Bitmap getImage(int fileType) {
        switch (fileType) {
            case TYPE_PHOTO : return photoImage;
            case TYPE_VIDEO : return videoImage;
            case TYPE_AUDIO : return audioImage;
            case TYPE_PDF : return pdfImage;
            case TYPE_DOC : return docImage;
            case TYPE_TXT : return txtImage;
        }
        return null;
    }
}
