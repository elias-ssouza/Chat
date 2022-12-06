package com.example.chat.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chat.R
import com.example.chat.databinding.DeleteLayoutBinding
import com.example.chat.databinding.SendMsgBinding
import com.example.chat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessagesAdapter (
    var context: Context,
    messages: ArrayList<Message>?,
    senderRoom: String,
    receiverRoom: String
        ): RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    lateinit var messages: ArrayList<Message>
    val item_sent = 1
    val item_receive = 2
    val senderRoom: String
    var receiverRoom: String

    inner class SentMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    init {
        if (messages != null) {
            this.messages = messages
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == item_sent) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SentMsgHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messages = messages[position]
        return if (FirebaseAuth.getInstance().uid == messages.senderId) {
            item_sent
        } else {
            item_receive
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val message = messages[position]
        if (holder.javaClass == SentMsgHolder::class.java) {
            val viewHolder = holder as SentMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.ivImage.visibility = View.VISIBLE
                viewHolder.binding.tvMessage.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(com.google.android.gms.base.R.drawable.common_full_open_on_phone)
                    .into(viewHolder.binding.ivImage)
            }
            viewHolder.binding.tvMessage.text = message.message
            viewHolder.itemView.setOnLongClickListener {

                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.tvDeleteEveryone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(message)
                    }
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.tvDelete4Me.setOnClickListener {
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.tvCancel.setOnClickListener { dialog.dismiss() }
                dialog.show()
                false
            }
        } else {
            val viewHolder = holder as ReceiveMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.ivImage.visibility = View.VISIBLE
                viewHolder.binding.tvMessage.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(com.google.android.gms.base.R.drawable.common_full_open_on_phone)
                    .into(viewHolder.binding.ivImage)
            }
            viewHolder.binding.tvMessage.text = message.message
            viewHolder.itemView.setOnLongClickListener {

                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.tvDeleteEveryone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(message)
                    }
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.tvDelete4Me.setOnClickListener {
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.tvCancel.setOnClickListener { dialog.dismiss() }
                dialog.show()
                false
            }
        }

    }
    override fun getItemCount(): Int = messages.size
}