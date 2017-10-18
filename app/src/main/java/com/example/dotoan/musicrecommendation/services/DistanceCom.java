package com.example.dotoan.musicrecommendation.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.dotoan.musicrecommendation.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DistanceCom extends Service {
    int n = getResources().getInteger(R.integer.cluster);
    int nMusic =  getResources().getInteger(R.integer.nMusic);
    int nUser = getResources().getInteger(R.integer.nUdata);

    public DistanceCom() {
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e("DistanceCom","running...");
//        Intent i = new Intent(getBaseContext(),CentroidCreate.class);
//        stopService(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    Log.e("END",simCombine[x].length+ " ------------------------------------------------------------------");
                    x++;
                }
                groupCen(simCombine);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error",databaseError+"");
            }
        });
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
        for (int i =0; i < n;i++ ){
            for (int j =0; j<nUser; j++){
                combine[i][j] = 0;
            }
        }

        for (int i =0 ; i< m[0].length;i++){
            checkm = new float[n];
            for (int j =0; j< n; j++){
                checkm[j] = m[j][i];
            }
            combine[smallestVal(checkm)][i] = 1;
            Log.i("System.out","combine["+smallestVal(checkm)+"]["+i+"])");
        }

        Log.d("balance: ",balance(combine)+"");

        //DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("app");
        //databaseReference.child("control").child("balance").setValue(balance(combine));
    }

    private boolean balance(int m[][]){
        boolean k = false;
        int temp[] = new int [nUser];
        for (int i =0; i < n; i++){
            int sum =0;
            for (int j = 0;j < nUser;j++){
                sum += m[i][j];
            }
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
            if (sma < m[i]) {
                sma = m[i];
                pos = i;
            }
        }
        return pos;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
