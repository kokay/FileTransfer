package com.kokayapp.filetransfer.ReceiveFiles;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.FileListAdapter;
import com.kokayapp.filetransfer.R;
import com.kokayapp.filetransfer.SendReceiveSelectionActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
    private final String downloadDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS) + "/FileTransfer/";
    private final String doneButtonText = "Done";
    private final String startReceiveButtonText = "Start";

    private List<FileInfo> fileList = new ArrayList<>();
    private FileListAdapter fileListAdapter;

    private Socket connection;

    private ListView fileListView;
    private ProgressBar processingFileProgressBar;
    private TextView processingFileStatus;
    private Button processingFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.processing_file_list);

        fileListAdapter = new FileListAdapter(this, fileList);
        fileListView = (ListView) findViewById(R.id.processing_file_list);
        fileListView.setAdapter(fileListAdapter);
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileInfo fileInfo = fileList.get(position);
                fileInfo.setChecked(!fileInfo.isChecked());
            }
        });

        processingFileProgressBar = (ProgressBar) findViewById(R.id.processing_file_progress_bar);
        processingFileStatus = (TextView) findViewById(R.id.processing_file_status);

        processingFileButton = (Button) findViewById(R.id.processing_file_button);
        processingFileButton.setText(startReceiveButtonText);
        processingFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processingFileButton.setVisibility(View.GONE);
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

                for(String line = in.readLine(); !line.isEmpty(); line = in.readLine())
                    fileList.add(FileInfo.parse(downloadDir, line));

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
            processingFileButton.setVisibility(View.VISIBLE);
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
                    if (fileInfo.isChecked()) {
                        String s = Integer.toString(i) + "\r\n";
                        out.write(s);
                        out.flush();
                        receiveFile(fileInfo, in);
                    }
                }
                out.write("\r\n");
                out.flush();

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
            FileOutputStream fin = null;
            try {
                if(!fileInfo.getParentFile().exists()) fileInfo.getParentFile().mkdirs();
                fin = new FileOutputStream(fileInfo);
                int count;
                while((fileInfo.getSoFar() != fileInfo.getSize()) && (count = in.read(buf)) > 0) {
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

            processingFileButton.setText(doneButtonText);
            processingFileButton.setVisibility(View.VISIBLE);
            processingFileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), SendReceiveSelectionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        }
    }
}
