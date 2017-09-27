package com.example.dotoan.musicrecommendation.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.dotoan.musicrecommendation.LoginActivity;
import com.example.dotoan.musicrecommendation.MainActivity;
import com.example.dotoan.musicrecommendation.MainPage.NavigationActivity;
import com.example.dotoan.musicrecommendation.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UpdateService extends Service {

    String app;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String deviceId;
    public UpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        final Handler handler = new Handler();
//        final int delay = 1000; //milliseconds
//        handler.postDelayed(new Runnable(){
//            public void run(){
//                //do something
//                DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy");
//                DateFormat dt = new SimpleDateFormat("HH:mm:ss");
//                String date = df.format(Calendar.getInstance().getTime());
//                String time = dt.format(Calendar.getInstance().getTime());
//
//                databaseReference.child("realtime").child(deviceID()).child("User").child("Online").child("Date").setValue(date);
//                databaseReference.child("realtime").child(deviceID()).child("User").child("Online").child("Time").setValue(time);
//                handler.postDelayed(this, delay);
//            }
//        }, delay);
        new downloadAsync().execute();
    }

    private String deviceID(){
        TelephonyManager telephonyManager = (TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();
        return deviceId;
    }

    private class downloadAsync extends AsyncTask<String,Long,String>{
        @Override
        protected String doInBackground(String... params) {
            DatabaseReference mCount = FirebaseDatabase.getInstance().getReference();
            mCount.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final long count = dataSnapshot.child("musics").getChildrenCount();
                    final long countu = dataSnapshot.child("udata").getChildrenCount();
                    Log.e("count", String.valueOf(count));

                    if (count!=0 &&countu!=0) {
                        final String musics[] = new String[(int) count];
                        final String users[] = new String[(int)countu];

                        if(CreateHabitatValue(musics) && CreateUserValue(users)){
                            for (int i =0;i<users.length;i++){
                                getListen(i,musics);
                            }

                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }

        private boolean getListen(final int i, final String spade[]){
            final boolean[] getted = {false};
                DatabaseReference mget = FirebaseDatabase.getInstance().getReference("udata");
                mget.child(String.valueOf(i)).child("listen").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot zone :dataSnapshot.getChildren()){
                                for (DataSnapshot detailzone : zone.getChildren()){
                                    for(int j =0;j<spade.length;j++){
                                        if (detailzone.getKey().toString().equals(spade[j])){
                                            DatabaseReference data = FirebaseDatabase.getInstance().getReference("habitatMatrix");
                                            data.child(String.valueOf(i)).child(String.valueOf(j)).setValue(detailzone.getValue());
                                            Log.e("user", String.valueOf(i));
                                            break;
                                        }
                                    }
                                }
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            return getted[0];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getBaseContext(),"Starting...",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        private boolean CreateHabitatValue(final String m[]){
            DatabaseReference mdata = FirebaseDatabase.getInstance().getReference("musics");
            mdata.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    File rootPath = new File(Environment.getExternalStorageDirectory(), "Download");
                    if(!rootPath.exists()) {
                        rootPath.mkdirs();
                    }
                    File localFile = new File(rootPath,"habitatValue.txt");

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(localFile);
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                        for (DataSnapshot zone : dataSnapshot.getChildren()){
                            String _id = zone.child("_id").getValue().toString();
                            String mid = zone.child("mid").getValue().toString();

                            m[Integer.parseInt(_id)] = mid;
                            Log.d("DT_TAG",m[Integer.parseInt(_id)]+":"+_id);

                            bw.write(_id+":"+mid);
                            bw.newLine();
                        }

                        bw.close();
                        Toast.makeText(getBaseContext(),"habitatValue created",Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;
        }

        private boolean CreateUserValue(final String m[]){
            DatabaseReference mdata = FirebaseDatabase.getInstance().getReference("udata");
            mdata.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    File rootPath = new File(Environment.getExternalStorageDirectory(), "Download");
                    if(!rootPath.exists()) {
                        rootPath.mkdirs();
                    }
                    File localFile = new File(rootPath,"userValue.txt");

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(localFile);
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                        for (DataSnapshot zone : dataSnapshot.getChildren()){
                            String _id = zone.child("_id").getValue().toString();
                            String zoneid = zone.getKey();

                            m[Integer.parseInt(zoneid)] = _id;
                            Log.d("DT_TAG",m[Integer.parseInt(zoneid)]+":"+_id);

                            bw.write(zoneid+":"+_id);
                            bw.newLine();
                        }

                        bw.close();
                        Toast.makeText(getBaseContext(),"userValue created",Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;
        }
    }
}
