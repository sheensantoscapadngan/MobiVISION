package com.example.android.myapplication;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DownloadLoadingActivity extends AppCompatActivity {

    private String table_name;
    private DatabaseReference databaseReference;
    private DatabaseConnect databaseConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_loading);

        setupViews();
        setupSQL();
        setupFirebase();
        finish();

    }

    private void removeSpace() {
        table_name = table_name.replaceAll("\\s","5");
    }

    private void setupFirebase() {

        databaseConnect.insertTable(table_name);
        final String table_copy = table_name;
        table_name = table_name.replace("5"," ");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Buildings").child(table_name);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    Bundle info = new Bundle();
                    info.putString("location",ds.child("Location").getValue().toString());
                    info.putString("symbol",ds.child("Symbol").getValue().toString());
                    info.putString("front",ds.child("Front").getValue().toString());
                    info.putString("front_dir",ds.child("Front Direction").getValue().toString());
                    info.putString("left",ds.child("Left").getValue().toString());
                    info.putString("left_dir",ds.child("Left Direction").getValue().toString());
                    info.putString("right",ds.child("Right").getValue().toString());
                    info.putString("right_dir",ds.child("Right Direction").getValue().toString());

                    Boolean res = databaseConnect.saveInfo(info,table_copy);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Boolean res = databaseConnect.saveToDownloadedList(table_name);
        if(res)
            Toast.makeText(this, "SAVE SUCCESS!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "SAVE FAIL!", Toast.LENGTH_SHORT).show();

    }

    private void setupSQL() {
        databaseConnect = new DatabaseConnect(getApplicationContext());
    }

    private void setupViews() {

        table_name = getIntent().getStringExtra("table_name");
        removeSpace();

    }
}
