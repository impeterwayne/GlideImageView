package com.genesys.glideimageview

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners as GlideRoundedCorners

interface Shape {

    fun transformation(view: ImageView): Transformation<Bitmap>?

    val appliesScaleType: Boolean get() = true

    object Original : Shape {
        override fun transformation(view: ImageView): Transformation<Bitmap>? = null
    }

    object Circle : Shape {
        private val transformation = CircleCrop()
        override val appliesScaleType: Boolean get() = false
        override fun transformation(view: ImageView): Transformation<Bitmap> = transformation
    }

    data class RoundedCorners(val radiusPx: Int) : Shape {
        override fun transformation(view: ImageView): Transformation<Bitmap>? =
            if (radiusPx > 0) GlideRoundedCorners(radiusPx) else null
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun of(
            transformation: Transformation<Bitmap>,
            appliesScaleType: Boolean = true
        ): Shape = Wrapped(transformation, appliesScaleType)
    }

    private data class Wrapped(
        private val transformation: Transformation<Bitmap>,
        override val appliesScaleType: Boolean
    ) : Shape {
        override fun transformation(view: ImageView): Transformation<Bitmap> = transformation
    }
}
