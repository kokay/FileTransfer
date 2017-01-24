package com.kokayapp.filetransfer.ReceiveFiles;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.kokayapp.filetransfer.ReceiveFiles.ServerSelectionActivity.wifiP2pInfo;

public class ReceivingFilesActivity extends AppCompatActivity {

    private List<FileInfo> fileList = new ArrayList<>();
    private FileListAdapter fileListAdapter;

    private Socket connection;
    private String ipAddress;

    private ListView fileListView;
    private ProgressBar processingFileProgressBar;
    private TextView processingFileStatus;
    private Button startReceivingFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.processing_file_list);

        fileListAdapter = new FileListAdapter(this, fileList);
        fileListView = (ListView) findViewById(R.id.processing_file_list);
        fileListView.setAdapter(fileListAdapter);

        processingFileProgressBar = (ProgressBar) findViewById(R.id.processing_file_progress_bar);
        processingFileStatus = (TextView) findViewById(R.id.processing_file_status);

        startReceivingFileButton = (Button) findViewById(R.id.start_processing_file_button);
        startReceivingFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new ReceiveFilesTask()).execute();
            }
        });

        (new ReceiveFileListTask()).execute();
    }

    private class ReceiveFileListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                connection = new Socket();
                connection.connect(new InetSocketAddress(wifiP2pInfo.groupOwnerAddress, 55555));

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                for(String line = in.readLine(); !line.isEmpty(); line = in.readLine()) {
                    System.out.println("receive files task :" + line);
                    fileList.add(FileInfo.parse(line));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            processingFileProgressBar.setVisibility(View.GONE);
            processingFileStatus.setVisibility(View.GONE);
            fileListView.setVisibility(View.VISIBLE);
            startReceivingFileButton.setVisibility(View.VISIBLE);
            fileListAdapter.notifyDataSetChanged();
        }
    }

    private class ReceiveFilesTask extends AsyncTask<Void, Void, Void> {
        private byte[] buf = new byte[1024 * 3];

        @Override
        protected Void doInBackground(Void... params) {
            Writer out = null;
            BufferedInputStream in = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                in = new BufferedInputStream(connection.getInputStream());

                for (int i = 0; i < fileList.size(); ++i ) {
                    FileInfo fileInfo = fileList.get(i);
                    if (!fileInfo.isChecked()) {
                        out.write(Integer.toString(i));
                        out.flush();
                        receiveFile(fileInfo, in);
                    }
                }

                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {}
            }

            return null;
        }

        private void receiveFile(FileInfo fileInfo, BufferedInputStream in) {
            BufferedOutputStream fin = null;
            try {
                fin = new BufferedOutputStream(new FileOutputStream(fileInfo));

                int count;
                while((fileInfo.getSoFar() != fileInfo.length()) && (count = in.read(buf)) > 0) {
                    fin.write(buf, 0, count);
                    fileInfo.addSoFar(count);
                    publishProgress();
                }
                fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
