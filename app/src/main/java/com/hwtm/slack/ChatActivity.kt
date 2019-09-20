package com.hwtm.slack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.hwtm.slack.UsersActivity.Companion.currentUser
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*


class ChatActivity : AppCompatActivity() {
    /**Variabili globali**/
    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        /**Riceve la variabile passata da UsersActivity**/
        toUser = intent.getParcelableExtra(UsersActivity.USER_KEY)

        supportActionBar?.title="Chatting with ${toUser?.username}"

        receiveMessage()

        sendButton.setOnClickListener {
            sendMessage()
        }

        recyclerviewChat.adapter = adapter
    }

    private fun receiveMessage(){
        /**Variabili per il messaggio**/
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        /**Riceve dal database le informazioni del messaggio e utilizza la funzione per
         * inserirli nel layout**/
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val chatMessage = p0.getValue(ChatMessage::class.java)

                if(chatMessage != null){

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(chatMessage.text, currentUser!!))
                    }else{
                        adapter.add(ChatToItem(chatMessage.text,toUser!!))
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    private fun sendMessage(){
        /**Variabili per il messaggio**/
        val text = enterMessage.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid

        if (fromId == null) return

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(ref.key!!,text,fromId,toId!!)

        /**Inserisce le informazioni nel database**/
        ref.setValue(chatMessage).addOnSuccessListener {
            Toast.makeText(this,"Message inviated correctly",Toast.LENGTH_SHORT).show()
            enterMessage.text.clear()
            recyclerviewChat.scrollToPosition(adapter.itemCount - 1)
        }

        toRef.setValue(chatMessage)
    }
}

/**Classe ChatMessage che contiene le informazioni del messaggio**/
class ChatMessage(val id:String, val text:String, val fromId:String, val toId:String){
    constructor() : this("","","","")
}

/**Adattamento al layout**/
class ChatFromItem(val msg:String, val user:User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.messageFromChat.text = msg

        Picasso.get().load(user.profileImage).into(viewHolder.itemView.imageMessageFrom)
    }

    override fun getLayout():Int{
        return R.layout.chat_from_row
    }
}

class ChatToItem(val msg:String, val user:User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.messageToChat.text = msg

        Picasso.get().load(user.profileImage).into(viewHolder.itemView.imageMessageTo)
    }

    override fun getLayout():Int{
        return R.layout.chat_to_row
    }
}