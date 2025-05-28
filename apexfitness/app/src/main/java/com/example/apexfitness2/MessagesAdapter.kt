package com.example.apexfitness2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MessagesAdapter(private val messagesList: List<Message>) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messagesList[position]
        holder.bind(message, message.senderId == currentUserId)
    }

    override fun getItemCount(): Int = messagesList.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(message: Message, isCurrentUser: Boolean) {
            messageTextView.text = message.message
            // Simple UI: align right if current user, left otherwise
            val params = messageTextView.layoutParams as ViewGroup.MarginLayoutParams
            if (isCurrentUser) {
                messageTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                params.marginStart = 100
                params.marginEnd = 0
            } else {
                messageTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                params.marginStart = 0
                params.marginEnd = 100
            }
            messageTextView.layoutParams = params
        }
    }
}
