package com.kokayapp.filetransfer.SendFiles;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kokayapp.filetransfer.R;

import java.util.List;

import static com.kokayapp.filetransfer.FindingDevicesActivity.getDeviceStatus;
import static com.kokayapp.filetransfer.SendFiles.ClientSelectionActivity.selectedDevices;

/**
 * Created by Koji on 1/5/2017.
 */

public class ClientListAdapter extends ArrayAdapter<WifiP2pDevice> {
    private List<WifiP2pDevice> deviceList;

    public ClientListAdapter(Context context, List<WifiP2pDevice> deviceList) {
        super(context, 0, deviceList);
        this.deviceList = deviceList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.client, null);

        WifiP2pDevice device = deviceList.get(position);
        if (device != null) {
            TextView serverName = (TextView) convertView.findViewById(R.id.client_name);
            if (serverName != null) serverName.setText(device.deviceName + " " + device.deviceAddress);

            TextView serverStatus = (TextView) convertView.findViewById(R.id.client_status);
            if (serverStatus != null) serverStatus.setText(getDeviceStatus(device.status));

            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.client_check_box);
            if (checkBox != null) checkBox.setChecked(selectedDevices.contains(deviceList.get(position)));
        }
        return convertView;
    }
}
