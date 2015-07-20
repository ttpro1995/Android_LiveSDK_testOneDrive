package com.meow.thaithien.testonedrive;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Thien on 7/7/2015.
 */
public class OneDriveUploader {
    private LiveAuthClient auth=null;
    private LiveConnectClient client=null;
    private  String ONEDRIVE_LOG_TAG = "Live SDK";
    private  String LiveSDK_ID=null;
    private Activity activity = null;
    private Context context = null;

    //
    private String _ParentFolderName = null;
    private  String _SubFolderName = null;
    private String _FileName = null;
    private InputStream _is = null;

    private boolean LoginError = false;

    //Handler
    private Handler handler = null;

    //flag
    boolean sign_in_ok = false;

    //LiveSDK_ID https://account.live.com/developers/applications/index
    //look like this
    //   0000000040159142
    public OneDriveUploader(String liveSDK_ID, Activity activity) {
        LiveSDK_ID = liveSDK_ID;
        this.activity = activity;
        this.context = this.activity.getBaseContext();
    }

    //TODO:1 call SignIn()
    //call it before upload
    public void SignIn() {
        LoginError = false;
        auth = new LiveAuthClient(context, LiveSDK_ID);
        Iterable<String> scopes = Arrays.asList("wl.signin", "wl.basic", "wl.skydrive", "wl.skydrive_update");
        auth.login(activity, scopes, new LiveAuthListener() {
            @Override
            public void onAuthComplete(LiveStatus liveStatus, LiveConnectSession liveConnectSession, Object o) {
                if (liveStatus == LiveStatus.CONNECTED) {
                    Log.i(ONEDRIVE_LOG_TAG, "complete");
                    client = new LiveConnectClient(liveConnectSession);
                    //resultTextView.setText("loged");
                    Log.i(ONEDRIVE_LOG_TAG,"Login");
                    sign_in_ok = true;//flag login
                }
            }

            @Override
            public void onAuthError(LiveAuthException e, Object o) {
                e.printStackTrace();
                LoginError = true;
            }
        });
    }

    //TODO:2 check if it is signed in
    //if it return true, it's ready to upload
    public boolean isSignin(){
        return  sign_in_ok;
    }


    //TODO:3 upload
    //upload to parentfolder/subfolder/filename
    public void UploadOneDrive(String ParentFolder, String SubFolder, String FileName, InputStream is, Handler mhandler){

        this.handler = mhandler;
        this._ParentFolderName = ParentFolder;
        this._SubFolderName = SubFolder;
        this._FileName = FileName;
        this._is = is;
        new UploadOneDriveAsynTask().execute();

    }


    public   void LoginAndUpload( final String  ParentFolder,final String SubFolder,final String FileName,final InputStream is,final Handler mhandler){
        LoginError = false;
        if (sign_in_ok == false) {
            auth = new LiveAuthClient(context, LiveSDK_ID);
            Iterable<String> scopes = Arrays.asList("wl.signin", "wl.basic", "wl.skydrive", "wl.skydrive_update");
            auth.login(activity, scopes, new LiveAuthListener() {
                @Override
                public void onAuthComplete(LiveStatus liveStatus, LiveConnectSession liveConnectSession, Object o) {
                    if (liveStatus == LiveStatus.CONNECTED) {
                        Log.i(ONEDRIVE_LOG_TAG, "complete");
                        client = new LiveConnectClient(liveConnectSession);
                        //resultTextView.setText("loged");
                        Log.i(ONEDRIVE_LOG_TAG, "Login");
                        sign_in_ok = true;//flag login

                        //upload
                        UploadOneDrive(ParentFolder, SubFolder, FileName, is, mhandler);
                        //
                    }
                }

                @Override
                public void onAuthError(LiveAuthException e, Object o) {
                    e.printStackTrace();
                    LoginError = true;
                }
            });
        }
        else UploadOneDrive(ParentFolder, SubFolder, FileName, is, mhandler);
    }

    private class UploadOneDriveAsynTask extends AsyncTask<Void,Void,Void>{


        String ParentFolderName = null;
        String SubFolderName = null;
        String FileName = null;
        InputStream is = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ParentFolderName = _ParentFolderName;
            SubFolderName = _SubFolderName;
            FileName = _FileName;
            is = _is;

        }

        @Override
        protected Void doInBackground(Void... params) {


            //upload
            try {
                ArrayList<OneDriveItem> root =getRootFiles();
                OneDriveItem Parent_folder = OneDriveItem.FindFolder(ParentFolderName, root);
                String Parent_folder_id = null;
                if (Parent_folder == null)
                {
                    Parent_folder_id = CreateFolderNoAsync("me/skydrive",ParentFolderName);
                }else{
                    Parent_folder_id = Parent_folder.getId();
                }
                //done create parent folder

                //create child
                ArrayList<OneDriveItem> ParentFolderFiles = getFolderFiles(Parent_folder_id);
                OneDriveItem Child_folder = OneDriveItem.FindFolder(SubFolderName, ParentFolderFiles);
                String Child_folder_id= null;
                if (Child_folder == null)
                {
                    Child_folder_id = CreateFolderNoAsync(Parent_folder_id,SubFolderName);
                }else{
                    Child_folder_id = Child_folder.getId();
                }

                //restore input stream
                InputStream tmp_is;
                CopyInputStream copyInputStream = new CopyInputStream(is);
                is = copyInputStream.getCopy();
                tmp_is = copyInputStream.getCopy();

                //upload
                LiveOperation uploadOperation = client.upload(Child_folder_id, FileName, tmp_is);

                  Log.i("Upload_result",uploadOperation.getResult().toString());
            }
            catch (Exception e){
                //failed
                Onedrive_failed_handle();
                e.printStackTrace();}


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Onedrive_success_handle();
        }
    }

    private String CreateFolderNoAsync(String path ,String name)
    {
        String result_id = null;
        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("description", "Keep Calm and Meow On");
            LiveOperation liveOperation = client.post(path,body);
            JSONObject result = liveOperation.getResult();
            result_id = result.optString("id");
        }
        catch (Exception e){e.printStackTrace();}
        return result_id;
    }

    private ArrayList<OneDriveItem> getFolderFiles(String Folder_id) {
        ArrayList<OneDriveItem> items = new ArrayList<OneDriveItem>();

        try {
            LiveOperation liveOperation = client.get(Folder_id+"/files");
            JSONObject jsonSource = liveOperation.getResult();
            JSONArray listItem = jsonSource.getJSONArray("data");
            for (int i = 0; i < listItem.length(); i++) {
                JSONObject fileObject = listItem.getJSONObject(i);
                String Name = fileObject.getString("name");
                String id = fileObject.getString("id");
                String type = fileObject.getString("type");
                OneDriveItem oneDriveItem = new OneDriveItem(Name, id, type);
                items.add(oneDriveItem);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return items;
    }

    private ArrayList<OneDriveItem> getRootFiles() {
        ArrayList<OneDriveItem> items = new ArrayList<OneDriveItem>();

        try {
            LiveOperation liveOperation = client.get("me/skydrive/files");
            JSONObject jsonSource = liveOperation.getResult();
            JSONArray listItem = jsonSource.getJSONArray("data");
            for (int i = 0; i < listItem.length(); i++) {
                JSONObject fileObject = listItem.getJSONObject(i);
                String Name = fileObject.getString("name");
                String id = fileObject.getString("id");
                String type = fileObject.getString("type");
                OneDriveItem oneDriveItem = new OneDriveItem(Name, id, type);
                items.add(oneDriveItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    private void Onedrive_failed_handle(){
        Message message = new Message();
        message.arg1 = -1;//error
        handler.sendMessage(message);
        handler=null;
    }

    private void Onedrive_success_handle(){
        Message message = new Message();
        message.arg1 = 1;//success
        handler.sendMessage(message);
        handler=null;
    }

}
