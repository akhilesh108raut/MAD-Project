package com.example.myapplication.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.PDSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PDSessionAdapter extends RecyclerView.Adapter<PDSessionAdapter.ViewHolder> {

    private List<PDSession> sessions;
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(PDSession session);
        void onExportClick(PDSession session);
    }

    public PDSessionAdapter(List<PDSession> sessions, OnSessionClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pd_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PDSession session = sessions.get(position);
        holder.tvTaskType.setText(session.taskType);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.tvTimestamp.setText(sdf.format(new Date(session.timestamp)));
        
        holder.itemView.setOnClickListener(v -> listener.onSessionClick(session));
        holder.btnExport.setOnClickListener(v -> listener.onExportClick(session));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskType, tvTimestamp;
        View btnExport;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskType = itemView.findViewById(R.id.tvTaskType);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnExport = itemView.findViewById(R.id.btnExport);
        }
    }
}
