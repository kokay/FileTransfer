package com.kokayapp.filetransfer;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public abstract class FindingDevicesActivity extends AppCompatActivity
        implements PeerListListener, GroupInfoListener, ConnectionInfoListener {

    protected WifiP2pDevice thisDevice;
    protected List<WifiP2pDevice> deviceList = new ArrayList<>();
    protected ArrayAdapter deviceListAdapter;

    protected final IntentFilter intentFilter = new IntentFilter();
    protected WifiP2pManager manager;
    protected Channel channel;
    protected BroadcastReceiver receiver;
    protected boolean isWiFiP2pEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(this.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void setWiFiP2pEnabled(boolean isWiFiP2pEnabled) {
        this.isWiFiP2pEnabled = isWiFiP2pEnabled;
    }

    public abstract void resetData();

    public void updateThisDevice(WifiP2pDevice device) {
        thisDevice = device;
        TextView view = (TextView) findViewById(R.id.my_name);
        view.setText(device.deviceName + " " + device.deviceAddress);
        view = (TextView) findViewById(R.id.my_status);
        view.setText(getDeviceStatus(device.status));
    }

    public static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:   return "Available";
            case WifiP2pDevice.INVITED:     return "Invited";
            case WifiP2pDevice.CONNECTED:   return "Connected";
            case WifiP2pDevice.FAILED:      return "Failed";
            case WifiP2pDevice.UNAVAILABLE: return "Unavailable";
            default:                        return "Unknown";
        }
    }

    public void test(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
