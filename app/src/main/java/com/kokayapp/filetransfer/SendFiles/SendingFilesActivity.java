package com.kokayapp.filetransfer.SendFiles;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kokayapp.filetransfer.R;
import com.kokayapp.filetransfer.SendReceiveSelectionActivity;

import java.util.ArrayList;
import java.util.List;

import static com.kokayapp.filetransfer.SendFiles.ClientSelectionActivity.connections;

public class SendingFilesActivity extends AppCompatActivity {

    private List<Fragment> sendingFilesFragments = new ArrayList<>();
    private Button doneButton;
    private int finishedSendingNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_files);

        for (int position = 0; position < connections.size(); ++position )
            sendingFilesFragments.add(SendingFilesFragment.newInstance(position));

        ViewPager viewPager = (ViewPager) findViewById(R.id.device_view_pager);
        viewPager.setAdapter(new FragmentListPagerAdapter(getSupportFragmentManager(), sendingFilesFragments));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.device_tabs);
        tabLayout.setupWithViewPager(viewPager);

        doneButton = (Button) findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SendReceiveSelectionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    public void reportDone() {
        finishedSendingNum++;
        if (finishedSendingNum == sendingFilesFragments.size())
            doneButton.setVisibility(View.VISIBLE);
    }
}
