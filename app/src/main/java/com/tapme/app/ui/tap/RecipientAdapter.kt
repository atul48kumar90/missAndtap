package com.tapme.app.ui.tap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.tapme.app.R
import com.tapme.app.data.remote.AllowedTapper

class RecipientAdapter(
    private val recipients: List<AllowedTapper>,
    private val selectedRecipient: AllowedTapper?,
    private val onRecipientSelected: (AllowedTapper) -> Unit
) : RecyclerView.Adapter<RecipientAdapter.RecipientViewHolder>() {

    class RecipientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.recipientCard)
        val codeText: TextView = itemView.findViewById(R.id.recipientCodeText)
        val subtextText: TextView? = itemView.findViewById(R.id.recipientCodeSubtext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipient, parent, false)
        return RecipientViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipientViewHolder, position: Int) {
        val recipient = recipients[position]
        
        // Show nickname if available, otherwise show code
        if (recipient.nickname != null && recipient.nickname.isNotEmpty()) {
            holder.codeText.text = recipient.nickname
            holder.subtextText?.text = recipient.tapperUserCode
            holder.subtextText?.visibility = View.VISIBLE
        } else {
            holder.codeText.text = recipient.tapperUserCode
            holder.subtextText?.visibility = View.GONE
        }
        
        // Highlight selected recipient
        val isSelected = selectedRecipient?.id == recipient.id
        holder.card.isSelected = isSelected
        holder.card.strokeWidth = if (isSelected) 3 else 0
        holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.primary_red)
        
        holder.card.setOnClickListener {
            onRecipientSelected(recipient)
        }
    }

    override fun getItemCount() = recipients.size
}
