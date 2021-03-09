package com.example.learn_akses_image

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.learn_akses_image.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val REQUEST_CODE = 100
    private lateinit var binding: ActivityMainBinding
    lateinit var photoPath :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            checkPermision(android.Manifest.permission.CAMERA, "camera", REQUEST_CODE)
            takePictureButton.setOnClickListener {
                takePictureIntent()
            }
        }
    }

    private fun takePictureIntent(){
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePicture.resolveActivity(packageManager)?.also {
            val photoFile : File? = try {
                createImageFile()
            } catch ( e : IOException ){
                null
            }
            photoFile?.also {
                val photoURI : Uri = FileProvider.getUriForFile(
                    this, "${BuildConfig.APPLICATION_ID}.fileprovider", it
                )
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePicture, REQUEST_CODE)
            }
        }
    }

    //buat nampilin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageBitmap = BitmapFactory.decodeFile(photoPath)
        binding.photoImageView.setImageBitmap(imageBitmap)
    }

    //membuat file
    private fun createImageFile() : File {
        val timeStramp : String = SimpleDateFormat("yyyyMMdd HHmmss").format(Date())
        val imageDir = filesDir
        val storageDir = File(imageDir, "photo ${timeStramp}.jpg")
        return storageDir.apply {
            photoPath = absolutePath
        }
    }


    //Access permission otomatis
    private fun checkPermision(permision: String, name: String, requestCode: Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            when{
                ContextCompat.checkSelfPermission(applicationContext, permision) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(applicationContext, "$name permision granted", Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(permision) -> showDialog(permision, name, requestCode)

                else -> ActivityCompat.requestPermissions(this, arrayOf(permision), requestCode)
            }
        }
    }

    private fun showDialog(permision: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("permission to your $name is required to use this app")
            setMessage("Permission required")
            setPositiveButton("OK"){
                dialog, which -> ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permision), requestCode)
            }
            val dialog = builder.create()
            dialog.show()
        }
    }
}