package com.example.android.myapplication;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class HomeFragment extends Fragment {

    private TextView connected,table;
    private MainActivity mainActivity;
    private String name,table_name;
    private Bundle globalData;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        setupViews(view);
        activateListeners(view);

        return view;

    }

    private void activateListeners(View view) {

    }

    private void setupViews(View view) {

        connected = (TextView) view.findViewById(R.id.textViewHomeConnected);
        table = (TextView) view.findViewById(R.id.textViewHomeTable);
        mainActivity = (MainActivity) getActivity();

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && isResumed()){
            globalData = mainActivity.getGlobalData();
            name = globalData.getString("name");
            table_name = globalData.getString("table_name");
            table_name = table_name.replace("5"," ");

            connected.setText("Connected to : " + name);
            table.setText(table_name);
        }
    }
}

