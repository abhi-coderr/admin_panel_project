package com.example.adminapp.ui.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.adminapp.databinding.ActivityTestimonySelectorBinding
import com.example.adminapp.databinding.BottomSheetExoplayerBinding
import com.example.adminapp.network.model.intent.TestimonyUrl
import com.example.adminapp.utils.Const.INTENT_TESTIMONY_URL
import com.example.adminapp.utils.Const.VIDEO_PICK_CAMERA_CODE
import com.example.adminapp.utils.Const.VIDEO_PICK_GALLERY_CODE
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer


class TestimonySelectorActivity : AppCompatActivity() {

    private lateinit var activityBinding: ActivityTestimonySelectorBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var bottomSheetExoplayer: BottomSheetExoplayerBinding
    private var isReadPermissionGranted = false
    private var isWritePermissionGranted = false
    private var isCameraPermissionGranted = false
    private var videoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityTestimonySelectorBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

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

        setUpBinding()

    }

    private fun setUpBinding() = activityBinding.apply {

        videoSelectorBtn.setOnClickListener {
            selectTestimony()
        }

        watchTestimony.setOnClickListener {
            if (videoUri != null) {
                setVideoToExoPlayer()
            } else {
                Toast.makeText(
                    this@TestimonySelectorActivity,
                    "Kindly select testimony",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        nextPage.setOnClickListener {
            if (videoUri != null) {
                //Navigating for verifying OTP
                startActivity(Intent(this@TestimonySelectorActivity, UploadTestimonyActivity::class.java).apply {
                    putExtra(
                        INTENT_TESTIMONY_URL,
                        TestimonyUrl(
                            videoUri.toString()
                        )
                    )
                })
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

    private fun selectTestimony() {
        if (isWritePermissionGranted && isReadPermissionGranted && isCameraPermissionGranted) {
            val options = arrayOf("Gallery", "Camera")
            // alert dialog
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Choose Medium").setItems(options) { _, i ->
                if (i == 0) {
                    //gallery
                    //video pick intent gallery
                    val intent = Intent()
                    intent.type = "video/*"
                    intent.action = Intent.ACTION_GET_CONTENT

                    startActivityForResult(
                        Intent.createChooser(intent, "Choose Testimony"),
                        VIDEO_PICK_GALLERY_CODE
                    )
                } else {
                    //camera
                    val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    startActivityForResult(intent, VIDEO_PICK_GALLERY_CODE)
                }
            }
            dialog.show()
        }
    }

    private fun setVideoToExoPlayer() {
        val filterUserDialog = Dialog(this)
        bottomSheetExoplayer = BottomSheetExoplayerBinding.inflate(layoutInflater)

        filterUserDialog.setContentView(bottomSheetExoplayer.root)
        filterUserDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        filterUserDialog.setCancelable(true)
        filterUserDialog.window?.attributes?.windowAnimations =
            com.example.adminapp.R.style.MyTheme
        filterUserDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window: Window? = filterUserDialog.window
        val wlp: WindowManager.LayoutParams? = window?.attributes
        wlp?.gravity = Gravity.BOTTOM
        window?.attributes = wlp

        val exoPlayer = SimpleExoPlayer.Builder(this).build()
        bottomSheetExoplayer.videoPlayer.player = exoPlayer
        val mediaItem = videoUri?.let { MediaItem.fromUri(it) }
        mediaItem?.let {
            exoPlayer.setMediaItem(it)
        }
        exoPlayer.prepare()
        exoPlayer.pause()
        filterUserDialog.setOnCancelListener {
            exoPlayer.release()
        }
        filterUserDialog.show()
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
                Toast.makeText(this, "Watch it", Toast.LENGTH_SHORT)
                    .show()

            } else if (requestCode == VIDEO_PICK_GALLERY_CODE) {
                // pick from gallery
                videoUri = data?.data
                Toast.makeText(this, "Watch it", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            // cancel picking video
            Toast.makeText(this, "Cancelled Testimony", Toast.LENGTH_SHORT).show()
        }
    }

}