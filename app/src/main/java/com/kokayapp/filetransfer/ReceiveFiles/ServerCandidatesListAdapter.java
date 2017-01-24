package com.kokayapp.filetransfer.ReceiveFiles;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.kokayapp.filetransfer.R;

import java.util.List;

import static com.kokayapp.filetransfer.FindingDevicesActivity.getDeviceStatus;
import static com.kokayapp.filetransfer.ReceiveFiles.ServerSelectionActivity.selectedServer;

/**
 * Created by Koji on 1/5/2017.
 */

public class ServerCandidatesListAdapter extends ArrayAdapter<WifiP2pDevice> {
    private List<WifiP2pDevice> deviceList;

    public ServerCandidatesListAdapter(Context context, List<WifiP2pDevice> deviceList) {
        super(context, 0, deviceList);
        this.deviceList = deviceList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.server, null);

        WifiP2pDevice device = deviceList.get(position);
        if (device != null) {
            TextView serverName = (TextView) convertView.findViewById(R.id.server_name);
            if (serverName != null)   serverName.setText(device.deviceName + " " + device.deviceAddress);

            TextView serverStatus = (TextView) convertView.findViewById(R.id.server_status);
            if (serverStatus != null) serverStatus.setText(getDeviceStatus(device.status));

            RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.server_radio_button);
            if (radioButton != null)  radioButton.setChecked(selectedServer != null && selectedServer.equals(device));
        }
        return convertView;
    }
}
