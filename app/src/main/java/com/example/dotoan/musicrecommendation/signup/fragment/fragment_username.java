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

public class fragment_username extends Fragment {

    public interface inteFragment2{
        public void getUsername(String user);
    }

    inteFragment2 listener;
    public String username;
    @BindView(R.id.name) EditText edtText;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (inteFragment2) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.fragment_username,container,false);
        ButterKnife.bind(this,view);

        edtText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                username = edtText.getText().toString();

                if (listener!= null){
                    listener.getUsername(username);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

}