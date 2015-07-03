package com.meow.thaithien.testonedrive;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;
import com.microsoft.live.LiveStatus;
import com.microsoft.live.LiveUploadOperationListener;

import org.json.JSONException;
import org.json.JSONObject;

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
/*To access certain OneDrive folders, you can use friendly names instead of folder IDs. Use the following friendly names to access these corresponding folders in the OneDriveUI:
USER_ID /skydrive/camera_roll represents the OneDrive camera roll folder.
USER_ID /skydrive/my_documents represents the Documents folder.
USER_ID /skydrive/my_photos represents the Pictures folder.
USER_ID /skydrive/public_documents represents the Public folder.
*/
    private LiveAuthClient auth;
    private LiveConnectClient client;

    TextView resultTextView;

    Button signin;
    Button upload;
    Button meow;
    Button createFolder;

    String ONEDRIVE_LOG_TAG= "Live SDK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signin = (Button) findViewById(R.id.Login);
        upload = (Button) findViewById(R.id.upload);
        createFolder = (Button) findViewById(R.id.CreateFOlder);
        resultTextView = (TextView) findViewById(R.id.result);
        meow = (Button) findViewById(R.id.meow);
        meow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetAllItem();//TODO test it
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        });
        createFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFolder();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    CreateFile createFile = new CreateFile("meow.txt", "Keep calm and meow on", MainActivity.this);
                    File f = createFile.getFile();
                    UploadFileOneDrive(f.getName(), new FileInputStream(f));
                }
                catch (Exception e){ e.printStackTrace();}
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


    /*Upload file with name and inputstream
    * */
    public void UploadFileOneDrive(final String file_name,final InputStream is){
        auth.login(this, Arrays.asList(new String[]{"wl.skydrive_update"}), new LiveAuthListener() {
            public void onAuthError(LiveAuthException exception, Object userState) {
                exception.printStackTrace();
            }

            public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
                client.uploadAsync("me/skydrive/", file_name, is, new LiveUploadOperationListener() {
                    public void onUploadFailed(LiveOperationException exception, LiveOperation operation) {
                        exception.printStackTrace();
                    }

                    @Override
                    public void onUploadProgress(int i, int i1, LiveOperation liveOperation) {

                    }

                    public void onUploadCompleted(LiveOperation operation) {

                        Log.i(ONEDRIVE_LOG_TAG, "onUploadComplete");
                        try {
                            is.close();
                        } catch (IOException ioe) {

                        }
                    }

                });
            }
        });

    }

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

    public void readFolder() {
        client.getAsync("folder.meowmeow", new LiveOperationListener() {
            public void onComplete(LiveOperation operation) {
                JSONObject result = operation.getResult();
                resultTextView.setText("Folder ID = " + result.optString("id") +
                        ", name = " + result.optString("name"));
            }

            public void onError(LiveOperationException exception, LiveOperation operation) {
                resultTextView.setText("Error reading folder: " + exception.getMessage());
            }
        });
    }

    public void createFolder() {
        final LiveOperationListener opListener = new LiveOperationListener() {
            public void onError(LiveOperationException exception, LiveOperation operation) {
                resultTextView.setText("Error creating folder: " + exception.getMessage());
            }
            public void onComplete(LiveOperation operation) {
                JSONObject result = operation.getResult();
                String text = "Folder created:\n" +
                        "\nID = " + result.optString("id") +
                        "\nName = " + result.optString("name");
                resultTextView.setText(text);
            }
        };
        auth.login(this, Arrays.asList(new String[]{"wl.skydrive_update"}),
                new LiveAuthListener() {
                    public void onAuthError(LiveAuthException exception, Object userState) {
                        resultTextView.setText("Error signing in: " + exception.getMessage());
                    }

                    public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
                        try {
                            JSONObject body = new JSONObject();
                            body.put("name", "MeowFolder");
                            body.put("description", "My brand new folder");
                            client.postAsync("me/skydrive/my_documents", body, opListener);
                        } catch (JSONException ex) {
                            resultTextView.setText("Error building folder: " + ex.getMessage());
                        }
                    }
                }
        );
    }

    private void GetAllItem(){

        client.getAsync("me/skydrive/files", new LiveOperationListener() {
            @Override
            public void onError(LiveOperationException e, LiveOperation liveOperation) {
                e.printStackTrace();
            }

            @Override
            public void onComplete(final LiveOperation operation) {
                final JSONObject result = operation.getResult();
                if (result.has("error")) {
                    final JSONObject error = result.optJSONObject("error");
                    final String message = error.optString("message");
                    final String code = error.optString("code");

                    return;
                }
            String json_body =    result.toString();
            Log.i("JSON",json_body);
            CreateFile createFile = new CreateFile("root_files.json",json_body,MainActivity.this);
                File file = createFile.getFile();
                try {
                    UploadFileOneDrive(file.getName(),new FileInputStream(file));
                }
                catch (Exception e){e.printStackTrace();};
            }
        });
    }

    

}
