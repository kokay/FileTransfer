package com.kokayapp.filetransfer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Koji on 12/26/2016.
 */

public class FileListAdapter extends ArrayAdapter {
    private List<FileInfo> fileList;

    public FileListAdapter(Context context, List<FileInfo> fileList) {
        super(context, 0, fileList);
        this.fileList = fileList;
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

        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.processing_file_check_box);
        checkBox.setChecked(fileList.get(position).isChecked());

        return convertView;
    }
}
