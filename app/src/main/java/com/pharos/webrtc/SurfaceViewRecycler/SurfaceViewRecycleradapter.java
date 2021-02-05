package com.pharos.webrtc.SurfaceViewRecycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pharos.webrtc.R;
import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;

public class SurfaceViewRecycleradapter extends RecyclerView.Adapter<SurfaceViewRecycleradapter.SurfaceViewRecyclerViewHolder> {
    private List<MediaStream> mediaStreamList = new ArrayList<>();
    private Context mContext;

    @NonNull
    @Override
    public SurfaceViewRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SurfaceViewRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.new_view,parent,false));
    }
    @Override
    public void onBindViewHolder(@NonNull SurfaceViewRecyclerViewHolder holder, int position) {

        MediaStream current_stream = mediaStreamList.get(position);
    }

    @Override
    public int getItemCount() {
        return mediaStreamList.size();
    }

    public class SurfaceViewRecyclerViewHolder extends RecyclerView.ViewHolder{

        public SurfaceViewRenderer surfaceViewRenderer;
        public TextView nametext;
        public SurfaceViewRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            surfaceViewRenderer=itemView.findViewById(R.id.surfacenew);
            nametext=itemView.findViewById(R.id.namepeer);
        }
    }
}
