package com.hwtm.slack

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button.setOnClickListener {
            register()
        }

        val intent = Intent(this, LoginActivity::class.java)
        text_register.setOnClickListener {
            startActivity(intent)
        }

        /**Bottone selezione foto**/
        selectPhoto.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhotoUri: Uri?= null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri= data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)

            selectPhoto_imageView.setImageBitmap(bitmap)
            selectPhoto.alpha=0f
        }
    }

    private fun register(){
        val email = email_register.text.toString()
        val username = username_register.text.toString()
        val password = password_register.text.toString()

        /**Controlli**/
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Please enter email or password", Toast.LENGTH_SHORT).show()
            return
        }

        if(password.length<6){
            Toast.makeText(this,"Password too short", Toast.LENGTH_SHORT).show()
            return
        }

        /**Ricerca nel database se esistono l'email e password
         * Se ha successo, salva sia l'immagine che l'utente
         * Se non ha successo, scrive un messaggio**/
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                if(!it.isSuccessful) return@addOnCompleteListener

                uploadImage()
            }
            .addOnFailureListener{
                Toast.makeText(this,"${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImage(){
        val email = email_register.text.toString()
        val username = username_register.text.toString()
        val password = password_register.text.toString()

        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        /**Inserisce nel database la foto**/
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Toast.makeText(this,"Successfully uploaded image", Toast.LENGTH_SHORT).show()
            ref.downloadUrl.addOnSuccessListener {
                saveUser(email, username, password,it.toString())
            }
        }
    }

    private fun saveUser(email:String, username:String, password:String, profileImage: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid,username,email,password,profileImage)

        /**Salvataggio dell'utente nel database**/
        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this,"User created", Toast.LENGTH_SHORT).show()

                /**Effettuata la registrazione, passa alla UsersActivity**/
                val intent = Intent(this, UsersActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
            }
    }
}

/**Classe user per la registrazione**/
@Parcelize
class User(val uid:String, val username:String, val email:String, val password:String,
           val profileImage:String): Parcelable {
    constructor():this("","","","","")
}