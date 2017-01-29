package com.kokayapp.filetransfer.SendFiles.FileSending;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.FileListAdapter;
import com.kokayapp.filetransfer.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class FileSendingFragment extends Fragment {
    private static final String NICK_NAME = "NICK_NAME";

    private Socket connection;
    private String nickName;
    private List<FileInfo> fileListLocal = new ArrayList<>();
    private FileListAdapter fileListAdapter;
    private SendFilesTask sendFilesTask;

    private ListView fileListView;
    private ProgressBar processingFileProgressBar;
    private TextView processingFileStatus;
    private Button button;

    private View.OnClickListener sendOnClickListener;
    private View.OnClickListener cancelOnClickListener;
    private View.OnClickListener doneOnClickListener;

    public FileSendingFragment() {}

    public static FileSendingFragment newInstance(String nickName) {
        FileSendingFragment fragment = new FileSendingFragment();
        Bundle args = new Bundle();
        args.putString(NICK_NAME, nickName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nickName = getArguments().getString(NICK_NAME);
        connection = ((FileSendingActivity) getActivity()).getConnection(nickName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_processing_page, container, false);
        fileListAdapter = new FileListAdapter(getContext(), fileListLocal);

        fileListView = (ListView) view.findViewById(R.id.processing_file_list);
        fileListView.setAdapter(fileListAdapter);

        sendFilesTask = new SendFilesTask();
        new SendFileListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return view;
    }

    private void setUpOnClickListeners() {
        sendOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setOnClickListener(cancelOnClickListener);
                sendFilesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        };

        cancelOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFilesTask.cancel(true);
            }
        };

        doneOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteThisFragment();
            }
        };
    }

    private void deleteThisFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(this).commit();
        ((FileSendingActivity)getActivity()).removeConnection(nickName);
    }
    public String getNickName() {
        return nickName;
    }

    private class SendFileListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            Writer out = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                System.out.println("check file list size " + ((FileSendingActivity)getActivity()).getSelectedFiles().size());
                for (FileInfo fileInfo : ((FileSendingActivity)getActivity()).getSelectedFiles()) {
                    fileListLocal.add(new FileInfo(fileInfo));
                    out.write(fileInfo.getName() + " " + fileInfo.length() + "\r\n");
                    System.out.println("check " + fileInfo.getName() + " " + fileInfo.length());
                }
                out.write("\r\n");
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            processingFileProgressBar.setVisibility(View.GONE);
            if (success) {
                processingFileStatus.setVisibility(View.GONE);
                fileListView.setVisibility(View.VISIBLE);
                button.setVisibility(View.VISIBLE);
                fileListAdapter.notifyDataSetChanged();
            } else {
                processingFileStatus.setVisibility(View.GONE);
            }
        }
    }

    private class SendFilesTask extends AsyncTask<Void, Void, Void> {
        private byte[] buf = new byte[1024 * 3];

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("send files task started!");
        }

        @Override
        protected Void doInBackground(Void... params) {
            BufferedOutputStream out = null;
            BufferedReader in = null;
            try {
                out = new BufferedOutputStream(connection.getOutputStream());
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                for(String line = in.readLine(); line != null && !line.isEmpty(); line = in.readLine())
                    sendFile(fileListLocal.get(Integer.parseInt(line)), out);

            }catch(IOException e){
                e.printStackTrace();
            }finally{
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private void sendFile(FileInfo fileInfo, BufferedOutputStream out) {
            BufferedInputStream fin = null;
            try {
                fin = new BufferedInputStream(new FileInputStream(fileInfo));

                int count;
                while((fileInfo.getSoFar() != fileInfo.length()) && (count = fin.read(buf)) > 0) {
                    out.write(buf, 0, count);
                    fileInfo.addSoFar(count);
                    publishProgress();
                }
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                try {
                    fin.close();
                } catch (IOException e) {}
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            fileListAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            button.setOnClickListener(doneOnClickListener);
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
