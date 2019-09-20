package com.hwtm.slack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_button.setOnClickListener {
            login()
        }
        text_login.setOnClickListener {
            finish()
        }
    }

    private fun login() {
        val email = email_login.text.toString()
        val password = password_login.text.toString()

        /**Manda un messaggio se l'email o la password è vuota**/
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email or password", Toast.LENGTH_SHORT).show()
            return
        }

        /**La password deve essere maggiore di sei caratteri**/
        if (password.length < 6) {
            Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show()
            return
        }

        /**Ricerca nel database se esistono l'email e password
         * Se ha successo, passa alla UsersActivity
         * Se non ha successo, scrive un messaggio**/
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                val intent = Intent(this, UsersActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener {
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}