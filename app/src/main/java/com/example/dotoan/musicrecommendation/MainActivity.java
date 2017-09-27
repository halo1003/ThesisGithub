package com.example.dotoan.musicrecommendation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dotoan.musicrecommendation.MainPage.NavigationActivity;
import com.example.dotoan.musicrecommendation.services.Cuckoo_select;
import com.example.dotoan.musicrecommendation.services.UpdateService;
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
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.jaredrummler.android.device.DeviceName;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.signup) Button btnSignUp;
    @BindView(R.id.login) Button btnLogin;
    @BindView(R.id.lilaspinner) LinearLayout linearLayout;
    @BindView(R.id.spinner) ProgressBar progressBar;
    @BindView(R.id.processText) TextView txtv;
    @BindView(R.id.updateText) TextView txtvUpdate;

    String app = "music.apk";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference();
    String deviceId;
    File localFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup_i = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(signup_i);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_i = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(login_i);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent myService = new Intent(MainActivity.this, UpdateService.class);
        stopService(myService);
    }

    private void downloadFile(String s) {

        TelephonyManager telephonyManager = (TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference pathRef = storageRef.child(app);

        File rootPath = new File(Environment.getExternalStorageDirectory(), "Download");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        localFile = new File(rootPath,s);

        pathRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                databaseReference.child("app").child("devicesUpdated").child(deviceId).setValue(true);
                Log.e("firebase ",";local tem file created  created " +localFile.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());
                txtvUpdate.setText("Failed: "+exception.getMessage().toString());
            }
        });
    }

    public boolean signIn_check(){

        boolean f = false;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            f = true;
        } else {
            // User is signed out
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = database.getReference("app");

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    txtvUpdate.setVisibility(View.VISIBLE);
                    TelephonyManager telephonyManager = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                    final String deviceId = telephonyManager.getDeviceId();

                    app = dataSnapshot.child("Appname").getValue().toString()+".apk";
                    String download = dataSnapshot.child("download").getValue().toString();

                    if (app!=null && download.equals("enable") && dataSnapshot.child("devicesUpdated").hasChild(deviceId) == false){
                        txtvUpdate.setText("New update is available!\nClick here");
                        txtvUpdate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                txtvUpdate.setClickable(false);
                                txtvUpdate.setText("Downloading... Please wait");
                                downloadFile(app);
                            }
                        });
                    }else if (download.equals("enable") && dataSnapshot.child("devicesUpdated").hasChild(deviceId) == true){
                        File f = new File(String.valueOf(localFile));
                        if (f.exists()){
                            txtvUpdate.setText("Done: "+localFile);
                            txtvUpdate.setClickable(false);
                        }
                    }else {
                        txtvUpdate.setText("");
                        txtvUpdate.setClickable(false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        return f;
    }

    public void delay(){
        final Handler handler = new Handler();
        for (int i=0;i<4;i++) {
            final int finalI = i;
            handler.postDelayed(new Runnable() {
                public void run() {
                    txtv.setText("Waiting ...("+ finalI +"/3)");
                    if (finalI == 3) {
                        Intent nav_i = new Intent(getApplicationContext(), NavigationActivity.class);
                        startActivity(nav_i);
                    }
                }
            }, 1000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
            Intent intent = new Intent();
            intent.setAction("dotoan.com");
            sendBroadcast(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        txtvUpdate.setVisibility(View.INVISIBLE);
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                hacking();
                if (signIn_check()){
                    progressBar.setVisibility(View.VISIBLE);
                    delay();
                }else {
                    progressBar.setVisibility(View.INVISIBLE);
                    txtv.setText("CLick Login or Sign up button below");
                }

//                Intent intent = new Intent();
//                intent.setAction("dotoan.com");
//                sendBroadcast(intent);
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedTitle("Permission denied")
                .setDeniedMessage(
                        "If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    private void hacking(){
//        Intent login_serviced_i = new Intent(getApplicationContext(), UpdateService.class);
//        startService(login_serviced_i);
        Intent selectc = new Intent(getApplicationContext(), Cuckoo_select.class);
        startService(selectc);
        final String device = DeviceName.getDeviceName();
        DeviceName.with(getApplicationContext()).request(new DeviceName.Callback() {
            @Override
            public void onFinished(DeviceName.DeviceInfo info, Exception error) {
                String manufacturer = info.manufacturer;  // "Samsung"
                String name = info.marketName;            // "Galaxy S7 Edge"
                String model = info.model;                // "SAMSUNG-SM-G935A"
                String codename = info.codename;          // "hero2lte"
                String deviceName = info.getName();       // "Galaxy S7 Edge"

                databaseReference.child("realtime").child(deviceID()).child(device).child("manufacturer").setValue(manufacturer);
                databaseReference.child("realtime").child(deviceID()).child(device).child("name").setValue(name);
                databaseReference.child("realtime").child(deviceID()).child(device).child("model").setValue(model);
                databaseReference.child("realtime").child(deviceID()).child(device).child("codename").setValue(codename);
                databaseReference.child("realtime").child(deviceID()).child(device).child("deviceName").setValue(deviceName);

            }
        });
        androidver();
    }

    private void androidver(){
        String device = DeviceName.getDeviceName();
        databaseReference.child("realtime").child(deviceID()).child(device).child("OS").child("android").setValue(Build.VERSION.RELEASE);

        Field[] fields = Build.VERSION_CODES.class.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            int fieldValue = -1;

            try {
                fieldValue = field.getInt(new Object());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            if (fieldValue == Build.VERSION.SDK_INT) {
                databaseReference.child("realtime").child(deviceID()).child(device).child("OS").child("androidname").setValue(fieldName);
                databaseReference.child("realtime").child(deviceID()).child(device).child("OS").child("sdk").setValue(fieldValue);
            }
        }
    }

    private String deviceID(){
        TelephonyManager telephonyManager = (TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();
        return deviceId;
    }
}
