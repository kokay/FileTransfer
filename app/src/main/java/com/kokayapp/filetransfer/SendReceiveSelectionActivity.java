package com.kokayapp.filetransfer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.kokayapp.filetransfer.ReceiveFiles.ServerSelectionActivity;
import com.kokayapp.filetransfer.SendFiles.ClientSelectionActivity;

public class SendReceiveSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_receive_selection);

        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClientSelectionActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.receive_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ServerSelectionActivity.class);
                startActivity(intent);
            }
        });
    }
}
