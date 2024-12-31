package com.photoTools.bgEraser.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.photoTools.bgEraser.FrameActivity
import com.photoTools.bgEraser.FrameAdapter2
import com.photoTools.bgEraser.databinding.FragmentFrameCategoryBinding

class FrameCategoryFragment : Fragment() {

    private var frameList: List<Int> = emptyList()
    private var _binding: FragmentFrameCategoryBinding? = null
    private val binding get() = _binding!!
    private var callback: FrameSelectionCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        frameList = arguments?.getIntegerArrayList(ARG_FRAME_LIST)?.toList() ?: emptyList()

        callback = activity as? FrameSelectionCallback
        if (callback == null) {
            throw IllegalStateException("Parent activity must implement FrameSelectionCallback")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFrameCategoryBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        val frameAdapter = FrameAdapter2(
            requireContext(),
            frameList.toTypedArray()
        ) { frameId ->
            handleFrameSelection(frameId)
        }
        binding.recyclerViewFrames.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = frameAdapter
        }
    }

    private fun handleFrameSelection(frameId: Int) {
        val frameUri = Uri.parse("android.resource://${requireContext().packageName}/$frameId")
        callback?.onFrameSelected(frameUri)
    }

    interface FrameSelectionCallback {
        fun onFrameSelected(frameUri: Uri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_FRAME_LIST = "FRAME_LIST"

        fun newInstance(frameList: List<Int>): FrameCategoryFragment {
            val fragment = FrameCategoryFragment()
            fragment.arguments = Bundle().apply {
                putIntegerArrayList(ARG_FRAME_LIST, ArrayList(frameList))
            }
            return fragment
        }
    }
}
