package com.meow.thaithien.testonedrive;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveStatus;
import com.microsoft.live.LiveUploadOperationListener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    private LiveAuthClient auth;
    private LiveConnectClient client;

    Button signin;
    Button upload;


    String ONEDRIVE_LOG_TAG= "Live SDK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signin = (Button) findViewById(R.id.Login);
        upload = (Button) findViewById(R.id.upload);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Upload2();
            }
        });

    }




    private void SignIn(){
        auth = new LiveAuthClient(MainActivity.this,MyConstants.Live_ID);
        Iterable<String> scopes = Arrays.asList("wl.signin", "wl.basic","wl.skydrive");
        auth.login(MainActivity.this, scopes, new LiveAuthListener() {
            @Override
            public void onAuthComplete(LiveStatus liveStatus, LiveConnectSession liveConnectSession, Object o) {
                if (liveStatus == LiveStatus.CONNECTED) {
                    Log.i(ONEDRIVE_LOG_TAG, "complete");
                    client = new LiveConnectClient(liveConnectSession);
                }
            }

            @Override
            public void onAuthError(LiveAuthException e, Object o) {
                e.printStackTrace();
            }
        });
    }

    private void Upload2(){
        createFile();
    }

   /* private void Upload(){
        auth.login(MainActivity.this, Arrays.asList(new String[]{"wl.skydrive_update"}), new LiveAuthListener() {
            @Override
            public void onAuthComplete(LiveStatus liveStatus, LiveConnectSession liveConnectSession, Object o) {
                if (liveStatus == LiveStatus.CONNECTED) {
                     }
            }

            @Override
            public void onAuthError(LiveAuthException e, Object o) {
                e.printStackTrace();
            }
        });

    }*/

//    private class UploadOneDrive extends AsyncTask<Void,Void,Void>{
//
//
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            CreateFile createFile = new CreateFile("mewo.txt","keep calm and meow on",MainActivity.this);
//            File file = createFile.getFile();
//
//
//            try {
//                InputStream is = new FileInputStream(file);
//               client.uploadAsync("TestOneDrive", file.getName(), is, new LiveUploadOperationListener() {
//                   @Override
//                   public void onUploadCompleted(LiveOperation liveOperation) {
//                       Log.i(ONEDRIVE_LOG_TAG,"onUploadComplete");
//                   }
//
//                   @Override
//                   public void onUploadFailed(LiveOperationException e, LiveOperation liveOperation) {
//                        e.printStackTrace();
//                   }
//
//                   @Override
//                   public void onUploadProgress(int i, int i1, LiveOperation liveOperation) {
//
//                   }
//               });
//            }
//
//            catch (Exception e){e.printStackTrace();}
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            Log.i(ONEDRIVE_LOG_TAG,"upload done");
//        }
//    }

    public void createFile() {
        final Runnable uploadImage = new Runnable() {
            public void run() {
                final String imageUrl = "http://cdn.akamai.steamstatic.com/steam/apps/322540/ss_cd9e9091b675ea3b060add79ecb33fe06ce1f048.1920x1080.jpg";
                final String fileName = "meow3.jpg";
                final URL url;
                final URLConnection ucon;
                final InputStream is;
                try {
                    url = new URL(imageUrl);
                    ucon = url.openConnection();
                    is = ucon.getInputStream();
                    client.uploadAsync("me/skydrive/", fileName, is, new LiveUploadOperationListener() {
                        public void onUploadFailed(LiveOperationException exception, LiveOperation operation) {
                            exception.printStackTrace();
                        }

                        @Override
                        public void onUploadProgress(int i, int i1, LiveOperation liveOperation) {

                        }

                        public void onUploadCompleted(LiveOperation operation) {

                            Log.i(ONEDRIVE_LOG_TAG,"onUploadComplete");
                            try {
                                is.close();
                            }
                            catch(IOException ioe) {

                            }
                        }

                    });
                }
                catch(IOException ioex) {
                    ioex.printStackTrace();
                    return;
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    return;
                }
            }
        };
        auth.login(this, Arrays.asList(new String[]{"wl.skydrive_update"}), new LiveAuthListener() {
            public void onAuthError(LiveAuthException exception, Object userState) {
                exception.printStackTrace();
            }

            public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
                new Thread(uploadImage).start();
            }
        });

    }

}
