package com.example.android.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class AvailableFragment extends Fragment{

    private EditText search;
    private RecyclerView feed;
    private ArrayList<String> buildingList = new ArrayList<>();
    private AvailableRecyclerViewAdapter adapter;
    private DatabaseConnect databaseConnect;
    private Cursor downloadedList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_available, container, false);

        setupViews(view);
        activateListeners();

        return view;

    }

    private void activateListeners() {

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                buildingList.clear();
                adapter.notifyDataSetChanged();

                if(s.toString().length() > 0){
                    Boolean found = findSQL(s.toString());
                    if(found){
                        addToRecyclerView(s.toString());
                    }
                }

            }
        });
    }

    private Boolean findSQL(String s) {

        downloadedList =  databaseConnect.getTableList();
        Boolean flag = false;
        if(downloadedList.getCount() > 0){
            while(downloadedList.moveToNext()){
                if(s.equals(downloadedList.getString(0)))
                    flag = true;
            }
        }

        if(flag)
            return true;
        else
            return false;

    }


    private void addToRecyclerView(final String s) {

        buildingList.add(s);
        adapter.notifyDataSetChanged();

    }

    private void setupViews(View view) {

        search = (EditText) view.findViewById(R.id.editTextAvailableSearch);
        feed = (RecyclerView) view.findViewById(R.id.recyclerViewAvailable);
        databaseConnect = new DatabaseConnect(getActivity().getApplicationContext());
        downloadedList =  databaseConnect.getTableList();

        adapter = new AvailableRecyclerViewAdapter(buildingList,getActivity());
        feed.setAdapter(adapter);
        feed.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

    }
}


