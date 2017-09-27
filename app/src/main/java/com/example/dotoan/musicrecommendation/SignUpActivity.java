package com.example.dotoan.musicrecommendation;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.morphingbutton.MorphingButton;
import com.example.dotoan.musicrecommendation.signup.fragment.fragment_email_pass;
import com.example.dotoan.musicrecommendation.signup.fragment.fragment_username;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpActivity extends FragmentActivity implements fragment_email_pass.inteFragment, fragment_username.inteFragment2 {

    @BindView(R.id.submit) MorphingButton btnSubmit;
    @BindView(R.id.txtvError) TextView txtvError;
    @BindView(R.id.viewpage) ViewPager viewpager;

    private String email,password,username;
    private boolean regE = false;
    private boolean regP = false;

    private FirebaseAuth mAuth;
    String TAG = "DT_TAG";

    private int signup_action = 0;
    private boolean signUp_1st = true;
    public int h,w;
    private String text;
    private int duration = 500;

    private boolean btnActived = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        PagerAdapter padapter = new PagerAdapter(getSupportFragmentManager());
        viewpager.setAdapter(padapter);

        ViewTreeObserver vto = btnSubmit.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                btnSubmit.getViewTreeObserver().removeOnPreDrawListener(this);
                h = btnSubmit.getMeasuredHeight();
                w = btnSubmit.getMeasuredWidth();
                Log.d("hw",h+" "+w+"");
                return true;
            }
        });

        viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1) {
                    txtvError.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        text = "NEXT";
                        signUp_1st = true;
                        if (signup_action == 1) {

                            signup_action = 0;
                            morphToSquare(btnSubmit,text);
                        }else{
                            btnSubmit.setText(text);
                        }

                        break;
                    case 1:
                        Log.d("view",position+"");
                        signUp_1st = false;
                        text = "SIGN UP";
                        btnSubmit.setText(text);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btnSubmit.setText("NEXT");

        mAuth = FirebaseAuth.getInstance();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (signup_action) {
                    case 0:
                        if (signUp_1st) {
                            viewpager.setCurrentItem(1, true);
                        } else {
                            if (regE && regP) {
                                Registration(email, password);
                            } else {
                                morphToFail(btnSubmit);
                                if (regE && regP) txtvError.setText("Email is required");
                                else if (regE && !regP) txtvError.setText("Password is required");
                                else txtvError.setText("Email and Password are required");
                                signup_action = 1;
                                txtvError.setVisibility(View.VISIBLE);
                            }
                        }
                        break;

                    case 1:
                        viewpager.setCurrentItem(0,true);
                        morphToSquare(btnSubmit,text);
                        //txtvError.setVisibility(View.INVISIBLE);
                        signup_action = 0;
                        break;

                    default:
                        break;
                }
            }
        });
    }

    protected void Registration(String email,String password){
        Toast.makeText(getApplicationContext(), "Please wait...", Toast.LENGTH_SHORT).show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (username == null){
                                TelephonyManager telephonyManager = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

                                String deviceId = telephonyManager.getDeviceId();
                                Log.d(TAG, "getDeviceId() " + deviceId);
                                username = deviceId;
                            }

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                txtvError.setTextColor(getResources().getColor(R.color.colorGreen));
                                                txtvError.setText("Success: Wellcome "+username);
                                                txtvError.setVisibility(View.VISIBLE);
                                                morphToSuccess(btnSubmit);
                                            }
                                        }
                                    });
                        } else {
                            signup_action = 1;
                            morphToFail(btnSubmit);
                            txtvError.setText(task.getException().getMessage().toString());
                            txtvError.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void morphToSuccess(final MorphingButton btnMorph) {
        btnActived = false;
        btnSubmit.setClickable(btnActived);
        MorphingButton.Params circle = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius(100)
                .width(h)
                .height(h)
                .icon(R.drawable.ic_done);
        btnMorph.morph(circle);
    }

    private void morphToFail(final MorphingButton btnMorph) {
        btnActived = false;
        btnSubmit.setClickable(btnActived);
        MorphingButton.Params circle = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius(100)
                .width(h)
                .height(h)
                .icon(R.drawable.ic_fail);
        btnMorph.morph(circle);

        if (!btnActived) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    btnSubmit.setClickable(true);
                    btnActived = true;
                }
            }, duration+100);
        }
    }

    private void morphToSquare(final MorphingButton btnMorph,String s){
        btnActived = false;
        btnSubmit.setClickable(btnActived);
        MorphingButton.Params square = MorphingButton.Params.create()
                .cornerRadius(100)
                .width(w)
                .height(h)
                .strokeColor(getColor(R.color.colorBlack))
                .color(getColor(R.color.colorOrange))
                .colorPressed(getColor(R.color.colorGoogle))
                .text(s)
                .duration(duration);
        btnMorph.morph(square);

        if (!btnActived) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    btnSubmit.setClickable(true);
                    btnActived = true;
                }
            }, duration+100);
        }
    }

    @Override
    public String getEmail(String email) {
        this.email = email;
        return this.email;
    }

    @Override
    public String getPassword(String password) {
        this.password = password;
        return this.password;
    }

    @Override
    public boolean getRegE(boolean regE) {
        this.regE = regE;
        return this.regE;
    }

    @Override
    public boolean getRegP(boolean regP) {
        this.regP = regP;
        return this.regP;
    }

    @Override
    public void getUsername(String user) {
        this.username = user;
    }
}
