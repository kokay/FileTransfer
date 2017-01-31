package com.kokayapp.filetransfer.SendFiles.FileSending;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.DeviceFindingActivity;
import com.kokayapp.filetransfer.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.kokayapp.filetransfer.R.id.toolbar;
import static com.kokayapp.filetransfer.SendFiles.FileSelection.FileSelectionActivity.SELECTED_FILES;

public class FileSendingActivity extends DeviceFindingActivity {

    private AcceptorFragment acceptorFragment;
    private TreeSet<FileInfo> selectedFiles;
    private List<Fragment> sendingFilesFragments;
    private FragmentPagerAdapter sendingFilesFragmentAdapter;

    private HashMap<InetAddress, Socket> connections;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sending);

        sendingFilesFragments = new ArrayList<>();
        acceptorFragment = AcceptorFragment.newInstance();
        acceptorFragment.start();
        sendingFilesFragments.add(acceptorFragment);

        connections = new HashMap<>();

        selectedFiles = (TreeSet<FileInfo>) getIntent().getSerializableExtra(SELECTED_FILES);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.sending_files));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        sendingFilesFragmentAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return sendingFilesFragments.get(position);
            }

            @Override
            public int getCount() {
                return sendingFilesFragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0) return "Waiting for the connections...";
                return ((FileSendingFragment)sendingFilesFragments.get(position)).getNickName();
            }
        };


        ViewPager viewPager = (ViewPager) findViewById(R.id.device_view_pager);
        viewPager.setAdapter(sendingFilesFragmentAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    public void onResume() {
        super.onResume();
        manager.createGroup(channel, null);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        manager.removeGroup(channel, null);
    }

    public TreeSet<FileInfo> getSelectedFiles() {
        return selectedFiles;
    }

    @Override
    public void resetData() {
        deviceList.clear();
    }

    @Override
    public void updateThisDevice(WifiP2pDevice device) {
        acceptorFragment.updateThisDevice(device);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {}

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        if(group == null) return;

        acceptorFragment.updateGroupInfo(group);
        deviceList.clear();
        deviceList.addAll(group.getClientList());
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {}

    public void addConnection(InetAddress remoteIpAddress, Socket connection) {
        connections.put(remoteIpAddress, connection);
        sendingFilesFragments.add(FileSendingFragment.newInstance(remoteIpAddress));
    }

    public Socket getConnection(InetAddress remoteIpAddress) {
        return connections.get(remoteIpAddress);
    }

    public void removeConnection(InetAddress remoteIpAddress) {
        connections.remove(remoteIpAddress);
    }

    public FragmentPagerAdapter getAdapter() {
        return sendingFilesFragmentAdapter;
    }
}
