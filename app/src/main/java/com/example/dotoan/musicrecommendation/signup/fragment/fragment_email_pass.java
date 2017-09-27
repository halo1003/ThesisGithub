package com.example.dotoan.musicrecommendation.signup.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.dotoan.musicrecommendation.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by DOTOAN on 9/19/2017.
 */

public class fragment_email_pass extends Fragment{

    public interface inteFragment{
        public String getEmail(String email);
        public String getPassword(String password);
        public boolean getRegE(boolean regE);
        public boolean getRegP(boolean regP);
    }

    @BindView(R.id.name) EditText edtEmail;
    @BindView(R.id.password) EditText edtPassword;

    inteFragment listener;
    String email,password;
    boolean regE = false, regP = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (inteFragment) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.fragment_email_pass,container,false);
        ButterKnife.bind(this,view);

        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                edtEmail.setError( "Email is required!" );
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                email = edtEmail.getText().toString();
                regE = true;

                if (listener!=null) {
                    listener.getEmail(email);
                    listener.getRegE(regE);
                }
            }
        });

        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                edtPassword.setError( "Password is required!" );
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                password = edtPassword.getText().toString();
                regP = true;

                if (listener!=null) {
                    listener.getPassword(password);
                    listener.getRegP(regP);
                }
            }
        });

        return view;
    }
}
