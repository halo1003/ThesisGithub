package com.example.dotoan.musicrecommendation.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Cuckoo_select extends Service {
    String user = "0";
    float radius =0;
    public Cuckoo_select() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("habitatMatrix");
        databaseReference.child(user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long childcount = dataSnapshot.getChildrenCount();
                int first_Cuckoo[] = new int[(int) childcount];
                int temp = 0;
                for (DataSnapshot zone : dataSnapshot.getChildren()){
                    first_Cuckoo[temp++] = Integer.parseInt(zone.getKey().toString());
                }
                getCuckoo(first_Cuckoo[0],first_Cuckoo[(int) (childcount-1)], (int) childcount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getCuckoo(final int small, final int large, final int number){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("habitatMatrix");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int N_cuckoo = 0;
                long N_normalbird = dataSnapshot.getChildrenCount();
                for(DataSnapshot zone : dataSnapshot.getChildren()){
                    long zonecount = zone.getChildrenCount();
                    int detail[] = new int[(int) zonecount];
                    int temp = 0;
                    for (DataSnapshot zonedetail:zone.getChildren()){
                        int s = Integer.parseInt(zonedetail.getKey().toString());
                        detail[temp++] = s;
                    }
                    if(detail[0]>large || detail[(int) (zonecount-1)]<small || zonecount<(number-3)){

                    }else{
                        Log.e("CUCKOO",zone.getKey().toString() + " "+ detail[0]+" "+detail[(int) (zonecount-1)]);
                        N_cuckoo++;
                    }
                }
                radius = (float) N_normalbird/(float) N_cuckoo;
                Log.e("Radius", N_cuckoo+" "+ N_normalbird+" "+String.valueOf(radius));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
