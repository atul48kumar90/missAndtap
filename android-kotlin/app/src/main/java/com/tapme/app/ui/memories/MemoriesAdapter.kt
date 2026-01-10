package com.tapme.app.ui.memories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.tapme.app.R
import com.tapme.app.data.remote.TapHistoryItem
import com.tapme.app.domain.models.TapType
import com.tapme.app.utils.DateFormatter
import java.text.SimpleDateFormat
import java.util.*

class MemoriesAdapter(
    private val onTapClick: (TapHistoryItem) -> Unit
) : ListAdapter<TapHistoryItem, RecyclerView.ViewHolder>(MemoryDiffCallback()) {

    private var isLoadingMore = false
    private var hasMore = true

    fun setLoadingMore(loading: Boolean) {
        val wasLoading = isLoadingMore
        isLoadingMore = loading
        
        val dataItemCount = super.getItemCount()
        if (wasLoading != loading) {
            if (loading && dataItemCount > 0) {
                notifyItemInserted(dataItemCount)
            } else if (!loading && hasMore && dataItemCount > 0) {
                notifyItemChanged(dataItemCount)
            } else if (!loading && !hasMore && dataItemCount > 0) {
                notifyItemRemoved(dataItemCount)
            }
        } else if (loading && dataItemCount > 0) {
            notifyItemChanged(dataItemCount)
        }
    }

    fun setHasMore(hasMoreItems: Boolean) {
        val hadMore = hasMore
        hasMore = hasMoreItems
        
        val dataItemCount = super.getItemCount()
        if (hadMore != hasMoreItems && dataItemCount > 0) {
            if (!hasMoreItems && isLoadingMore) {
                notifyItemRemoved(dataItemCount)
            } else if (hasMoreItems && !isLoadingMore) {
                notifyItemInserted(dataItemCount)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val dataItemCount = super.getItemCount()
        return if (position >= dataItemCount) {
            1 // Footer view type
        } else {
            0 // Memory item view type
        }
    }

    override fun getItemCount(): Int {
        val dataItemCount = super.getItemCount()
        return dataItemCount + if (dataItemCount > 0 && (isLoadingMore || hasMore)) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            // Footer view for loading more
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading_footer, parent, false)
            LoadingFooterViewHolder(view)
        } else {
            // Memory item view
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_memory, parent, false)
            MemoryViewHolder(view, onTapClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataItemCount = super.getItemCount()
        if (holder is MemoryViewHolder) {
            if (position < dataItemCount) {
                holder.bind(getItem(position))
            }
        } else if (holder is LoadingFooterViewHolder) {
            holder.bind(isLoadingMore)
        }
    }

    class LoadingFooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(isLoading: Boolean) {
            val progressBar = itemView.findViewById<android.widget.ProgressBar>(R.id.loadingProgressBar)
            val textView = itemView.findViewById<TextView>(R.id.loadingText)
            
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                textView.text = "Loading more..."
            } else {
                progressBar.visibility = View.GONE
                textView.text = "Scroll for more"
            }
        }
    }

    class MemoryViewHolder(
        itemView: View,
        private val onTapClick: (TapHistoryItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.memoryCard)
        private val emojiText: TextView = itemView.findViewById(R.id.memoryEmoji)
        private val messageText: TextView = itemView.findViewById(R.id.memoryMessage)
        private val timeText: TextView = itemView.findViewById(R.id.memoryTime)
        private val directionText: TextView = itemView.findViewById(R.id.memoryDirection)

        fun bind(tap: TapHistoryItem) {
            // Set emoji
            val emoji = if (!tap.customEmoji.isNullOrBlank()) {
                tap.customEmoji
            } else {
                tap.tapType?.let { TapType.fromString(it)?.emoji } ?: "❤️"
            }
            emojiText.text = emoji

            // Set message
            if (!tap.message.isNullOrBlank()) {
                messageText.text = tap.message
                messageText.visibility = View.VISIBLE
            } else {
                messageText.visibility = View.GONE
            }

            // Set time - handle multiple timestamp formats
            try {
                val date = parseTimestamp(tap.timestamp)
                if (date != null) {
                    timeText.text = DateFormatter.formatTimeAgo(date)
                } else {
                    // Fallback: show raw timestamp or "unknown"
                    android.util.Log.w("MemoriesAdapter", "Failed to parse timestamp: ${tap.timestamp}")
                    timeText.text = "Recently"
                }
            } catch (e: Exception) {
                android.util.Log.e("MemoriesAdapter", "Error parsing timestamp: ${tap.timestamp}", e)
                timeText.text = "Recently"
            }

            // Set direction (sent/received)
            directionText.text = if (tap.isSent) {
                "Sent"
            } else {
                "Received"
            }

            // Set background color based on tap type
            val backgroundColor = if (!tap.customEmoji.isNullOrBlank()) {
                // Default color for custom emoji
                ContextCompat.getColor(itemView.context, R.color.bg_card)
            } else {
                tap.tapType?.let { TapType.fromString(it)?.backgroundColor }
                    ?: ContextCompat.getColor(itemView.context, R.color.bg_card)
            }
            card.setCardBackgroundColor(backgroundColor)

            // Make text readable on colored background
            val textColor = if (isLightColor(backgroundColor)) {
                ContextCompat.getColor(itemView.context, R.color.text_primary)
            } else {
                ContextCompat.getColor(itemView.context, android.R.color.white)
            }
            messageText.setTextColor(textColor)
            timeText.setTextColor(textColor)
            directionText.setTextColor(textColor)

            card.setOnClickListener {
                onTapClick(tap)
            }
        }

        private fun parseTimestamp(timestamp: String): Date? {
            if (timestamp.isBlank()) return null
            
            // Try different timestamp formats
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss"
            )
            
            for (format in formats) {
                try {
                    val dateFormat = SimpleDateFormat(format, Locale.US)
                    if (format.contains("Z") || format.contains("XXX")) {
                        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val date = dateFormat.parse(timestamp)
                    if (date != null) {
                        return date
                    }
                } catch (e: Exception) {
                    // Try next format
                    continue
                }
            }
            
            return null
        }

        private fun isLightColor(color: Int): Boolean {
            val red = android.graphics.Color.red(color)
            val green = android.graphics.Color.green(color)
            val blue = android.graphics.Color.blue(color)
            val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0
            return luminance > 0.5
        }
    }

    class MemoryDiffCallback : DiffUtil.ItemCallback<TapHistoryItem>() {
        override fun areItemsTheSame(oldItem: TapHistoryItem, newItem: TapHistoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TapHistoryItem, newItem: TapHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
