package com.photoTools.bgEraser.fragments

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.photoTools.bgEraser.adapter.ColorAdapter
import com.photoTools.bgEraser.databinding.FragmentBorderBinding

class BorderFragment : Fragment() {

    private var _binding: FragmentBorderBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_IMAGE_URI)?.let {
            imageUri = Uri.parse(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBorderBinding.inflate(inflater, container, false)
        loadImage()
        binding.borderBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .remove(this@BorderFragment)
                .commit()
        }
        binding.listBorder.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val cAdapter = ColorAdapter(requireContext())
        cAdapter.setOnColorClick(object : ColorAdapter.ColorClickListener {
            override fun onItemClick(view: View, colorName: String) {
                binding.frameView.setBackgroundColor(Integer.valueOf(Color.parseColor(colorName)))
            }
        })
        binding.listBorder.adapter = cAdapter
        return binding.root
    }

    private fun loadImage() {
        imageUri?.let {
            Glide.with(this)
                .load(it)
                .into(binding.imageView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_IMAGE_URI = "IMAGE_URI"

        fun newInstance(imageUri: String): BorderFragment {
            return BorderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_IMAGE_URI, imageUri)
                }
            }
        }
    }
}

