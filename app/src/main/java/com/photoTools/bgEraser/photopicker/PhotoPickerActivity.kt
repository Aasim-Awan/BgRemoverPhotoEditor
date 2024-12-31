package com.photoTools.bgEraser

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.homeworkoutmate.dailyfitnessapp.gymexercise.utils.AdsManager
import com.photoTools.bgEraser.PickerViewModel
import com.photoTools.bgEraser.PickerViewModelFactory
import com.photoTools.bgEraser.SelectedPhotoAdapter
import com.photoTools.bgEraser.databinding.ActivityPhotoPickerBinding


class PhotoPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoPickerBinding
    private lateinit var adapter: PhotoPickerAdapter
    private lateinit var selectedAdapter: SelectedPhotoAdapter
    private var imageList: MutableList<Uri> = ArrayList()
    private var selectedList: MutableList<Uri> = ArrayList()
    private var isRedirectToPermission: Boolean = false
    private val adsManager by lazy {
        AdsManager.getInstance(this)
    }

    private val viewModel: PickerViewModel by viewModels {
        PickerViewModelFactory(PickerRepo(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //binding.toolbar.title = "Photo Picker"
        supportActionBar?.hide()

        val log = checkPermission()

        val pickerType = intent.getIntExtra("picker_type", 0)

        if (pickerType != 0) {
            binding.clSelectedCont.visibility = View.VISIBLE
        }

        val layoutManager = GridLayoutManager(this, 4)
        val layoutManagerLinear = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvPicker.layoutManager = layoutManager
        binding.rcvSelected.layoutManager = layoutManagerLinear

        adapter = PhotoPickerAdapter(
            this,
            imageList,
            selectedList,
            object : PhotoPickerAdapter.ClickListener {
                override fun onItemClick(isChecked: Boolean, uri: Uri) {
                    Log.d("PhotoPicker", "isSelected $isChecked, uri: $uri")
                    if (pickerType == 0) {
                        val returnIntent = Intent()
                        returnIntent.putExtra("result", uri.toString())
                        adsManager.showAd(this@PhotoPickerActivity, object :AdsManager.IInterstitialListener{
                            override fun onError(message: String) {
                                setResult(RESULT_OK, returnIntent)
                                finish()
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                setResult(RESULT_OK, returnIntent)
                                finish()
                            }

                        })
                    } else {
                        if (isChecked) {
                            selectedList.add(uri)
                        } else {
                            try {
                                val pos = selectedList.indexOf(uri)
                                selectedList.removeAt(pos)
                            } catch (e: IndexOutOfBoundsException) {
                                selectedList.clear()
                                e.printStackTrace()
                            }
                        }
                        binding.tvHint.text = "You have selected ${selectedList.size} images"
                        selectedAdapter.notifyDataSetChanged()
                    }
                }
            })
        adapter.setPickerType(pickerType)

        selectedAdapter =
            SelectedPhotoAdapter(this, selectedList, object : SelectedPhotoAdapter.ClickListener {
                override fun onItemRemoved(uri: Uri) {
                    try {
                        val pos = selectedList.indexOf(uri)
                        selectedList.removeAt(pos)
                    } catch (e: IndexOutOfBoundsException) {
                        selectedList.clear()
                        e.printStackTrace()
                    }
                    binding.tvHint.text = "You have selected ${selectedList.size} images"
                    selectedAdapter.notifyDataSetChanged()
                    adapter.notifyDataSetChanged()
                }
            })

        binding.rcvPicker.adapter = adapter
        binding.rcvSelected.adapter = selectedAdapter

        viewModel.onFetchedImage().observe(this) {
            imageList.clear()
            selectedList.clear()
            imageList.addAll(it)
            adapter.notifyDataSetChanged()
            selectedAdapter.notifyDataSetChanged()
            Log.d("selected File", "Selected File => ${it.size}")
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val returnIntent = Intent()
                setResult(Activity.RESULT_CANCELED, returnIntent)
                finish()
            }
        })

        binding.tvNext.setOnClickListener {
            val list = selectedAdapter.getSelectedList()
            val arrayList2 = ArrayList<String>()
            for (element in list) {
                arrayList2.add(element.toString())
            }
            if (list.size > 1) {
                val returnIntent = Intent()
                returnIntent.putStringArrayListExtra("result", arrayList2)

                adsManager.showAd(this@PhotoPickerActivity, object :AdsManager.IInterstitialListener{
                    override fun onError(message: String) {
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }

                })
            } else {
                Toast.makeText(this, "Select 2 to 9 images", Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnGrantPerm.setOnClickListener {
            val intent = Intent()
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.setData(uri)
            startActivity(intent)
            isRedirectToPermission = true

        }

        binding.btnBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
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

    }

    private fun checkPermission(): Boolean {
        for (p in PermissionsUtils.getStoragePermission()) {
            Log.d("PermissionLog", "Permission granted: $p")
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                binding.rlPermission.visibility = View.VISIBLE
                return false
            }
        }
        binding.rlPermission.visibility = View.GONE
        return true
    }

    override fun onResume() {
        super.onResume()
        if (isRedirectToPermission) {
            viewModel.getImages()
        }
        isRedirectToPermission = false
    }
}