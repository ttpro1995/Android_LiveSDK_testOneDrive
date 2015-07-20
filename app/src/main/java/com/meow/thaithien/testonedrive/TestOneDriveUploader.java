package com.meow.thaithien.testonedrive;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.SyncStateContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class TestOneDriveUploader extends ActionBarActivity {

    Button login;
    Button upload;
    OneDriveUploader oneDriveUploader = null;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_one_drive_uploader);
        oneDriveUploader = new OneDriveUploader(MyConstants.Live_ID,TestOneDriveUploader.this);

        login = (Button) findViewById(R.id.login_bt);
        upload = (Button) findViewById(R.id.upload_bt);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oneDriveUploader.SignIn();
            }
        });

        handler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateFile createFile = new CreateFile("TitleMeow_testUpload","Test One Drive Uploader",TestOneDriveUploader.this);
                File file = createFile.getFile();
                InputStream is = null;
                try {
                    is = new FileInputStream(file);
                }catch (Exception e){e.printStackTrace();}

                oneDriveUploader.UploadOneDrive("TestOneDriveParent","TestOneDriveSub",file.getName(),is,handler);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_one_drive_uploader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
