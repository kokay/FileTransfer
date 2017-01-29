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
import android.widget.TextView;

import com.kokayapp.filetransfer.FileInfo;
import com.kokayapp.filetransfer.DeviceFindingActivity;
import com.kokayapp.filetransfer.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.kokayapp.filetransfer.SendFiles.FileSelection.FileSelectionActivity.SELECTED_FILES;

public class FileSendingActivity extends DeviceFindingActivity {

    private TreeSet<FileInfo> selectedFiles;
    private List<FileSendingFragment> sendingFilesFragments;
    private FragmentPagerAdapter sendingFilesFragmentAdapter;

    private Acceptor acceptor = new Acceptor();
    private Map<String, Socket> connections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sending);

        sendingFilesFragments = new ArrayList<>();
        connections = new TreeMap<>();

        selectedFiles = (TreeSet<FileInfo>) getIntent().getSerializableExtra(SELECTED_FILES);
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
                return sendingFilesFragments.get(position).getNickName();
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
        acceptor.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        acceptor.interrupt();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        manager.removeGroup(channel, null);
        deviceList.clear();
        sendingFilesFragments.clear();
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

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {}

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        if(group == null) return;

        ((TextView) findViewById(R.id.ssid)).setText(group.getNetworkName());
        ((TextView) findViewById(R.id.password)).setText(group.getPassphrase());

        deviceList.clear();
        deviceList.addAll(group.getClientList());
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {}

    public Socket getConnection(String nickName) {
        return connections.get(nickName);
    }

    public void removeConnection(String nickName) {
        connections.remove(nickName);
    }

    private class Acceptor extends Thread {
        private final int PORT = 55555;
        private ServerSocket server;

        @Override
        public void run() {
            try {
                System.out.println("check start acceptor connections size " + connections.size());
                System.out.println("check start acceptor fragments size " + sendingFilesFragments.size());
                server = new ServerSocket(PORT);
                while(true) {
                    Socket connection = server.accept();
                    (new CheckConnectionTask())
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, connection);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        private class CheckConnectionTask extends AsyncTask<Socket, Void, Boolean> {
            private final int TIME_OUT = 50000; // 50 seconds
            @Override
            protected Boolean doInBackground(Socket... params) {
                Socket connection = params[0];
                BufferedReader in = null;
                try {
                    System.out.println("check before connection check connections size " + connections.size());
                    System.out.println("check before connection check fragments size " + sendingFilesFragments.size());
                    connection.setSoTimeout(TIME_OUT);
                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String nickName = in.readLine();
                    System.out.println("check accept:" + nickName + " " + connection.getInetAddress().toString());
                    if (!connections.containsKey(nickName)) {
                        connections.put(nickName, connection);
                        sendingFilesFragments.add(FileSendingFragment.newInstance(nickName));
                    }
                    System.out.println("check after connection check connections size " + connections.size());
                } catch (IOException e) {
                    System.out.println("check false end connection check");
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                sendingFilesFragmentAdapter.notifyDataSetChanged();
                if (success) sendingFilesFragmentAdapter.notifyDataSetChanged();
            }
        }
    }
}
