package com.example.dotoan.musicrecommendation.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.dotoan.musicrecommendation.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class CentroidCreate extends Service {
    int n = 25;
    int nMusic =  13369;
    int nUser = 1259;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = db.getReference();

    public CentroidCreate() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cenSelect();
    }

    private boolean cenSelect (){
        Log.e("cenSelect","Running...");
        final boolean[] kt = {false};
        final boolean[] kt2 = {false};
        final boolean[] k = {false};

        databaseReference.child("Kmean").removeValue();
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                int j = 0;

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot zone : dataSnapshot.child("habitatMatrix").getChildren()) {
                        int j2=0;
                            for (DataSnapshot zoneDetail : zone.getChildren()) {
                                kt2[0] = false;
                                databaseReference.child("Kmean").child("centroid").child(zone.getKey()).child(zoneDetail.getKey()).setValue(zoneDetail.getValue());
                                Log.i("System.out",zone.getKey()+" - "+zoneDetail.getKey()+" : "+zoneDetail.getValue());
                                if (j2++ == zone.getChildrenCount()-1) kt2[0] = true;
                            }
                            j++;
                            if (j==n) {
                                break;
                            }
                        }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        return kt[0];
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("CentroidCreate","Stop");
    }

    private void cenIni(final int m[][]){
        final int tempini[][] = new int [nUser][nMusic];

        for (int i =0; i<nUser;i++){
            for (int j =0; j< nMusic;j++){
                tempini[i][j] = 0;
            }
        }

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Kmean/centroid").removeValue();
        databaseReference.addValueEventListener(new ValueEventListener() {
            int i =0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot zone:dataSnapshot.child("habitatMatrix").getChildren()){
                    int upos = Integer.parseInt(zone.getKey().toString());
                    for(DataSnapshot zoneDetail: zone.getChildren()){
                        int pos = Integer.parseInt(zoneDetail.getKey().toString());
                        int val = Integer.parseInt(zoneDetail.getValue().toString());
                        tempini[upos][pos] = val;
                    }
                    if (i++ == zone.getChildrenCount()-1) databaseReference.child("app").child("control").child("newCen").setValue(true);
                }

                boolean s = (boolean) dataSnapshot.child("app").child("control").child("newCen").getValue();
                if (s){
                    newCen(m,tempini);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void newCen(int m[][],int tempini[][]){
        int temp[];
        for (int i=0;i<n;i++){
            temp = new int[nUser];
            int k =0;
            for (int j =0;j<nUser; j++){
                if (m[i][j] == 1) temp[k++] = j;
            }

            DatabaseReference data = FirebaseDatabase.getInstance().getReference("Kmean/centroid");

            for (int t = 0; t< nMusic; t++ ) {
                float sol = 0;
                for (int j = 0; j < temp.length; j++) {
                    sol = sol + tempini[temp[j]][t];
                }
                sol = sol/temp.length;
                data.child(String.valueOf(i)).child(String.valueOf(t)).setValue(sol);
                Log.e("check","String.valueOf("+i+")).child(String.valueOf("+t+")).setValue("+sol+")");
            }
        }
    }
}
