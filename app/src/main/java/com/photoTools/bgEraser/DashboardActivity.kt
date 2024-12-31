package com.photoTools.bgEraser

import Utilities
import Utilities.deleteAllFiles
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.firebase.FirebaseApp
import com.homeworkoutmate.dailyfitnessapp.gymexercise.utils.AdsManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.photoTools.bgEraser.databinding.ActivityDashboardBinding
import com.photoTools.bgEraser.utils.PickerRequestCode
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DashboardActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityDashboardBinding
    private var cameraIntentFile: File? = null
    private var currentFragment: Fragment? = null
    private var iselected: Boolean = true
    private var pickerRequestCode = PickerRequestCode.PHOTO_EDITOR
    private val adsManager:AdsManager by lazy {
        AdsManager.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setupCameraLauncher()
        setupGalleryLauncher()
        setupBottomNavigation()
        setupClickListeners()

        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.done, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem.itemId)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })

        adsManager.nativeMediumLoadAd(this, object :AdsManager.NativeLoadCallback{
            override fun onLoadAd(root: TemplateView) {
                binding.rlAds.removeAllViews()
                binding.rlAds.addView(root)
            }

            override fun onErrorAd(error: String) {
                binding.rlAds.removeAllViews()
            }

        })
    }

    private fun handleNavigationItemSelected(itemId: Int) {
        when (itemId) {
            R.id.nav_privacy_policy -> {
                onClickPrivacyPolicy()
            }

            R.id.nav_rate_us -> {
                onClickRateUs()
            }

            R.id.nav_share_app -> {
                onClickShareApp()
            }

            R.id.nav_more_apps -> {
                moreApps()
            }

            R.id.nav_close -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }

            R.id.nav_Exit -> {
                showExitDialog()
            }
        }
    }

    private fun moreApps() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("More Apps")
        builder.setMessage("You will be redirect to our Play store account.\nAre you sure?")
        builder.setPositiveButton("Redirect") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.cancel()
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://search?q=pub:" + getString(R.string.developer_account_link))
                    )
                )
            } catch (ex: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/search?q=3DevTech&c=apps")
                    )
                )
            }
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
        builder.show()
    }

    private fun onClickRateUs() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rate App")
        builder.setMessage("Please give your feedback about the application. We will consider your point of view at serious note.")
        builder.setPositiveButton("Rate us") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.cancel()
            val uri =
                Uri.parse("market://details?id=com.bg.remover.android.background.eraser.editor")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            try {
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.bg.remover.android.background.eraser.editor")
                    )
                )
            }
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
        builder.show()
    }

    private fun onClickShareApp() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Share with Friend")
        builder.setMessage("App invite link will be share with your friend. Thanks for like our app.\nAre you sure?")
        builder.setPositiveButton("Share") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.cancel()
            val intent =
                Intent(Intent.ACTION_SEND)
            val shareBody =
                "https://play.google.com/store/apps/details?id=com.bg.remover.android.background.eraser.editor"
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(intent, "Share Using"))
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
        builder.show()
    }

    private fun onClickPrivacyPolicy() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Privacy Policy")
        builder.setMessage("You will be redirect to Browser to open privacy policy.\nAre you sure?")
        builder.setPositiveButton("Redirect") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.cancel()
            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse(getString(R.string.policy_url))
                )
            )
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
        builder.show()
    }

    private fun showExitDialog() {
        val exitDialog = ExitDialogBuilder.Builder(this).withTitle("Exit App")
            .withMessage("Are you sure you want to exit?")
            .withButtonListener("Yes", object : ExitDialogBuilder.OnOkClick {
                override fun onClick(dialogs: AlertDialog) {
                    dialogs.dismiss()
                    finish()
                }
            }).withCancelButtonListener("No", object : ExitDialogBuilder.OnCancelClick {
                override fun onCancel(dialogs: AlertDialog) {
                    dialogs.dismiss()
                }
            }).build()
        exitDialog.show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            adsManager.showAd(this, object :AdsManager.IInterstitialListener{
                override fun onError(message: String) {
                    onClickEvent(menuItem.itemId)
                }
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                   onClickEvent(menuItem.itemId)
                }
            })
            true
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container_fragment, fragment)
            .addToBackStack(null).commit()
        currentFragment = fragment
    }

    private fun removeFragment() {
        currentFragment?.let {
            supportFragmentManager.beginTransaction().remove(it).addToBackStack(null).commit()
            currentFragment = null
        }
    }

    override fun onClick(view: View) {
        onClickEvent(view.id)
    }

    private fun onClickEvent(id:Int){
        when (id) {
            binding.toolbar.id -> toggleDrawer()

            binding.llRemove.id -> {
                pickerRequestCode = PickerRequestCode.REMOVE_BG
                requestPermission(PermissionsUtils.getStoragePermission(), 0)
            }

            binding.llCollage.id -> {
                requestPermission(PermissionsUtils.getStoragePermission(), 1)
            }

            binding.llEdit.id -> {
                pickerRequestCode = PickerRequestCode.PHOTO_EDITOR
                requestPermission(PermissionsUtils.getStoragePermission(), 0)
            }

            binding.llFrame.id -> {
                pickerRequestCode = PickerRequestCode.FRAME
                requestPermission(PermissionsUtils.getStoragePermission(), 0)
            }

            R.id.creation -> {
                openFragment(MyCreationFragment())
                iselected = true
            }

            R.id.nav_home -> {
                removeFragment()
            }
        }
    }

    private fun setupClickListeners() {
        binding.llRemove.setOnClickListener(this)
        //  binding.camera.setOnClickListener(this)
        binding.llEdit.setOnClickListener(this)
        binding.llCollage.setOnClickListener(this)
        //   binding.gallery.setOnClickListener(this)
        binding.llFrame.setOnClickListener(this)
        binding.toolbar.setOnClickListener(this)
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupCameraLauncher() {
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    Log.d("CameraIntentUri2", cameraIntentFile.toString())
                    if (cameraIntentFile != null) {
                        Log.d("CameraIntentUri0", cameraIntentFile.toString())
                        startActivity(Intent(this, ImageEditActivity::class.java).apply {
                            putExtra("IMAGE_URI", cameraIntentFile.toString())
                        })
                    }
                }
            }
    }

    private fun openCameraAndTakePicture() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val timeStamp: String =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        cameraIntentFile = File(file, "IMG-PhotoEditor-$timeStamp.jpg")
        val cameraIntentUri = FileProvider.getUriForFile(
            this, "$packageName.provider", cameraIntentFile!!
        )
        Log.d("CameraIntentUri", cameraIntentUri.toString())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraIntentUri)
        cameraLauncher.launch(intent)
    }

    private fun setupGalleryLauncher() {
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data.let { intent ->
                        val tempUri = intent?.getStringExtra("result")
                        val uri = Utilities.getRealPathFromURI(this, Uri.parse(tempUri))
                        startActivity(Intent(this, ImageEditActivity::class.java).apply {
                            putExtra("IMAGE_URI", uri)
                        })
                    }
                }
            }
    }

    private val gallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data.let { intent ->
                    val tempUri = intent?.getStringExtra("result")
                    val uri = Utilities.getRealPathFromURI(this, Uri.parse(tempUri))
                    val source = "Dashboard"
                    if (pickerRequestCode == PickerRequestCode.FRAME) {
                        startActivity(Intent(this, FrameActivity::class.java).apply {
                            putExtra("IMAGE_URI", uri)
                            putExtra("SOURCE", source)
                        })
                    } else if (pickerRequestCode == PickerRequestCode.PHOTO_EDITOR) {
                        startActivity(Intent(this, ImageEditActivity::class.java).apply {
                            putExtra("IMAGE_URI", uri)
                            putExtra("SOURCE", source)
                        })
                    } else if (pickerRequestCode == PickerRequestCode.REMOVE_BG) {
                        startActivity(Intent(this, BgRemoverActivity::class.java).apply {
                            putExtra("IMAGE_URI", uri)
                            putExtra("SOURCE", source)
                        })
                    }
                }
            }
        }

    private fun requestPermission(onGranted: () -> Unit) {
        Dexter.withContext(this).withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    onGranted()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?, token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun requestPermission(permissions: List<String>, type: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val limitedAccessPerm = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
            if (limitedAccessPerm == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this@DashboardActivity, PhotoPickerActivity::class.java)
                if (type == 0) {
                    intent.putExtra("picker_type", 0)
                    gallery.launch(intent)
                } else {
                    intent.putExtra("picker_type", 1)
                    galleryForCollageLauncher.launch(intent)
                }
                return
            }
        }
        Dexter.withContext(this).withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted() || multiplePermissionsReport.grantedPermissionResponses.contains(
                            PermissionGrantedResponse.from(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                        )
                    ) {
                        val intent = Intent(this@DashboardActivity, PhotoPickerActivity::class.java)
                        if (type == 0) {
                            intent.putExtra("picker_type", 0)
                            gallery.launch(intent)
                        } else {
                            intent.putExtra("picker_type", 1)
                            galleryForCollageLauncher.launch(intent)
                        }
                    } else if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied) {
                        showPermanentDenialDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).withErrorListener { dexterError: DexterError? ->
                Toast.makeText(this@DashboardActivity, "Error occurred! ", Toast.LENGTH_SHORT)
                    .show()
            }.onSameThread().check()
    }

    private fun showPermanentDenialDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Denied")
        builder.setMessage("You have permanently denied this permission. Please allow it from settings.")
        builder.setPositiveButton("Settings") { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private val galleryForCollageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { intent ->
                    val tempUriArray = intent.getStringArrayListExtra("result")
                    if (tempUriArray != null) {
                        startActivity(Intent(this, CollageActivity::class.java).apply {
                            putStringArrayListExtra("IMAGE_URIS", tempUriArray)
                        })
                    }
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        deleteAllFiles(this)
    }
}
