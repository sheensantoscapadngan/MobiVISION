package com.example.android.myapplication;

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

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private ArrayList<String> buildingNames = new ArrayList<>();
    private Context context;

    public RecyclerViewAdapter(ArrayList<String> buildingNames,Context context) {
        this.buildingNames = buildingNames;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.searchlist_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        holder.search_name.setText(buildingNames.get(position));
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context,DownloadLoadingActivity.class);
                intent.putExtra("table_name",buildingNames.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return buildingNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView search_name, view_details;
        ImageView download;

        public ViewHolder(View itemView) {
            super(itemView);

            search_name = (TextView) itemView.findViewById(R.id.textViewSearchListLayoutName);
            view_details = (TextView) itemView.findViewById(R.id.textViewSearchListLayoutViewDetails);
            download = (ImageView) itemView.findViewById(R.id.imageViewAvailableListLayoutDownload);

        }

    }
}
