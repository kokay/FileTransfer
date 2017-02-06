package com.kokayapp.filetransfer.ReceiveFiles;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kokayapp.filetransfer.DeviceFindingActivity;
import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.FileListAdapter;
import com.kokayapp.filetransfer.R;
import com.kokayapp.filetransfer.SendReceiveSelectionActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.kokayapp.filetransfer.SendFiles.FileSending.AcceptorFragment.PORT_NUMBER;
import static com.kokayapp.filetransfer.SendFiles.FileSending.AcceptorFragment.REQUEST;


public class FileReceivingActivity extends DeviceFindingActivity {
    private static final String DIALOG_TAG = "Dialog tag";
    private final String downloadDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS) + "/FileTransfer/";

    private Socket connection;
    private ReceiveFileListTask receiveFileListTask = new ReceiveFileListTask();
    private ReceiveFilesTask receiveFilesTask = new ReceiveFilesTask();
    private WifiP2pDevice connectingDevice = null;

    private List<FileInfo> fileList = new ArrayList<>();
    private FileListAdapter fileListAdapter;

    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private TextView thisDeviceName;
    private TextView thisDeviceMacAddress;

    private ListView deviceListView;
    private ListView fileListView;

    private ProgressBar progressBar;
    private TextView status;

    private LinearLayout buttons;
    private Button cancelButton;
    private Button receiveButton;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_receiving);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.select_device));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        thisDeviceName = (TextView) findViewById(R.id.this_device_name);
        thisDeviceMacAddress = (TextView) findViewById(R.id.this_device_mac_address);

        deviceListView = (ListView) findViewById(R.id.device_list);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogFragment dialog = new ConnectDeviceDialogFragment(deviceList.get(position));
                dialog.show(getSupportFragmentManager(), DIALOG_TAG);
            }
        });

        fileListAdapter = new FileListAdapter(this, fileList);
        fileListView = (ListView) findViewById(R.id.file_list);
        fileListView.setAdapter(fileListAdapter);
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileInfo fileInfo = fileList.get(position);
                fileInfo.setChecked(!fileInfo.isChecked());
                fileListAdapter.notifyDataSetChanged();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        status = (TextView) findViewById(R.id.status);


        buttons = (LinearLayout) findViewById(R.id.buttons);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        receiveButton = (Button) findViewById(R.id.receive_button);
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiveButton.setVisibility(View.GONE);
                receiveFilesTask.execute();
            }
        });

        closeButton = (Button) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeApplication();
            }
        });
    }

    private void closeApplication() {
        this.finishAffinity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.discoverPeers(channel, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.stopPeerDiscovery(channel, null);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        manager.removeGroup(channel, null);
    }

    @Override
    public void updateThisDevice(WifiP2pDevice device) {
        thisDeviceName.setText(device.deviceName);
        thisDeviceMacAddress.setText(device.deviceAddress);
    }

    private void connect(WifiP2pDevice device) {
        deviceListView.setVisibility(View.GONE);
        showProgressBar(getResources().getString(R.string.connecting));
        connectingDevice = device;
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        manager.connect(channel, config, null);
    }

    private void showCancelButton() {
        buttons.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        receiveButton.setVisibility(View.GONE);
        closeButton.setVisibility(View.GONE);
    }

    // When Receive button is shown, Cancel button should be shown too.
    private void showReceiveButton() {
        buttons.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        receiveButton.setVisibility(View.VISIBLE);
        closeButton.setVisibility(View.GONE);
    }

    private void showGoHomeButton() {
        buttons.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.GONE);
        receiveButton.setVisibility(View.GONE);
        closeButton.setVisibility(View.VISIBLE);
    }

    private void hideButtons() {
        buttons.setVisibility(View.GONE);
    }

    private void showProgressBar(String message) {
        status.setText(message);
        status.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        status.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        deviceList.clear();
        deviceList.addAll(peers.getDeviceList());
        Boolean rejected = connectingDevice != null && !deviceList.contains(connectingDevice);
        if (connectingDevice == null || rejected) {
            if (deviceList.size() != 0) {
                deviceListView.setVisibility(View.VISIBLE);
                hideProgressBar();
            } else {
                manager.discoverPeers(channel, null);
                deviceListView.setVisibility(View.GONE);
                showProgressBar(getResources().getString(R.string.finding_the_device));
            }
            deviceListAdapter.notifyDataSetChanged();
        }
        if (rejected && coordinatorLayout != null) {
            hideButtons();
            Snackbar.make(coordinatorLayout,
                    getResources().getString(R.string.connection_fail), Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        receiveFileListTask.execute(info.groupOwnerAddress);
    }

    private static class ConnectDeviceDialogFragment extends DialogFragment {
        private WifiP2pDevice device;

        public ConnectDeviceDialogFragment(WifiP2pDevice device) {
            this.device = device;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_connect_device, null);
            builder.setView(view);
            builder.setTitle(R.string.dialog_device_connect_title);

            TextView deviceNameTv = (TextView) view.findViewById(R.id.device_name);
            deviceNameTv.setText(device.deviceName);

            TextView deviceMacAddressTv = (TextView) view.findViewById(R.id.device_mac_address);
            deviceMacAddressTv.setText(device.deviceAddress);

            builder.setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FileReceivingActivity fileReceivingActivity = (FileReceivingActivity) getActivity();
                    fileReceivingActivity.showCancelButton();
                    fileReceivingActivity.connect(device);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            return builder.create();
        }
    }

    private class ReceiveFileListTask extends AsyncTask<InetAddress, Void, Boolean> {

        @Override
        protected Boolean doInBackground(InetAddress... params) {
            try {
                connection = new Socket();
                connection.connect(new InetSocketAddress(params[0], PORT_NUMBER));

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                Writer out = new OutputStreamWriter(connection.getOutputStream());

                out.write(REQUEST + "\r\n");
                out.flush();
                for(String line = in.readLine(); !line.isEmpty(); line = in.readLine()) {
                    fileList.add(FileInfo.parse(downloadDir, line));
                }

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                hideProgressBar();
                showReceiveButton();
                fileListView.setVisibility(View.VISIBLE);
                fileListAdapter.notifyDataSetChanged();
            } else {
                showProgressBar("Error Occurs.");
                showGoHomeButton();
            }
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

            showGoHomeButton();
        }
    }
}
