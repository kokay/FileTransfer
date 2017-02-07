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
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class FileSendingFragment extends Fragment {
    private static final String REMOTE_IP_ADDRESS = "Remote IP Address";
    private static final int NOT_STARTED = 0;
    private static final int RUNNING = 1;
    private static final int DONE = 2;

    private Socket connection;
    private InetAddress remoteIpAddress;
    private int state = NOT_STARTED;

    private List<FileInfo> fileListLocal = new ArrayList<>();
    private FileListAdapter fileListAdapter;

    private SendFileListTask sendFileListTask;
    private SendFilesTask sendFilesTask;

    private ListView fileListView;
    private Button readyButton;
    private Button cancelButton;
    private Button doneButton;

    private View.OnClickListener readyButtonClickListener;
    private View.OnClickListener cancelButtonClickListener;
    private View.OnClickListener doneButtonClickListener;

    public FileSendingFragment() {}

    public static FileSendingFragment newInstance(InetAddress remoteIpAddress) {
        FileSendingFragment fragment = new FileSendingFragment();
        Bundle args = new Bundle();
        args.putSerializable(REMOTE_IP_ADDRESS, remoteIpAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        remoteIpAddress = (InetAddress)getArguments().getSerializable(REMOTE_IP_ADDRESS);
        connection = ((FileSendingActivity) getActivity()).getConnection(remoteIpAddress);

        for (FileInfo fileInfo : ((FileSendingActivity)getActivity()).getSelectedFiles())
            fileListLocal.add(new FileInfo(fileInfo));

        fileListAdapter = new FileListAdapter(getContext(), fileListLocal);

        readyButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = RUNNING;
                showButtons();
                sendFileListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        };

        cancelButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendFileListTask.getStatus() == AsyncTask.Status.RUNNING) {
                    sendFileListTask.cancel(true);
                } else if (sendFilesTask.getStatus() == AsyncTask.Status.RUNNING) {
                    sendFilesTask.cancel(true);
                }
            }
        };

        doneButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };

        sendFileListTask = new SendFileListTask();
        sendFilesTask = new SendFilesTask();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_sending, container, false);
        fileListAdapter = new FileListAdapter(getContext(), fileListLocal);
        fileListView = (ListView) view.findViewById(R.id.processing_file_list);
        fileListView.setAdapter(fileListAdapter);

        readyButton = (Button) view.findViewById(R.id.ready_button);
        readyButton.setOnClickListener(readyButtonClickListener);

        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(cancelButtonClickListener);

        doneButton = (Button) view.findViewById(R.id.done_button);
        doneButton.setOnClickListener(doneButtonClickListener);

        showButtons();
        return view;
    }

    private void showButtons() {
        switch (state) {
            case NOT_STARTED :
                readyButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                doneButton.setVisibility(View.GONE);
                break;
            case RUNNING :
                readyButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
                doneButton.setVisibility(View.GONE);
                break;
            case DONE :
                readyButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                doneButton.setVisibility(View.VISIBLE);
                break;
        }
    }
    public String getNickName() {
        if (remoteIpAddress == null) return "";
        return remoteIpAddress.toString();
    }


    private void deleteThisFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(this).commit();
        ((FileSendingActivity)getActivity()).removeConnection(remoteIpAddress);
    }


    private class SendFileListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            Writer out = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                for (FileInfo fileInfo : fileListLocal) {
                    out.write(fileInfo.getName() + " " + fileInfo.length() + " " + fileInfo.getFileType() + "\r\n");
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
            if (success) {
                sendFilesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {

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
                    if (isCancelled()) return;
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
            state = DONE;
            showButtons();
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
