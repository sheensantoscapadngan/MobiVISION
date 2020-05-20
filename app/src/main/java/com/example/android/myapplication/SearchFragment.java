package com.example.android.myapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class SearchFragment extends Fragment {

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private EditText search;
    private RecyclerView feed;
    private ArrayList<String> buildingList = new ArrayList<>();
    private RecyclerViewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);


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

                if(s.toString().length() > 0)
                    addToRecyclerView(s.toString());
            }
        });
    }

    private void addToRecyclerView(final String s) {

        databaseReference.child(s).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    buildingList.add(s);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setupViews(View view) {

        search = (EditText) view.findViewById(R.id.editTextSearchSearch);
        feed = (RecyclerView) view.findViewById(R.id.recyclerViewSearch);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Buildings");

        adapter = new RecyclerViewAdapter(buildingList,getActivity().getApplicationContext());
        feed.setAdapter(adapter);
        feed.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

    }

}
