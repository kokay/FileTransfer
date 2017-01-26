package com.kokayapp.filetransfer.SendFiles;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.kokayapp.filetransfer.FindingDevicesActivity;
import com.kokayapp.filetransfer.R;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientSelectionActivity extends FindingDevicesActivity {

    public static List<WifiP2pDevice> selectedDevices = new ArrayList<>();
    public static List<Socket> connections = new ArrayList<>();
    public ServerSocket server;

    private ClientListAdapter clientListAdapter;

    private AcceptingConnectionTask acceptingConnectionTask;
    private boolean running = true;

    private Button sendFilesButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_client_selection);
        clientListAdapter = new ClientListAdapter(this, deviceList);
        deviceListAdapter = clientListAdapter;

        sendFilesButton = (Button) findViewById(R.id.send_files_button);
        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SendingFilesActivity.class);
                startActivity(intent);
            }
        });

        ListView clientCandidatesList = (ListView) findViewById(R.id.client_candidates_list);
        clientCandidatesList.setAdapter(clientListAdapter);
        clientCandidatesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pDevice device = deviceList.get(position);
                if (selectedDevices.contains(device)) selectedDevices.remove(device);
                else                                 selectedDevices.add(device);

                clientListAdapter.notifyDataSetChanged();
                if (selectedDevices.size() == 0) sendFilesButton.setVisibility(View.GONE);
                else                             sendFilesButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.createGroup(channel, null);
        running = true;
        selectedDevices.clear();
        acceptingConnectionTask = new AcceptingConnectionTask();
        acceptingConnectionTask.start();
        test("resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        acceptingConnectionTask.interrupt();
        running = false;
        test("pause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        manager.removeGroup(channel, null);
        resetData();
        test("destroy");
    }

    @Override
    public void resetData() {
        selectedDevices.clear();
        deviceList.clear();
        clientListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {}

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        if(group == null) return;

        ((TextView) findViewById(R.id.ssid)).setText(group.getNetworkName());
        ((TextView) findViewById(R.id.password)).setText(group.getPassphrase());

        resetData();
        deviceList.addAll(group.getClientList());
        clientListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {}

    private class AcceptingConnectionTask extends Thread {

        @Override
        public void run() {
            try {
                server = new ServerSocket(55555);
                while(running) {
                    Socket connection = server.accept();
                    connections.add(connection);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
