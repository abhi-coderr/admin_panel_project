package com.example.adminapp.ui.activity


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.adminapp.databinding.ActivityMainBinding
import com.example.adminapp.utils.Const.Companion.VIDEO_PICK_CAMERA_CODE
import com.example.adminapp.utils.Const.Companion.VIDEO_PICK_GALLERY_CODE


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private var isReadPermissionGranted = false
    private var isWritePermissionGranted = false
    private var isCameraPermissionGranted = false

    private var videoUri: Uri? = null

    private val diseases =
        arrayOf("select disease", "Prostate", "Cervical", "Hip replacement", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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

        requestPermission()

        binding.videoSelectorBtn.setOnClickListener {
            selectTestimony()
        }

        binding.pauseBtn.setOnClickListener {
            Toast.makeText(this, "pause", Toast.LENGTH_SHORT).show()
        }

        binding.cancelBtn.setOnClickListener {
            Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show()
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

    private fun selectTestimony() {
        if (isWritePermissionGranted && isReadPermissionGranted && isCameraPermissionGranted) {
            val options = arrayOf("Gallery", "Camera")
            // alert dialog
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Choose Medium").setItems(options) { _, i ->
                if (i == 0) {
                    //camera
                    val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    startActivityForResult(intent, VIDEO_PICK_GALLERY_CODE)
                } else {
                    //gallery
                    //video pick intent gallery
                    val intent = Intent()
                    intent.type = "video/*"
                    intent.action = Intent.ACTION_GET_CONTENT

                    startActivityForResult(
                        Intent.createChooser(intent, "Choose Testimony"),
                        VIDEO_PICK_GALLERY_CODE
                    )
                }
            }
            dialog.show()
        }
    }

    // handle video pick result
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            //video is picked from camera or gallery
            if (requestCode == VIDEO_PICK_CAMERA_CODE) {
                // pick from camera
                videoUri = data?.data
//                setVideoToPlayer()
                binding.fileName.text = videoUri.toString()

            } else if (requestCode == VIDEO_PICK_GALLERY_CODE) {
                // pick from gallery
                videoUri = data?.data
                binding.fileName.text = videoUri.toString()
            }
        } else {
            // cancel picking video
            Toast.makeText(this, "Cancelled Testimony", Toast.LENGTH_SHORT).show()
        }

//        private fun setVideoToPlayer() {
//            //set the picked video to video view
//            // video play controls
//            val exoPlayer = SimpleExoPlayer.Builder(this).build()
//            activityUploadVideoBinding.videoPlayer.player = exoPlayer
//            val mediaItem = videoUri?.let { MediaItem.fromUri(it) }
//            mediaItem?.let {
//                exoPlayer.setMediaItem(mediaItem)
//            }
//            exoPlayer.prepare()
//            exoPlayer.pause()
//        }

    }

}