package com.kokayapp.filetransfer;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kokayapp.filetransfer.ReceiveFiles.FileReceivingActivity;
import com.kokayapp.filetransfer.SendFiles.FileSelection.FileSelectionActivity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class SendReceiveSelectionActivity extends AppCompatActivity {
    private final int PERMISSIONS_REQUEST_FOR_SEND = 0;
    private final int PERMISSIONS_REQUEST_FOR_RECEIVE = 1;

    private  int permissionCheck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_receive_selection);


        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionCheck == PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), FileSelectionActivity.class));
                } else {
                    showPermissionRequest();
                }
            }
        });

        findViewById(R.id.receive_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionCheck == PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), FileReceivingActivity.class));
                } else {
                    showPermissionRequest();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FOR_SEND : {
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), FileSelectionActivity.class));
                }
                return;
            }
            case PERMISSIONS_REQUEST_FOR_RECEIVE : {
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), FileReceivingActivity.class));
                }
                return;
            }
        }
    }

    private void showPermissionRequest() {
        Toast.makeText(this, "Permission needed to access files on your device", Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_FOR_RECEIVE);
    }
}
