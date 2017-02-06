package com.kokayapp.filetransfer.SendFiles.FileSending;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kokayapp.filetransfer.R;
import com.kokayapp.filetransfer.ReceiveFiles.FileReceivingActivity;
import com.kokayapp.filetransfer.SendFiles.FileSelection.FileSelectionActivity;
import com.kokayapp.filetransfer.SendReceiveSelectionActivity;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

public class AcceptorFragment extends Fragment {
    public static final int PORT_NUMBER = 55555;
    public static final int TIME_OUT = 50000; // 50 seconds
    public static final String REQUEST = "Request File List";
    private ServerSocket server;

    private FileSendingActivity fileSendingActivity;
    private Acceptor acceptor = new Acceptor();

    private String thisDeviceName = new String();
    private String thisDeviceMacAddress = new String();
    private String thisDeviceSSID = new String();
    private String thisDevicePassword = new String();

    private TextView thisDeviceNameTextView;
    private TextView thisDeviceMacAddressTextView;
    private TextView thisDeviceSSIDTextView;
    private TextView thisDevicePasswordTextView;

    private ProgressBar progressBar;
    private TextView status;

    private Button finishButton;

    public AcceptorFragment() {
    }

    public static AcceptorFragment newInstance() {
        return new AcceptorFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_acceptor, container, false);

        thisDeviceNameTextView = (TextView) view.findViewById(R.id.this_device_name);
        thisDeviceNameTextView.setText(thisDeviceName);

        thisDeviceMacAddressTextView = (TextView) view.findViewById(R.id.this_device_mac_address);
        thisDeviceMacAddressTextView.setText(thisDeviceMacAddress);

        thisDeviceSSIDTextView = (TextView) view.findViewById(R.id.this_device_ssid);
        thisDeviceSSIDTextView.setText(thisDeviceSSID);

        thisDevicePasswordTextView = (TextView) view.findViewById(R.id.this_device_password);
        thisDevicePasswordTextView.setText(thisDevicePassword);

        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        status = (TextView) view.findViewById(R.id.status);

        finishButton = (Button) view.findViewById(R.id.finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                    Intent intent = new Intent(getActivity(), SendReceiveSelectionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {

                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fileSendingActivity = (FileSendingActivity) context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!server.isClosed()) {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        acceptor.start();
    }

    public void updateThisDevice(WifiP2pDevice device) {
        thisDeviceName = device.deviceName;
        thisDeviceMacAddress = device.deviceAddress;

        if(thisDeviceNameTextView != null)
            thisDeviceNameTextView.setText(thisDeviceName);
        if(thisDeviceMacAddressTextView != null)
            thisDeviceMacAddressTextView.setText(thisDeviceMacAddress);
    }

    public void updateGroupInfo(WifiP2pGroup group) {
        thisDeviceSSID = group.getNetworkName();
        thisDevicePassword = group.getPassphrase();

        if(thisDeviceSSIDTextView != null)
            thisDeviceSSIDTextView.setText(thisDeviceSSID);
        if(thisDevicePasswordTextView != null)
            thisDevicePasswordTextView.setText(thisDevicePassword);
    }

    private class Acceptor extends Thread {
        @Override
        public void run() {
            try {
                server = new ServerSocket(PORT_NUMBER);
                while(true) {
                    Socket connection = server.accept();
                    (new CheckConnectionTask())
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, connection);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        public class CheckConnectionTask extends AsyncTask<Socket, Void, Boolean> {

            @Override
            protected Boolean doInBackground(Socket... params) {
                Socket connection = params[0];
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    if (!in.readLine().equals(REQUEST))
                        return false;

                    InetAddress remoteIpAddress =
                            ((InetSocketAddress) connection.getRemoteSocketAddress()).getAddress();
                    fileSendingActivity.addConnection(remoteIpAddress, connection);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if(success) {
                    fileSendingActivity.getAdapter().notifyDataSetChanged();
                    fileSendingActivity.changeCurrentTab();
                }
            }
        }
    }
}
