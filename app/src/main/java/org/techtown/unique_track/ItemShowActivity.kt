package org.techtown.unique_track

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_iteminfo.*
import org.techtown.unique_track.model.ItemData
import org.techtown.unique_track.model.User
import java.lang.Exception

class ItemShowActivity : AppCompatActivity() {
    private var auth : FirebaseAuth? = null
    private lateinit var database: DatabaseReference

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iteminfo)
        auth = Firebase.auth

        //home버튼 -> main창으로
        val home_button5 =findViewById<Button>(R.id.home_button5)
        home_button5.setOnClickListener{
            startActivity(Intent(this@ItemShowActivity,MainActivity::class.java))
        }
        //back 버튼 -> 이전 창(NFC인식창으로)
        val back_button3=findViewById<Button>(R.id.back_button3)
        back_button3.setOnClickListener{
            //startActivity(Intent(this@ItemShowActivity,NFCActivity::class.java))
            finish()

        }

        var NFCuid = getIntent().getStringExtra("NFCuid")
        var notNullableNFCuid : String = NFCuid!!

        database = FirebaseDatabase.getInstance().getReference()

        val productListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                for (data in snapshot.children) {
//                    if (data.key != "image")
//                        InformationText.append(data.key + " : " + data.getValue<String>() + "\n")
//                    if (data.key == "ownerUID")
//                        ownerUID = data.getValue<String>()
//                }
                val data : ItemData? = snapshot.getValue<ItemData>()
                val productName : String? = data?.productName
                val registerDate : String? = data?.registerDate
                val image : String? = data?.image //Get a image url
                val ownerID : String? = data?.ownerUID
                val nfcuid : String? = data?.nfcuid
                val explanation : String? = data?.explanation

                val imageView = findViewById<ImageView>(R.id.editItemImage)
                // Create a storage reference from our app
                val storageRef = FirebaseStorage.getInstance().reference
                val head_len = "https://firebasestorage.googleapis.com/v0/b/unique-track-f112a.appspot.com/o/images%2F"
                    .length
                //Get the image's short name
                var imageName = image?.substring(head_len)
                imageName = imageName?.substringBefore("?alt=media&token=ca1ee99a-a96e-4074-9bf8-49eda0d701a4")

                // Create a reference with an initial file path and name
                val pathReference = storageRef.child("images/" + imageName)

                pathReference.downloadUrl.addOnSuccessListener(object : OnSuccessListener<Uri>{
                    override fun onSuccess(p0: Uri?) {
                        Glide.with(applicationContext).load(p0).override(100, 100).centerCrop().into(imageView)
                    }
                }).addOnFailureListener(object : OnFailureListener{
                    override fun onFailure(p0: Exception) {
                        Toast.makeText(applicationContext, "실패", Toast.LENGTH_SHORT).show()
                    }
                })
                InformationText.append("ProductName: " + productName + "\n")
                InformationText.append("RegisterDate: " + registerDate + "\n")
                InformationText.append("NFCuid: " + nfcuid + "\n")
                InformationText.append("Explanation: " + explanation + "\n")
                InformationText.append("OwnerUID: " + ownerID + "\n")

                //Get owner name from ownerUID
                val ownerUID = ownerID!!

                database.child("Owners").child(ownerUID).addValueEventListener(object: ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {
                        }
                        override fun onDataChange(snapshot: DataSnapshot) {
//                            for (data in snapshot.children) {
//                                if (data.key == "username") {
//                                    val ownerName = data.value
//                                    InformationText.append("OwnerName: " + ownerName + "\n")
//                                }
//                            }
                            val user : User? = snapshot.getValue<User>()
                            val ownerName = user?.username
                            InformationText.append("OwnerName: " + ownerName + "\n")
                        }
                    })

                if(ownerUID == auth!!.uid) {
                    itemEditButton.setOnClickListener {
                        val newintent = Intent(this@ItemShowActivity, NewItemActivity::class.java)
                        newintent.putExtra("NFCcode", snapshot.child("nfcuid").getValue<String>())
                        newintent.putExtra("editTrue", true)
                        startActivity(newintent)
                        finish()
                    }
                }
                else{
                    itemEditButton.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                InformationText.setText("Error: "+error.toException())
            }
        }
        database.child("Products").child(notNullableNFCuid).addValueEventListener(productListener)
    }

}