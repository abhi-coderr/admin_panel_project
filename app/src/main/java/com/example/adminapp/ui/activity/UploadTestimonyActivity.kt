package com.example.adminapp.ui.activity


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.adminapp.databinding.ActivityUploadTestimonyBinding
import com.example.adminapp.network.model.Testimony
import com.example.adminapp.network.model.intent.TestimonyUrl
import com.example.adminapp.utils.Const
import com.example.adminapp.utils.parcelable
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.File


class UploadTestimonyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadTestimonyBinding

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private var testimonyDetailText = String()
    private var testimonyCategoryText = String()
    private var videoTitle = String()

    lateinit var uploadTask: StorageTask<UploadTask.TaskSnapshot>

    private var isReadPermissionGranted = false
    private var isWritePermissionGranted = false
    private var isCameraPermissionGranted = false

    private val testimonyCategoryList = arrayListOf<String>(
        "Select Category",
        "First Category",
        "Second Category",
        "Third Category",
        "Fourth Category",
        "Fifth Category",
        "Sixth Category",
        "Seventh Category",
        "Eighth Category",
        "UP to The 30"
    )

    private lateinit var testimonyUrl: TestimonyUrl

    private lateinit var storageRef: StorageReference
    private lateinit var referenceVideo: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadTestimonyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
            { permissions ->
                isReadPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                    ?: isReadPermissionGranted

                isWritePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                    ?: isWritePermissionGranted

                isCameraPermissionGranted = permissions[Manifest.permission.CAMERA]
                    ?: isCameraPermissionGranted
            }

        storageRef = FirebaseStorage.getInstance().reference.child("videos")
        referenceVideo = FirebaseDatabase.getInstance().reference.child("videos")

        requestPermission()

        getDataFromIntent()

        setUpBinding()

        firebaseSetting()
    }

    @SuppressLint("Recycle")
    private fun firebaseSetting() {
        if (testimonyUrl.testimonyUrl.isNotEmpty()) {
            var path: String? = null
            val cursor: Cursor
            val projection = arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Thumbnails.DATA
            )
            val orderedBy = MediaStore.Video.Media.DEFAULT_SORT_ORDER
            cursor = this@UploadTestimonyActivity.contentResolver?.query(
                testimonyUrl.testimonyUrl.toUri(), projection, null,
                null, orderedBy
            )!!

            val columnIndexData: Int = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            while (cursor.moveToNext()) {
                path = cursor.getString(columnIndexData)//todo change 1st
                val uri: Uri = testimonyUrl.testimonyUrl.toUri()
                val file = uri.path?.let { File(it) }
                videoTitle = file?.name.toString()
            }

            binding.fileName.text = videoTitle

        }
    }

    private fun uploadFileToFirebase() {
        if (binding.testimonyDetail.text.isNotEmpty()
            && testimonyCategoryText != "Select Category"
        ) {

            if (testimonyUrl.testimonyUrl.isNotEmpty()) {
                val mStorageRef = storageRef.child(videoTitle)
                uploadTask = mStorageRef.putFile(testimonyUrl.testimonyUrl.toUri())
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->

                            uri?.let {
                                val testimony = Testimony(
                                    testimonyName = binding.testimonyDetail.text.toString(),
                                    testimonyCategory = testimonyCategoryText,
                                    testimonyUrl = uri.toString()
                                )

                                val uploadsId = referenceVideo.push().key
                                uploadsId?.let {
                                    referenceVideo.child(uploadTask.toString()).setValue(testimony)
                                }
                            }
                        }
                    }
                    .addOnProgressListener {
                        val progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                        binding.progressBarUpload.progress = progress.toInt()
                    }
            }

        } else {
            Toast.makeText(this, "Kindly give name and category", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDataFromIntent() {
        intent?.let {
            testimonyUrl = it.parcelable(Const.INTENT_TESTIMONY_URL)!!
            Toast.makeText(this, testimonyUrl.testimonyUrl, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpBinding() {

        setCategory()

        binding.videoUploadBtn.setOnClickListener {
            uploadFileToFirebase()
        }

        binding.pauseBtn.setOnClickListener {
            Toast.makeText(this, "pause", Toast.LENGTH_SHORT).show()
        }

        binding.cancelBtn.setOnClickListener {
            Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setCategory() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, testimonyCategoryList.toList()
        )
        binding.testimonyCategory.adapter = adapter

        binding.testimonyCategory.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                Toast.makeText(
                    this@UploadTestimonyActivity,
                    testimonyCategoryList[position],
                    Toast.LENGTH_SHORT
                ).show()
                testimonyCategoryText = testimonyCategoryList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun requestPermission() {
        isReadPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWritePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isCameraPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequestList: MutableList<String> = ArrayList()

        if (!isReadPermissionGranted) {
            permissionRequestList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!isWritePermissionGranted) {
            permissionRequestList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!isCameraPermissionGranted) {
            permissionRequestList.add(Manifest.permission.CAMERA)
        }

        if (permissionRequestList.isNotEmpty()) {
            permissionLauncher.launch(permissionRequestList.toTypedArray())
        }

    }

}