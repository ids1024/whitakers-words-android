package com.ids1024.whitakerswords

import java.util.ArrayList
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder

class SearchAdapter(results: ArrayList<SpannableStringBuilder>) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    var results = results

    override fun getItemCount(): Int {
        return results.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text_view.text = results.get(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result, null)
        return ViewHolder(view)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text_view: TextView = view.findViewById(R.id.result_text)
    }
}
