package com.genesys.glideimageview.sample.demo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.ImageView.ScaleType
import androidx.fragment.app.Fragment
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.genesys.glideimageview.GlideImageView
import com.genesys.glideimageview.OnLoadListener
import com.genesys.glideimageview.RequestDecorator
import com.genesys.glideimageview.Shape
import com.genesys.glideimageview.sample.R
import com.genesys.glideimageview.sample.SampleImages
import com.genesys.glideimageview.sample.databinding.FragmentPlaygroundBinding
import com.genesys.glideimageview.sample.ext.Avatar
import com.genesys.glideimageview.sample.ext.BorderShape
import com.genesys.glideimageview.sample.ext.ExactSizeDecorator
import com.genesys.glideimageview.sample.ext.GrayscaleShape
import com.genesys.glideimageview.sample.ext.SquircleShape
import com.genesys.glideimageview.sample.ext.priorityDecorator
import com.genesys.glideimageview.sample.ext.thumbnailDecorator
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PlaygroundFragment : Fragment(R.layout.fragment_playground) {

    private class Option<T>(val label: String, val value: T)

    private var binding: FragmentPlaygroundBinding? = null

    private val sources = listOf(
        Option<Any?>("Remote", SampleImages.REMOTE),
        Option<Any?>("Drawable", R.drawable.placeholder_image),
        Option<Any?>("Asset", SampleImages.BANNER),
        Option<Any?>("Avatar model", Avatar(userId = 7, sizePx = 600)),
        Option<Any?>("Broken URL", SampleImages.BROKEN),
        Option<Any?>("null", null)
    )

    private val shapeBuilders = listOf<Option<(Float, Int) -> Shape>>(
        Option("Circle") { _, _ -> Shape.Circle },
        Option("Rounded") { _, radiusPx -> Shape.RoundedCorners(radiusPx) },
        Option("Squircle") { _, _ -> SquircleShape() },
        Option("Grayscale") { _, _ -> GrayscaleShape },
        Option("Border") { density, radiusPx ->
            BorderShape(3 * density, 0xFF2563EB.toInt(), radiusPx.toFloat())
        }
    )

    private val decoratorOptions = listOf(
        Option("Thumbnail", thumbnailDecorator(0.05f)),
        Option("Exact size", ExactSizeDecorator),
        Option("Low priority", priorityDecorator(Priority.LOW)),
        Option("No memory cache", RequestDecorator { _, request -> request.skipMemoryCache(true) })
    )

    private val scaleTypes = listOf(
        Option("centerCrop", ScaleType.CENTER_CROP),
        Option("fitCenter", ScaleType.FIT_CENTER),
        Option("centerInside", ScaleType.CENTER_INSIDE)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentPlaygroundBinding.bind(view).also { this.binding = it }

        binding.image.addOnLoadListener(statusListener(binding))

        binding.sourceGroup.fill(sources.map { it.label }, singleSelection = true) { index ->
            binding.image.load(sources[index].value)
        }
        binding.shapeGroup.fill(shapeBuilders.map { it.label }) { applyOptions() }
        binding.decoratorGroup.fill(decoratorOptions.map { it.label }) { applyOptions() }
        binding.scaleTypeGroup.fill(scaleTypes.map { it.label }, singleSelection = true) { index ->
            binding.image.scaleType = scaleTypes[index].value
        }

        binding.sliderRadius.addOnChangeListener { _, value, _ ->
            binding.tvRadius.text = getString(R.string.label_corner_radius_value, value.toInt())
            applyOptions()
        }
        binding.tvRadius.text =
            getString(R.string.label_corner_radius_value, binding.sliderRadius.value.toInt())

        binding.btnClear.setOnClickListener { binding.image.clear() }

        binding.sourceGroup.checkAt(0)
        binding.scaleTypeGroup.checkAt(0)
        binding.shapeGroup.checkAt(1)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun applyOptions() {
        val binding = binding ?: return
        val density = resources.displayMetrics.density
        val radiusPx = (binding.sliderRadius.value * density).toInt()

        binding.image.updateOptions {
            copy(
                shapes = binding.shapeGroup.checkedIndices()
                    .map { shapeBuilders[it].value(density, radiusPx) },
                decorators = binding.decoratorGroup.checkedIndices()
                    .map { decoratorOptions[it].value }
            )
        }
    }

    private fun statusListener(binding: FragmentPlaygroundBinding) = object : OnLoadListener {
        override fun onLoadStarted(view: GlideImageView) {
            binding.tvStatus.setText(R.string.status_loading)
        }

        override fun onResourceReady(
            view: GlideImageView,
            resource: Drawable,
            dataSource: DataSource
        ) {
            binding.tvStatus.text = getString(R.string.status_success, dataSource.name)
        }

        override fun onLoadFailed(view: GlideImageView, error: GlideException?) {
            binding.tvStatus.setText(R.string.status_failed)
        }

        override fun onCleared(view: GlideImageView) {
            binding.tvStatus.setText(R.string.status_cleared)
        }
    }

    private fun ChipGroup.fill(
        labels: List<String>,
        singleSelection: Boolean = false,
        onChanged: (index: Int) -> Unit
    ) {
        val themed = ContextThemeWrapper(context, R.style.Chip_Filter)
        labels.forEachIndexed { index, label ->
            addView(
                Chip(themed).apply {
                    text = label
                    isCheckable = true
                    setOnCheckedChangeListener { _, checked ->
                        if (!singleSelection || checked) onChanged(index)
                    }
                }
            )
        }
    }

    private fun ChipGroup.checkAt(index: Int) {
        (getChildAt(index) as? Chip)?.isChecked = true
    }

    private fun ChipGroup.checkedIndices(): List<Int> =
        (0 until childCount).filter { (getChildAt(it) as? Chip)?.isChecked == true }
}
