package com.genesys.glideimageview.sample.demo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.genesys.glideimageview.Shape
import com.genesys.glideimageview.sample.R
import com.genesys.glideimageview.sample.SampleImages
import com.genesys.glideimageview.sample.databinding.FragmentScrollListBinding
import com.genesys.glideimageview.sample.databinding.ItemShapeBinding
import com.genesys.glideimageview.sample.ext.BorderShape
import com.genesys.glideimageview.sample.ext.GrayscaleShape
import com.genesys.glideimageview.sample.ext.SquircleShape
import androidx.core.graphics.toColorInt

class ShapesFragment : Fragment(R.layout.fragment_scroll_list) {

    private data class ShapeDemo(val label: String, val shapes: List<Shape>)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentScrollListBinding.bind(view)
        val inflater = LayoutInflater.from(requireContext())
        val density = resources.displayMetrics.density

        binding.container.addView(caption())
        demos(density).chunked(COLUMNS).forEach { rowItems ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (16 * density).toInt() }
            }
            rowItems.forEach { demo ->
                val item = ItemShapeBinding.inflate(inflater, row, false)
                item.tvLabel.text = demo.label
                item.image.shapes = demo.shapes
                item.image.load(SampleImages.AVATAR)
                row.addView(item.root)
            }
            repeat(COLUMNS - rowItems.size) { row.addView(spacer()) }
            binding.container.addView(row)
        }
    }

    private fun demos(density: Float) = listOf(
        ShapeDemo(getString(R.string.shape_none), emptyList()),
        ShapeDemo(getString(R.string.shape_circle), listOf(Shape.Circle)),
        ShapeDemo(
            getString(R.string.shape_rounded),
            listOf(Shape.RoundedCorners((16 * density).toInt()))
        ),
        ShapeDemo(getString(R.string.shape_squircle), listOf(SquircleShape())),
        ShapeDemo(getString(R.string.shape_grayscale), listOf(GrayscaleShape)),
        ShapeDemo(
            getString(R.string.shape_circle_gray),
            listOf(Shape.Circle, GrayscaleShape)
        ),
        ShapeDemo(
            getString(R.string.shape_rounded_border),
            listOf(
                Shape.RoundedCorners((16 * density).toInt()),
                BorderShape(4 * density, "#2563EB".toColorInt(), 16 * density)
            )
        ),
        ShapeDemo(
            getString(R.string.shape_squircle_gray),
            listOf(SquircleShape(curvature = 3f), GrayscaleShape)
        )
    )

    private fun caption() = TextView(requireContext()).apply {
        setText(R.string.shapes_caption)
        setTextColor(resources.getColor(R.color.text_secondary, null))
        textSize = 12f
        setPadding(0, 0, 0, (16 * resources.displayMetrics.density).toInt())
    }

    private fun spacer() = View(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
    }

    private companion object {
        const val COLUMNS = 3
    }
}
