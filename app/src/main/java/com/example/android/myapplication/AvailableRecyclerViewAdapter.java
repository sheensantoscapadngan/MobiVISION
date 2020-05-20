package com.example.android.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AvailableRecyclerViewAdapter extends RecyclerView.Adapter<AvailableRecyclerViewAdapter.ViewHolders> {


    private AdapterCallback adapterCallback;

    private ArrayList<String> buildingNames = new ArrayList<>();
    private Context context;

    public AvailableRecyclerViewAdapter(ArrayList<String> buildingNames, Context context) {

        adapterCallback = ((AdapterCallback)context);
        this.buildingNames = buildingNames;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.availablelist_layout,parent,false);


        return new AvailableRecyclerViewAdapter.ViewHolders(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolders holder, final int position) {

        holder.search_name.setText(buildingNames.get(position));

        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tableName = holder.search_name.getText().toString();
                tableName = tableName.replaceAll("\\s","5");
                adapterCallback.foo(tableName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return buildingNames.size();
    }

    public class ViewHolders extends RecyclerView.ViewHolder{

        TextView search_name, view_details;
        ImageView play;

        public ViewHolders(View itemView) {
            super(itemView);

            search_name = (TextView) itemView.findViewById(R.id.textViewAvailableListLayoutName);
            view_details = (TextView) itemView.findViewById(R.id.textViewAvailableListLayoutViewDetails);
            play = (ImageView) itemView.findViewById(R.id.imageViewAvailableListLayoutDownload);

        }
    }
}
