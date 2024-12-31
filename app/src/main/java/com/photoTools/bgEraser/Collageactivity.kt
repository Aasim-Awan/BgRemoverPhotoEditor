package com.photoTools.bgEraser

import Utilities
import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.homeworkoutmate.dailyfitnessapp.gymexercise.utils.AdsManager
import com.photoTools.bgEraser.adapter.BackgroundAdapter
import com.photoTools.bgEraser.adapter.FrameAdapter
import com.photoTools.bgEraser.frame.FramePhotoLayout
import com.photoTools.bgEraser.model.TemplateItem
import com.photoTools.bgEraser.multitouch.PhotoView
import com.photoTools.bgEraser.utils.FrameImageUtils
import com.photoTools.bgEraser.utils.ImageUtils
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.photoTools.bgEraser.databinding.ActivityCollageactivityBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class CollageActivity : AppCompatActivity(), View.OnClickListener,
    FrameAdapter.OnFrameClickListener, BackgroundAdapter.OnBGClickListener {

    private var mFramePhotoLayout: FramePhotoLayout? = null
    private var defaultspace: Float = 0.0f
    var maxspace: Float = 0.0f
    var maxcorner: Float = 0.0f
    private val ratiosquare = 0
    private val ratiogolden = 2
    val maxspaceprogress = 300.0f
    val maxcornerprogress = 200.0f
    private val maxImageCount: Int = 10
    private val minImageCount: Int = 2
    private var mSpace = defaultspace
    private var mCorner = 0f
    private val selectedPhotos = mutableListOf<Uri>()
    private var mTemplateItemList = arrayListOf<TemplateItem>()
    private var mSelectedPhotoPaths: MutableList<String> = java.util.ArrayList()
    private var mSelectedTemplateItem: TemplateItem? = null
    private var mImageInTemplateCount: Int = 0
    private var mSavedInstanceState: Bundle? = null
    private lateinit var frameAdapter: FrameAdapter
    private lateinit var mPhotoView: PhotoView
    private var mLayoutRatio = ratiosquare
    private var mOutputScale = 1f
    private var mBackgroundImage: Bitmap? = null
    private lateinit var binding: ActivityCollageactivityBinding
    private lateinit var imgBackground: ImageView
    private var mBackgroundColor = Color.WHITE
    private var mLastClickTime: Long = 0
    private val adsManager by lazy {
        AdsManager.getInstance(this)
    }
    private lateinit var exitDialog:AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollageactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        defaultspace = ImageUtils.pxFromDp(this, 2F)
        maxspace = ImageUtils.pxFromDp(this, 30F)
        maxcorner = ImageUtils.pxFromDp(this, 60F)
        mSpace = defaultspace

        if (savedInstanceState != null) {
            mSpace = savedInstanceState.getFloat("mSpace")
            mCorner = savedInstanceState.getFloat("mCorner")
            mSavedInstanceState = savedInstanceState
        }

        binding.listBg.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.listBg.adapter = BackgroundAdapter(this, this)
        imgBackground = findViewById(R.id.img_background)

        binding.tabLayout.setOnClickListener(this)
        binding.tabBg.setOnClickListener(this)
        binding.tabBorder.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.seekbarSpace.setOnSeekBarChangeListener(Spacelistener())
        binding.seekbarCorner.setOnSeekBarChangeListener(Cornerlistener())

        binding.tabLayout.isSelected = true
        mPhotoView = PhotoView(this)
        //mImageInTemplateCount = intent.getIntExtra("IMAGE_URIS", 0)
        val uris = intent.getStringArrayListExtra("IMAGE_URIS")?.map { Uri.parse(it) } ?: listOf()

           Log.d("CollageActivity1", "Received URIs: $uris")

        selectedPhotos.clear()
        selectedPhotos.addAll(uris)
        //selectedPhotos.addAll(uris.take(maxImageCount).map { it })
        mImageInTemplateCount = selectedPhotos.size

        Log.d("CollageActivity2", "Selected Photos: ${selectedPhotos.size}")
        mPhotoView = PhotoView(this)


        loadFrameImages()
        setupFrameRecyclerView()
        //requestStoragePermission()

        mSelectedTemplateItem = mTemplateItemList[0]
        Log.d("CollageActivity3", "Selected Template Item: $mSelectedTemplateItem")
        mSelectedTemplateItem!!.isSelected = true

        binding.collageContainer.getViewTreeObserver()
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mOutputScale = ImageUtils.calculateOutputScaleFactor(
                        binding.collageContainer.width,
                        binding.collageContainer.height
                    )
                    buildLayout(mSelectedTemplateItem!!)
                    // remove listener
                    binding.collageContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                }
            })


        mSelectedTemplateItem?.let { templateItem ->
            Log.d(
                "CollageActivity4",
                "Uri size: ${uris.size}, SelectedTemplate size: ${templateItem.photoItemList.size}"
            )

            val size = uris.size.coerceAtMost(templateItem.photoItemList.size)
            Log.d("CollageActivity6", "Size: $size")

            for (i in 0 until size) {
                templateItem.photoItemList[i].imagePath = uris[i].toString() // Use Uri string
            }
        } ?: run {
            Toast.makeText(this, "No template selected", Toast.LENGTH_SHORT).show()
            Log.d("CollageActivity5", "No template selected")
        }


        adsManager.loadBanner(this, object : AdsManager.IBannerListener {
            override fun onBannerLoaded(root: AdView) {
                binding.rlAdCont.removeAllViews()
                binding.rlAdCont.addView(root)
            }

            override fun onBannerError(s: String) {
                binding.rlAdCont.removeAllViews()
            }

        })
        exitDialog = ExitDialogBuilder.Builder(this)
            .withTitle("Discard")
            .withMessage("Are you sure you want to discard?")
            .withButtonListener("Discard", object : ExitDialogBuilder.OnOkClick {
                override fun onClick(dialogs: AlertDialog) {
                    AdsManager.showAd = true
                    adsManager.showAd(this@CollageActivity, object : AdsManager.IInterstitialListener {
                        override fun onError(message: String) {
                            dialogs.dismiss()
                            finish()
                        }

                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            dialogs.dismiss()
                            finish()
                        }

                    })
                }
            })
            .withCancelButtonListener("No", object : ExitDialogBuilder.OnCancelClick {
                override fun onCancel(dialogs: AlertDialog) {
                    dialogs.dismiss()
                }
            })
            .build()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                exitDialog.show()
            }

        })


    }


    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        Dexter.withContext(this).withPermission(permission)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    launchImagePicker()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(
                        this@CollageActivity,
                        "Permission required to access storage",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun launchImagePicker() {
        imagePickerLauncher.launch(arrayOf("image/*"))
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris != null && uris.isNotEmpty()) {
                if (uris.size > maxImageCount) {
                    val toast = Toast.makeText(
                        this, "You can select up to $maxImageCount images only.", Toast.LENGTH_LONG
                    )
                    toast.show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 3000)
                    return@registerForActivityResult
                }

                selectedPhotos.clear()
                selectedPhotos.addAll(uris.take(maxImageCount).map { it }) // Limit to max 10 photos

                mImageInTemplateCount = selectedPhotos.size
                mPhotoView = PhotoView(this)


                loadFrameImages()
                setupFrameRecyclerView()

                mSelectedTemplateItem = mTemplateItemList[0]
                mSelectedTemplateItem!!.isSelected = true

                binding.collageContainer.getViewTreeObserver()
                    .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            mOutputScale = ImageUtils.calculateOutputScaleFactor(
                                binding.collageContainer.width,
                                binding.collageContainer.height
                            )
                            buildLayout(mSelectedTemplateItem!!)
                            // remove listener
                            binding.collageContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                        }
                    })


                mSelectedTemplateItem?.let { templateItem ->
                    val size = uris.size.coerceAtMost(templateItem.photoItemList.size)
                    for (i in 0 until size) {
                        templateItem.photoItemList[i].imagePath = uris[i].toString()
                    }
                } ?: run {
                    Toast.makeText(this, "No template selected", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
                finish()
            }
        }


    inner class Spacelistener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            mSpace = maxspace * seekBar!!.progress / maxspaceprogress
            if (mFramePhotoLayout != null) mFramePhotoLayout!!.setSpace(mSpace, mCorner)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    inner class Cornerlistener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            mCorner = maxcorner * seekBar!!.progress / maxcornerprogress
            if (mFramePhotoLayout != null) mFramePhotoLayout!!.setSpace(mSpace, mCorner)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putFloat("mSpace", mSpace)
        outState.putFloat("mCornerBar", mCorner)
        if (mFramePhotoLayout != null) {
            mFramePhotoLayout!!.saveInstanceState(outState)
        }

    }

    private fun loadFrameImages() {
        val mAllTemplateItemList = java.util.ArrayList<TemplateItem>()

        mAllTemplateItemList.addAll(FrameImageUtils.loadFrameImages(this))

        mTemplateItemList = java.util.ArrayList<TemplateItem>()
        if (mImageInTemplateCount > 0) {
            for (item in mAllTemplateItemList) if (item.photoItemList.size == mImageInTemplateCount) {
                mTemplateItemList.add(item)
            }
        } else {
            mTemplateItemList.addAll(mAllTemplateItemList)
        }
    }

    private fun buildLayout(item: TemplateItem) {
        mFramePhotoLayout = FramePhotoLayout(this, item.photoItemList)

        var viewWidth = binding.collageContainer.width
        var viewHeight = binding.collageContainer.height

        if (mLayoutRatio == ratiosquare) {
            if (viewWidth > viewHeight) {
                viewWidth = viewHeight
            } else {
                viewHeight = viewWidth
            }
        } else if (mLayoutRatio == ratiogolden) {
            val goldenRatio = 1.61803398875
            if (viewWidth <= viewHeight) {
                if (viewWidth * goldenRatio >= viewHeight) {
                    viewWidth = (viewHeight / goldenRatio).toInt()
                } else {
                    viewHeight = (viewWidth * goldenRatio).toInt()
                }
            } else if (viewHeight <= viewWidth) {
                if (viewHeight * goldenRatio >= viewWidth) {
                    viewHeight = (viewWidth / goldenRatio).toInt()
                } else {
                    viewWidth = (viewHeight * goldenRatio).toInt()
                }
            }
        }

        mOutputScale = ImageUtils.calculateOutputScaleFactor(viewWidth, viewHeight)

        Log.d("mOutputScale", mOutputScale.toString())

        mFramePhotoLayout?.build(viewWidth, viewHeight, mOutputScale, mSpace, mCorner)

        Log.d("mframephotolayout", mFramePhotoLayout.toString() + "framephoto")

        if (mSavedInstanceState != null) {
            mFramePhotoLayout!!.restoreInstanceState(mSavedInstanceState!!)
            mSavedInstanceState = null
        }
        val params = RelativeLayout.LayoutParams(viewWidth, viewHeight)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        binding.collageContainer.removeAllViews()

        binding.collageContainer.removeView(binding.imgBackground)
        binding.collageContainer.addView(binding.imgBackground, params)

        binding.collageContainer.addView(mFramePhotoLayout, params)
        //add sticker view
        binding.collageContainer.removeView(mPhotoView)
        binding.collageContainer.addView(mPhotoView, params)

        Log.d("photoview", mPhotoView.toString() + "photoview")

        binding.seekbarSpace.progress = (maxspaceprogress * mSpace / maxspace).toInt()
        binding.seekbarCorner.progress = (maxcornerprogress * mCorner / maxcorner).toInt()
    }

    private fun setupFrameRecyclerView() {
        binding.listFrames.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        frameAdapter = FrameAdapter(this, mTemplateItemList, this)
        binding.listFrames.adapter = frameAdapter
    }

    override fun onFrameClick(templateItem: TemplateItem) {
        mSelectedTemplateItem?.isSelected = false

        for (idx in 0 until mSelectedTemplateItem!!.photoItemList.size) {
            val photoItem = mSelectedTemplateItem!!.photoItemList[idx]
            if (photoItem.imagePath != null && photoItem.imagePath!!.isNotEmpty()) {
                if (idx < mSelectedPhotoPaths.size) {
                    mSelectedPhotoPaths.add(idx, photoItem.imagePath!!)
                } else {
                    mSelectedPhotoPaths.add(photoItem.imagePath!!)
                }
            }
        }

        val size = mSelectedPhotoPaths.size.coerceAtMost(templateItem.photoItemList.size)
        for (idx in 0 until size) {
            val photoItem = templateItem.photoItemList[idx]
            if (photoItem.imagePath == null || photoItem.imagePath!!.isEmpty()) {
                photoItem.imagePath = mSelectedPhotoPaths[idx]
            }
        }

        mSelectedTemplateItem = templateItem
        mSelectedTemplateItem!!.isSelected = true
        frameAdapter.notifyDataSetChanged()
        buildLayout(templateItem)
    }

    override fun onBGClick(drawable: Drawable) {
        try {
            val frameLayout = mFramePhotoLayout ?: return
            val bmp = frameLayout.createImage()
            val bitmapDrawable = drawable as? BitmapDrawable ?: return
            val bitmap = bitmapDrawable.bitmap
            mBackgroundImage = AndroidUtils.resizeImageToNewSize(bitmap, bmp.width, bmp.height)
            mBackgroundImage?.let {
                imgBackground.setImageBitmap(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tab_layout -> {
                binding.tabLayout.isSelected = true
                binding.tabBorder.isSelected = false
                binding.tabBg.isSelected = false


                binding.textLayout.setTextColor(resources.getColor(R.color.white))
                binding.textBorder.setTextColor(resources.getColor(R.color.black))
                binding.textBg.setTextColor(resources.getColor(R.color.black))

                // Update visibility
                binding.llFrame.visibility = View.VISIBLE
                binding.llBorder.visibility = View.GONE
                binding.llBg.visibility = View.GONE
            }

            R.id.tab_border -> {
                binding.tabLayout.isSelected = false
                binding.tabBorder.isSelected = true
                binding.tabBg.isSelected = false

                binding.textLayout.setTextColor(resources.getColor(R.color.black))
                binding.textBorder.setTextColor(resources.getColor(R.color.white))
                binding.textBg.setTextColor(resources.getColor(R.color.black))

                binding.llFrame.visibility = View.GONE
                binding.llBorder.visibility = View.VISIBLE
                binding.llBg.visibility = View.GONE
            }

            R.id.tab_bg -> {
                binding.tabLayout.isSelected = false
                binding.tabBorder.isSelected = false
                binding.tabBg.isSelected = true

                binding.textLayout.setTextColor(resources.getColor(R.color.black))
                binding.textBorder.setTextColor(resources.getColor(R.color.black))
                binding.textBg.setTextColor(resources.getColor(R.color.white))

                binding.llFrame.visibility = View.GONE
                binding.llBorder.visibility = View.GONE
                binding.llBg.visibility = View.VISIBLE
            }

            R.id.btn_done -> {
                if (selectedPhotos.isEmpty()) {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                } else {
                    val outStream: FileOutputStream?
                    try {
                        val collageBitmap = createOutputImage()
                        outStream = FileOutputStream(File(cacheDir, "tempBMP"))
                        collageBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outStream)
                        outStream.close()
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val intent = Intent(this, FilterCollageActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            R.id.btn_back -> {
                exitDialog.show()
            }

        }
    }

    fun checkClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    private fun createOutputImage(): Bitmap {
        try {
            val template = mFramePhotoLayout!!.createImage()
            val result =
                Bitmap.createBitmap(template.width, template.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            if (mBackgroundImage != null && !mBackgroundImage!!.isRecycled) {
                canvas.drawBitmap(
                    mBackgroundImage!!,
                    Rect(0, 0, mBackgroundImage!!.getWidth(), mBackgroundImage!!.getHeight()),
                    Rect(0, 0, result.width, result.height),
                    paint
                )
            } else {
                canvas.drawColor(mBackgroundColor)
            }

            canvas.drawBitmap(template, 0f, 0f, paint)
            template.recycle()
            val stickers = mPhotoView.getImage(mOutputScale)
            canvas.drawBitmap(stickers!!, 0f, 0f, paint)
            stickers.recycle()
            //stickers = null
            System.gc()
            return result
        } catch (error: OutOfMemoryError) {
            throw error
        }
    }

}