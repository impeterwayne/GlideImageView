package com.genesys.glideimageview.sample.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.genesys.glideimageview.sample.R
import com.genesys.glideimageview.sample.SampleImages
import com.genesys.glideimageview.sample.databinding.FragmentListBinding
import com.genesys.glideimageview.sample.databinding.ItemPhotoBinding
import com.genesys.glideimageview.sample.ext.Avatar
import com.genesys.glideimageview.sample.ext.LoadStats

class ListFragment : Fragment(R.layout.fragment_list) {

    private var statsObserver: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentListBinding.bind(view)

        binding.recycler.layoutManager = GridLayoutManager(requireContext(), COLUMNS)
        binding.recycler.setHasFixedSize(true)
        binding.recycler.adapter = PhotoAdapter(SampleImages.feed(ITEM_COUNT))

        val observer = { binding.tvStats.text = LoadStats.summary() }
        statsObserver = observer
        LoadStats.observe(observer)
    }

    override fun onDestroyView() {
        statsObserver?.let(LoadStats::stopObserving)
        statsObserver = null
        super.onDestroyView()
    }

    private class PhotoAdapter(private val items: List<Any>) :
        RecyclerView.Adapter<PhotoViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PhotoViewHolder(
            ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) =
            holder.bind(items[position])

        override fun onViewRecycled(holder: PhotoViewHolder) = holder.recycle()
    }

    private class PhotoViewHolder(
        private val binding: ItemPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: Any) {
            binding.tvCaption.text = caption(model)
            binding.image.load(model)
        }

        fun recycle() {
            binding.image.clear()
        }

        private fun caption(model: Any) = when {
            model is Avatar -> "Avatar(${model.userId})"
            model is String && model.startsWith("http") -> "URL " + model.substringAfter("seed/")
                .substringBefore('/')
            model is String -> model.substringAfterLast('/')
            else -> model.toString()
        }
    }

    private companion object {
        const val ITEM_COUNT = 1000
        const val COLUMNS = 3
    }
}
