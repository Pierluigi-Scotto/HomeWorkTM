package com.hwtm.slack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_users.*
import kotlinx.android.synthetic.main.user_row.view.*

class UsersActivity : AppCompatActivity() {
    companion object{
        var currentUser: User? = null
        val USER_KEY = "USER_KEY"
    }

    val uid = FirebaseAuth.getInstance().uid
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        verifyUserIsLoggedIn()

        supportActionBar?.title = "Select user to start chat"

        recyclerView_users.adapter = adapter

        fetchCurrentUser()

        fetchUsers()
    }

    /**Preleva dal database l'utente che ha effettuato l'accesso**/
    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                helloUser.text="Welcome back ${currentUser?.username}!"
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun verifyUserIsLoggedIn() {
        /**Controlla se è già stato eseguito l'accesso
         * Se è già stato effettuato un accesso, esegue UsersActivity
         * Se non è stato effettuato un accesso, esegue RegisterActivity**/
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    /**Preleva tutti gli utenti che sono sul database**/
    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        if(user.username != currentUser?.username)
                            adapter.add(UserItem(user))
                    }
                }

                /**Invia a ChatActivity le informazioni del messaggio: l'oggetto UserItem**/
                adapter.setOnItemClickListener { item, view ->

                    val userItem = item as UserItem

                    val intent = Intent(view.context, ChatActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                }

                recyclerView_users.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    /**Iniazializzazione del layout menu**/
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            /**Se si preme sign out, si esce**/
            R.id.menu_sign_out->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**Creazione layout menu**/
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}

/**Adattamento al layout**/
class UserItem(val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.usernameTextView.text = user.username

        Picasso.get().load(user.profileImage).into(viewHolder.itemView.imageUser)
    }

    override fun getLayout():Int{
        return R.layout.user_row
    }
}