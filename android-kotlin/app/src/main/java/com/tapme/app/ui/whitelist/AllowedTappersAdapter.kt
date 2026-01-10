package com.tapme.app.ui.whitelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tapme.app.R
import com.tapme.app.data.remote.AllowedTapper
import com.tapme.app.utils.DateFormatter
import java.text.SimpleDateFormat
import java.util.*

class AllowedTappersAdapter(
    private val onRemoveClick: (String) -> Unit
) : ListAdapter<AllowedTapper, AllowedTappersAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_allowed_tapper, parent, false)
        return ViewHolder(view, onRemoveClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onRemoveClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tapperCodeText: TextView = itemView.findViewById(R.id.tapperCodeText)
        private val tapperCodeSubtext: TextView = itemView.findViewById(R.id.tapperCodeSubtext)
        private val addedDateText: TextView = itemView.findViewById(R.id.addedDateText)
        private val btnRemove = itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRemove)

        fun bind(tapper: AllowedTapper) {
            // Show nickname if available, otherwise show code
            if (tapper.nickname != null && tapper.nickname.isNotEmpty()) {
                tapperCodeText.text = tapper.nickname
                tapperCodeSubtext.text = tapper.tapperUserCode
                tapperCodeSubtext.visibility = View.VISIBLE
            } else {
                tapperCodeText.text = tapper.tapperUserCode
                tapperCodeSubtext.visibility = View.GONE
            }

            // Format added date
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                val date = dateFormat.parse(tapper.addedAt)
                date?.let {
                    addedDateText.text = "Added ${DateFormatter.formatTimeAgo(it)}"
                } ?: run {
                    addedDateText.text = ""
                }
            } catch (e: Exception) {
                addedDateText.text = ""
            }

            btnRemove.setOnClickListener {
                onRemoveClick(tapper.id)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AllowedTapper>() {
        override fun areItemsTheSame(oldItem: AllowedTapper, newItem: AllowedTapper): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AllowedTapper, newItem: AllowedTapper): Boolean {
            return oldItem == newItem
        }
    }
}
