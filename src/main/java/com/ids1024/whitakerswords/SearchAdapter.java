package com.ids1024.whitakerswords;

import java.util.ArrayList;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private ArrayList<SpannableStringBuilder> results;

    SearchAdapter(ArrayList<SpannableStringBuilder> results) {
        this.results = results;
    }

    @Override
    public int getItemCount() {
        return results.size();
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.text_view.setText(results.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result, null);
        return new ViewHolder(view);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView text_view;

        public ViewHolder(View view) {
            super(view);
            this.text_view = (TextView)view.findViewById(R.id.result_text);
        }
    }
}
