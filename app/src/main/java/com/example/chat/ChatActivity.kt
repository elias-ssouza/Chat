package com.example.chat

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chat.adapter.MessagesAdapter
import com.example.chat.databinding.ActivityChatBinding
import com.example.chat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class ChatActivity : AppCompatActivity() {

    var binding: ActivityChatBinding? = null
    var adapter: MessagesAdapter? = null
    var messages: ArrayList<Message>?= null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    var receiverUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Carregando imagem")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding!!.tvUsername01.text = name
        Glide.with(this@ChatActivity).load(profile)
            .placeholder(com.google.android.gms.base.R.drawable.common_full_open_on_phone)
            .into(binding!!.ivProfile01)
        binding!!.ivSend.setOnClickListener{ finish()}
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val status = snapshot.getValue(String::class.java)
                        if (status == "offline"){
                            binding!!.tvStatus.visibility = View.GONE
                        }
                        else{
                            binding!!.tvStatus.setText(status)
                            binding!!.tvStatus.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adapter = MessagesAdapter(this@ChatActivity, messages, senderRoom!!, receiverRoom!!)

        binding!!.rvChat.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding!!.rvChat.adapter = adapter
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("message")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val status = snapshot.getValue(String::class.java)
                        if (status == "offline"){
                            binding!!.tvStatus.visibility = View.GONE
                        }
                        else{
                            binding!!.tvStatus.setText(status)
                            binding!!.tvStatus.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adapter = MessagesAdapter(this@ChatActivity, messages, senderRoom!!, receiverRoom!!)

        binding!!.rvChat.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding!!.rvChat.adapter = adapter
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("message")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapshot1 in snapshot.children){
                        val message: Message? = snapshot1.getValue(Message::class.java)
                        message!!.messageId = snapshot1.key
                        messages!!.add(message)
                    }
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        binding!!.ivSend.setOnClickListener{
            val messageTxt: String = binding!!.etMessage.text.toString()
            val date = Date()
            val message = Message (messageTxt, senderUid,date.time)

            binding!!.etMessage.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time

            database!!.reference.child("chats").child(senderRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("message")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {  }
                }
        }
    }
}