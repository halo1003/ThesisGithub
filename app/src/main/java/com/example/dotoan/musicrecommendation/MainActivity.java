package com.example.dotoan.musicrecommendation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.example.dotoan.musicrecommendation.services.CentroidCreate;
import com.example.dotoan.musicrecommendation.services.DistanceCom;
import com.example.dotoan.musicrecommendation.services.FirebaseEventListener;
import com.example.dotoan.musicrecommendation.services.UpdateService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.signup) Button btnSignUp;
    @BindView(R.id.login) Button btnLogin;
    @BindView(R.id.lilaspinner) LinearLayout linearLayout;
    @BindView(R.id.spinner) ProgressBar progressBar;
    @BindView(R.id.processText) TextView txtv;
    @BindView(R.id.updateText) TextView txtvUpdate;

    int n = 25;
    int nMusic = 13369;
    int nUser = 1259;

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
        Log.e("MainActivity","run");
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
        Intent listen = new Intent(MainActivity.this, FirebaseEventListener.class);
        startService(listen);

        final DatabaseReference data = FirebaseDatabase.getInstance().getReference("app");
        data.child("controlv2").setValue(false);

        data.child("control").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int c = Integer.parseInt(dataSnapshot.getValue().toString());
                switch (c){
                    case 0:
                        Log.e("switch",0+"");
                        Intent Centroid_i = new Intent(getApplicationContext(), CentroidCreate.class);
                        startService(Centroid_i);

                        break;
                    case 1:
                        Log.e("switch",1+"");
                        Intent i = new Intent(getApplicationContext(), CentroidCreate.class);
                        stopService(i);

                        DatabaseReference d2 = FirebaseDatabase.getInstance().getReference();
                        d2.child("app").child("control").setValue(2);

                        new DistanceComputeAsyn().execute();
                        break;
                    case 2:
                        Log.e("switch",2+"");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        data.child("controlv2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals("true")){
                    data.child("control").setValue(1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

    public class DistanceComputeAsyn extends AsyncTask<String,Integer,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("DistanceComputeAsyn","DONE");
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i("DistanceComputeAsyn","Running");
            final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                int cenPos[];
                int cenVal[] ;

                int objPos[] ;
                int objVal[] ;

                float simCombine[][];

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    int nx = (int) dataSnapshot.child("Kmean/centroid").getChildrenCount();
                    int ny = (int) dataSnapshot.child("habitatMatrix").getChildrenCount();

                    simCombine = new float [nx][ny];
                    int x =0;
                    for (DataSnapshot zone: dataSnapshot.child("Kmean/centroid").getChildren()){
                        cenPos = new int[(int) zone.getChildrenCount()];
                        cenVal = new int[(int) zone.getChildrenCount()];
                        int i =0, y=0;
                        for(DataSnapshot zoneDetail: zone.getChildren()){
                            cenPos[i] = Integer.parseInt(zoneDetail.getKey().toString());
                            cenVal[i++] = Integer.parseInt(zoneDetail.getValue().toString());
                        }

                        for (DataSnapshot zone1: dataSnapshot.child("habitatMatrix").getChildren()){
                            objPos = new int[(int) zone1.getChildrenCount()];
                            objVal = new int[(int) zone1.getChildrenCount()];
                            int j =0;
                            for (DataSnapshot zoneDetail: zone1.getChildren()){
                                objPos[j] = Integer.parseInt(zoneDetail.getKey().toString());
                                objVal[j++] = Integer.parseInt(zoneDetail.getValue().toString());
                            }

                            if (cenVal != null && cenPos !=null){
                                float sim = similarityDistance(cenVal,cenPos,objVal,objPos);
                                simCombine[x][y++] = sim;
                                Log.i("System.out","Similarity of user " + zone.getKey() +" with user "+zone1.getKey()+" equal "+ String.valueOf(sim));
                            }
                        }
                        Log.e("ENDLOOP",x+" : " +simCombine[x].length+ " ------------------------------------------------------------------");
                        x++;
                    }
                    groupCen(simCombine);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Error",databaseError+"");
                }
            });
            return null;
        }

        private int[][] Matrix(){
            final int m[][] = new int[nUser][nMusic];
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("habitatMatrix");
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                int i =0;
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot zone: dataSnapshot.getChildren()){
                        int row = Integer.parseInt(zone.getKey().toString());
                        for (DataSnapshot zonedetail: zone.getChildren()){
                            int column = Integer.parseInt(zonedetail.getKey().toString());
                            int value = Integer.parseInt(zonedetail.getValue().toString());
                            for (int j = i; j<column; j++){
                                m[row][j] = 0;
                            }
                            m[row][column] = value;
                            Log.i("Matrix","m["+row+"]["+column+"] = "+value);
                            i = column+1;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return m;
        }

        private float similarityDistance(int centVal[],int centPos[], int objVal[],int objPos[]){
            float tag =0, cenX=0,cenY=0,sol =0;
            if (centPos.length>=objPos.length) {
                for (int i = 0; i < centPos.length; i++) {
                    if (i<objPos.length && centPos[i] == objPos[i]) {
                        tag = centVal[i] - objVal[i];
                    } else {
                        cenX = centVal[i];
                        if (i < objPos.length) cenY = objVal[i];
                    }
                    sol = sol + tag * tag + cenX * cenX + cenY * cenY;
                }
            }else{
                for (int i = 0; i < objPos.length; i++) {
                    if (i< centPos.length && centPos[i] == objPos[i]) {
                        tag = centVal[i] - objVal[i];
                    } else {
                        if (i<centPos.length) cenX = centVal[i];
                        cenY = objVal[i];
                    }
                    sol = sol + tag * tag + cenX * cenX + cenY * cenY;
                }
            }
            return (float) Math.sqrt(sol);
        }

        private void groupCen(float m[][]){
            Log.e("groupCen","Running...");
            final int combine[][] = new int [n][nUser];
            float checkm[];
            for (int i =0; i < n;i++ ) {
                for (int j = 0; j < nUser; j++) {
                    combine[i][j] = 0;
                }
            }

            Log.e("SYS","--------------------------------------------------");
            Log.e("SYS","m[0].length:"+m[0].length+" n:"+n);
            Log.e("SYS","--------------------------------------------------");

            for (int i =0 ; i< m[0].length;i++){
                checkm = new float[n];
                List<Float> a = new ArrayList<Float>();
                for (int j =0; j< n; j++){
                    checkm[j] = m[j][i];
                    a.add(m[j][i]);
                }
                combine[smallestVal(checkm)][i] = 1;
                Log.e("a",a+"");
                Log.i("System.out","combine["+smallestVal(checkm)+"]["+i+"])");
            }
            Log.e("Balance: ",balance(combine)+"");

            if (!balance(combine)) {
                databaseReference.child("Kmean").child("centroid").removeValue();
                for (int i =0; i<n;i++) {
                    final List<Integer> sumMatrix = new ArrayList<Integer>();
                    for (int j = 0; j < nUser; j++){
                        if (combine[i][j] == 1){
                            sumMatrix.add(j);
                        }
                    }
                    Log.e("sumMatrix",i+": "+sumMatrix+"");

                    if (sumMatrix.size()>1){
//                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//                        final int finalI1 = i;
//                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                for (int j : sumMatrix){
//                                    for (DataSnapshot zone: dataSnapshot.child("habitatMatrix").child(String.valueOf(j)).getChildren()){
//                                        databaseReference.child("Kmean").child("centroid").child(String.valueOf(finalI1)).child(zone.getKey()).setValue(zone.getValue());
//                                        Log.i("System.out",finalI1+" / "+zone.getKey()+" / "+zone.getValue());
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
                        int habitat[][] = new int [nUser][nMusic];
                        habitat = Matrix();
                    }else {
                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        final int finalI1 = i;
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (int j : sumMatrix){
                                        for (DataSnapshot zone: dataSnapshot.child("habitatMatrix").child(String.valueOf(j)).getChildren()){
                                            databaseReference.child("Kmean").child("centroid").child(String.valueOf(finalI1)).child(zone.getKey()).setValue(zone.getValue());
                                            Log.i("System.out",finalI1+" / "+zone.getKey()+" / "+zone.getValue());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                    }
                }
            }
        }

        private boolean balance(int m[][]){
            boolean k = false;
            int temp[] = new int [nUser];
            for (int i =0; i < n; i++){
                int sum =0;
                for (int j = 0;j < nUser;j++){
                    sum += m[i][j];
                }
                Log.d("Balance number",i+" -> "+sum);
                temp[i]= sum;
            }

            int min = temp[0], max = temp[0];

            for (int i =0; i< temp.length;i++){
                if (temp[i]<min) {
                    min = temp[i];
                    break;
                }

                if (temp[i]>max){
                    max = temp[i];
                    break;
                }

                if (i == temp.length-1 && max == min){
                    k = true;
                }
            }
            return k;
        }

        private int smallestVal(float m[]){
            float sma = m[0];
            int pos =0;
            for (int i =1;i<m.length;i++){
                if (sma > m[i]) {
                    sma = m[i];
                    pos = i;
                }
            }
            return pos;
        }
    }
}
