package ru.geekbrains.a2l7_sms_homework;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewChatAdapter extends RecyclerView.Adapter<RecyclerViewChatAdapter.ViewHolder> {

    private static final String TAG = "33333";
    private ArrayList<String> data;

    RecyclerViewChatAdapter(){
        Log.d(TAG, "RecyclerViewChatAdapter RecyclerViewChatAdapter");
        data = new ArrayList<>();
    }

    void addItem(String message) {
        Log.d(TAG, "RecyclerViewChatAdapter addItem message = " + message );
        data.add(message);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerViewChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
      View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        Log.d(TAG, "RecyclerViewChatAdapter onCreateViewHolder");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewChatAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "RecyclerViewChatAdapter onBindViewHolder");
        holder.textView.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "RecyclerViewChatAdapter getItemCount");
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "RecyclerViewChatAdapter  class ViewHolder  ViewHolder");
            textView = itemView.findViewById(R.id.textViewChat);
        }
    }
}
