package com.genesys.glideimageview.sample.ext

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.load.Transformation
import com.genesys.glideimageview.Shape

data class SquircleShape(val curvature: Float = 4f) : Shape {
    override fun transformation(view: ImageView): Transformation<Bitmap> =
        SquircleTransformation(curvature)
}

data class BorderShape(
    val widthPx: Float,
    val color: Int,
    val radiusPx: Float = 0f
) : Shape {
    override fun transformation(view: ImageView): Transformation<Bitmap> =
        BorderTransformation(widthPx, color, radiusPx)
}

object GrayscaleShape : Shape {
    private val transformation = GrayscaleTransformation()
    override val appliesScaleType: Boolean get() = false
    override fun transformation(view: ImageView): Transformation<Bitmap> = transformation
}
