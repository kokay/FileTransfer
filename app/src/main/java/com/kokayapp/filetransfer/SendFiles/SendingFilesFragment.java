package com.kokayapp.filetransfer.SendFiles;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.FileListAdapter;
import com.kokayapp.filetransfer.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.kokayapp.filetransfer.SendFiles.ClientSelectionActivity.connections;
import static com.kokayapp.filetransfer.SendFiles.FileSelectionActivity.fileList;

public class SendingFilesFragment extends Fragment {
    private static final String SOCKET_POSITION = "socket position";

    private Socket connection;
    private List<FileInfo> fileListLocal = new ArrayList<>();
    private FileListAdapter fileListAdapter;

    private SendFileListTask sendFileListTask;
    private SendFilesTask sendFilesTask;

    private ListView fileListView;
    private ProgressBar sendingFileProgressBar;
    private TextView sendingFileStatus;
    private Button startReceivingFileButton;


    public SendingFilesFragment() {}

    public static SendingFilesFragment newInstance(int position) {
        SendingFilesFragment fragment = new SendingFilesFragment();
        Bundle args = new Bundle();
        args.putInt(SOCKET_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connection = connections.get(getArguments().getInt(SOCKET_POSITION));
        sendFileListTask = new SendFileListTask();
        sendFileListTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sending_files, container, false);
        fileListAdapter = new FileListAdapter(getContext(), fileListLocal);

        fileListView = (ListView) view.findViewById(R.id.sending_file_list);
        fileListView.setAdapter(fileListAdapter);

        sendingFileProgressBar = (ProgressBar) view.findViewById(R.id.sending_file_progress_bar);
        sendingFileStatus = (TextView) view.findViewById(R.id.sending_file_status);

        startReceivingFileButton = (Button) view.findViewById(R.id.start_sending_file_button);
        startReceivingFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        return view;
    }

    private class SendFileListTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            System.out.println("send file list task started!");
            Writer out = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                for (FileInfo fileInfo : fileList) {
                    fileListLocal.add(new FileInfo(fileInfo));
                    out.write(fileInfo.getUri() + " " + fileInfo.getTitle() + " " + fileInfo.getSize() + "\r\n");
                }
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            sendingFileProgressBar.setVisibility(View.GONE);
            sendingFileStatus.setVisibility(View.GONE);
            fileListView.setVisibility(View.VISIBLE);
            startReceivingFileButton.setVisibility(View.VISIBLE);
            fileListAdapter.notifyDataSetChanged();

            sendFilesTask = new SendFilesTask();
            sendFilesTask.execute();
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

                for(String line = in.readLine(); !line.isEmpty(); line = in.readLine()) {
                    System.out.println("send files task :" + line);
                    sendFile(fileListLocal.get(Integer.parseInt(line)), out);
                }

                System.out.println("send files task : done");
                out.close();
                in.close();
            }catch(IOException e){
                e.printStackTrace();
            }finally{
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                }
            }
            return null;
        }

        private void sendFile(FileInfo fileInfo, BufferedOutputStream out) {
            BufferedInputStream fin = null;
//            try {
//                fin = new BufferedInputStream(new FileInputStream(fileInfo.getUri()));
//                int count;
//                while ((count = fin.read(buf)) > 0) {
//                    out.write(buf, 0, count);
//                    fileInfo.addSoFar(count);
//                    publishProgress();
//                }
//                fin.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            fileListAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
