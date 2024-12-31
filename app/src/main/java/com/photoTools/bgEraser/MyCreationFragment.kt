package com.photoTools.bgEraser

import RecentImagesAdapter
import Utilities
import android.app.Activity.RESULT_OK
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.homeworkoutmate.dailyfitnessapp.gymexercise.utils.AdsManager
import com.photoTools.bgEraser.ImageEditActivity
import com.photoTools.bgEraser.databinding.BottomSheetImageOptionsBinding
import com.photoTools.bgEraser.databinding.FragmentMyCreationBinding
import java.io.File

class MyCreationFragment : Fragment() {

    private var _binding: FragmentMyCreationBinding? = null
    private val recentImages: MutableList<File> by lazy { getSavedImages().toMutableList() }
    private val binding get() = _binding!!
    private lateinit var recentImagesAdapter: RecentImagesAdapter
    private val adsManager: AdsManager by lazy {
        AdsManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyCreationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerViewRecent.layoutManager = gridLayoutManager

        if (recentImages.isEmpty()) {
            showNoRecentImages()
        } else {
            showRecentImages()
        }

        adsManager.loadCollapsibleBanner(requireContext(), Utilities.getAdSize(requireActivity()),
            object : AdsManager.IBannerListener {
                override fun onBannerLoaded(root: AdView) {
                    binding.rlAds.removeAllViews()
                    binding.rlAds.addView(root)
                }

                override fun onBannerError(s: String) {
                    binding.rlAds.removeAllViews()
                }

            })
    }

    private fun showNoRecentImages() {
        binding.llRecent.visibility = View.VISIBLE
        binding.recyclerViewRecent.visibility = View.GONE
    }

    private fun showRecentImages() {
        binding.llRecent.visibility = View.GONE
        binding.recyclerViewRecent.visibility = View.VISIBLE

        recentImagesAdapter = RecentImagesAdapter(recentImages) { clickedImage ->
            onImageClicked(clickedImage)
        }
        binding.recyclerViewRecent.adapter = recentImagesAdapter
    }

    private fun onImageClicked(clickedImage: File) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetImageOptionsBinding.inflate(layoutInflater)

        bottomSheetDialog.setContentView(binding.root)

        binding.optionView.setOnClickListener {
            val intent = Intent(requireContext(), SaveActivity::class.java).apply {
                putExtra("IMAGE_URI", clickedImage.absolutePath)
            }
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }

        binding.optionDelete.setOnClickListener {
            showDeleteConfirmationDialog(clickedImage)
            bottomSheetDialog.dismiss()
        }

        binding.optionShare.setOnClickListener {
            shareImage(clickedImage)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showDeleteConfirmationDialog(clickedImage: File) {
        val confirmationDialog = AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this image?")
            .setPositiveButton("Yes") { _, _ ->
                deleteImage(clickedImage)
            }
            .setNegativeButton("No", null)
            .create()
        confirmationDialog.show()
    }

    private fun deleteImage(imageFile: File) {
        val uri: Uri? = getImageContentUri(imageFile)

        uri?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val deleted = requireContext().contentResolver.delete(it, null, null) > 0
                    if (deleted) {
                        handleSuccessfulDeletion(imageFile)
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete image.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: RecoverableSecurityException) {
                    // Launch the intent to request permission from the user
                    val intentSender = e.userAction.actionIntent.intentSender
                    deletePermissionLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
            }
        } ?: run {
            if (imageFile.delete()) {
                handleSuccessfulDeletion(imageFile)
            } else {
                Toast.makeText(requireContext(), "Failed to delete image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareImage(imageFile: File) {
        val imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.provider",
            imageFile
        )


        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Image"))
    }

    private fun handleSuccessfulDeletion(imageFile: File) {
        Toast.makeText(requireContext(), "Image deleted successfully.", Toast.LENGTH_SHORT).show()

        recentImages.remove(imageFile)
        recentImagesAdapter.notifyDataSetChanged()

        if (recentImages.isEmpty()) {
            showNoRecentImages()
        }
    }

    private val deletePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(requireContext(), "Permission granted, retrying deletion.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Failed to delete image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageContentUri(imageFile: File): Uri? {
        val filePath = imageFile.absolutePath
        val resolver: ContentResolver = requireContext().contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=?",
            arrayOf(filePath), null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            }
        }
        return null
    }

    private fun getSavedImages(): List<File> {
        val picturesDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Photo Editor")
        return if (picturesDir.exists() && picturesDir.isDirectory) {
            picturesDir.listFiles()?.filter {
                it.isFile && it.name.endsWith(".png")
            }?.sortedByDescending {
                it.lastModified()
            } ?: emptyList()
        } else {
            emptyList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
