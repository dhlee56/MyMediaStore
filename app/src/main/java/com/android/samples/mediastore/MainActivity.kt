package com.android.samples.mediastore

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.samples.mediastore.ui.theme.MyMediaStoreTheme
import kotlin.getValue
private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045
private const val DELETE_PERMISSION_REQUEST = 0x1033

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMediaStoreTheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(text = stringResource(id = R.string.app_name)) }
                        )
                    }
                ) { innerPadding ->
                    val welcomeView by viewModel.welcomeView.observeAsState(true)
                    val permissionRationaleView by viewModel.permissionRationaleView.observeAsState(false)
                    val images by viewModel.images.observeAsState()
                    viewModel.permissionNeededForDelete.observe(this, Observer { intentSender ->
                        intentSender?.let {
                            // On Android 10+, if the app doesn't have permission to modify
                            // or delete an item, it returns an `IntentSender` that we can
                            // use here to prompt the user to grant permission to delete (or modify)
                            // the image.
                            startIntentSenderForResult(
                                intentSender,
                                DELETE_PERMISSION_REQUEST,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        }
                    })

                    Box(Modifier.padding(innerPadding)) {
                        if(permissionRationaleView!!) {
                            //PermissionRationaleScreen()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = R.drawable.ic_phonelink_lock_black_24dp
                                    ),
                                    modifier = Modifier
                                        .height(128.dp)
                                        .width(128.dp),
                                    contentDescription = null
                                )
                                Text(text = stringResource(R.string.permission_not_granted))
                                Button(
                                    onClick = {
                                        openMediaStore()
                                    },
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("GRANT PERMISSION")
                                }
                            }
                            //viewModel.setPermissionRationaleView(false)
                        }
                         else if(welcomeView!!) {
                            //WelcomeScreen()
                            //viewModel.setWelcomeView(false)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = R.drawable.ic_photo_album_black_24dp),
                                    modifier = Modifier
                                        .height(128.dp)
                                        .width(128.dp),
                                    contentDescription = null
                                )
                                Button(
                                    onClick = {
                                        openMediaStore()
                                    },
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("OPEN ALBUM")
                                }
                            }
                        }
                        else {
                            GalleryScreen()
                        }

//                        if (!haveStoragePermission()) {
//                            viewModel.setWelcomeView(true)
//                            viewModel.setPermissionRationaleView(true)
//                        }

                    }
                }
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                println("KOTLINCLASS: onRequestPermissionsResult ${grantResults.contentToString()}")
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    showImages()
                } else {
                    // If we weren't granted the permission, check to see if we should show
                    // rationale for the permission.
                    val showRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                        Manifest.permission.READ_MEDIA_IMAGES
                                       else Manifest.permission.READ_EXTERNAL_STORAGE
                                    //Manifest.permission.READ_EXTERNAL_STORAGE
                        )

                    /**
                     * If we should show the rationale for requesting storage permission, then
                     * we'll show [ActivityMainBinding.permissionRationaleView] which does this.
                     *
                     * If `showRationale` is false, this means the user has not only denied
                     * the permission, but they've clicked "Don't ask again". In this case
                     * we send the user to the settings page for the app so they can grant
                     * the permission (Yay!) or uninstall the app.
                     */
                    println("KOTLINCLASS: onRequestPermissionsResult $showRationale")
                    if (showRationale) {
                        showNoAccess()
                    } else {
                        goToSettings()
                    }
                }
                return
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == com.android.samples.mediastore.DELETE_PERMISSION_REQUEST) {
            viewModel.deletePendingImage()
        }
    }

    private fun showImages() {
        viewModel.loadImages()
        viewModel.setWelcomeView (false)
        viewModel.setPermissionRationaleView(false)
    }

    private fun showNoAccess() {
        viewModel.setWelcomeView(false)
        viewModel.setPermissionRationaleView(true)
    }

    private fun openMediaStore() {
        if (haveStoragePermission()) {
            showImages()
        } else {
            requestPermission()
        }
    }

    private fun goToSettings() {
        Intent(ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            startActivity(intent)
        }
    }

    /**
     * Convenience method to check if [Manifest.permission.READ_EXTERNAL_STORAGE] permission
     * has been granted to the app.
     */
    private fun haveStoragePermission(): Boolean {

        val ret_val =
                ContextCompat.checkSelfPermission(
                    this,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                    Manifest.permission.READ_MEDIA_IMAGES
                               else Manifest.permission.READ_EXTERNAL_STORAGE

                            //Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED
        println("KOTLINCLASS: haveStoragePermission $ret_val")
        return ret_val
    }


    /**
     * Convenience method to request [Manifest.permission.READ_EXTERNAL_STORAGE] permission.
     */
    private fun requestPermission() {
        println("KOTLINCLASS: requestPermission")
        if (!haveStoragePermission()) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            ActivityCompat.requestPermissions(this, permissions,
                com.android.samples.mediastore.READ_EXTERNAL_STORAGE_REQUEST
            )
        }
    }

    private fun deleteImage(image: MediaStoreImage) {
//        MaterialAlertDialogBuilder(this)
//            .setTitle(R.string.delete_dialog_title)
//            .setMessage(getString(R.string.delete_dialog_message, image.displayName))
//            .setPositiveButton(R.string.delete_dialog_positive) { _: DialogInterface, _: Int ->
//                viewModel.deleteImage(image)
//            }
//            .setNegativeButton(R.string.delete_dialog_negative) { dialog: DialogInterface, _: Int ->
//                dialog.dismiss()
//            }
//            .show()
    }

    /**
     * A [ListAdapter] for [MediaStoreImage]s.
     */
}
