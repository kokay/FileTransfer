package com.kokayapp.filetransfer.ReceiveFiles;

import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.kokayapp.filetransfer.FindingDevicesActivity;
import com.kokayapp.filetransfer.R;

public class ServerSelectionActivity extends FindingDevicesActivity {

    public static WifiP2pDevice selectedServer;
    public static WifiP2pInfo wifiP2pInfo;

    private ServerCandidatesListAdapter serverCandidatesListAdapter;
    private Button receiveFilesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_selection);
        serverCandidatesListAdapter = new ServerCandidatesListAdapter(this, deviceList);
        deviceListAdapter = serverCandidatesListAdapter;

        receiveFilesButton = (Button) findViewById(R.id.connect_button);
        receiveFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = selectedServer.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                manager.connect(channel, config, null);
                test(selectedServer.deviceAddress + " " + selectedServer.deviceName);
            }
        });

        ListView serverCandidatesListView = (ListView) findViewById(R.id.server_candidates_list);
        serverCandidatesListView.setAdapter(serverCandidatesListAdapter);
        serverCandidatesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedServer = deviceList.get(position);
                serverCandidatesListAdapter.notifyDataSetChanged();
                if (!selectedServer.deviceName.isEmpty())
                    receiveFilesButton.setText("Connect to " + selectedServer.deviceName);
                else
                    receiveFilesButton.setText("Connect to " + selectedServer.deviceAddress);
                receiveFilesButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.discoverPeers(channel, null);
        test("resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.stopPeerDiscovery(channel, null);
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
        selectedServer = null;
        deviceList.clear();
        deviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        deviceList.clear();
        deviceList.addAll(peers.getDeviceList());
        if(!deviceList.contains(selectedServer)) {
            selectedServer = null;
            receiveFilesButton.setVisibility(View.GONE);
        }
        deviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        this.wifiP2pInfo = info;
        Intent intent = new Intent(getApplicationContext(), ReceivingFilesActivity.class);
        startActivity(intent);
    }
}
