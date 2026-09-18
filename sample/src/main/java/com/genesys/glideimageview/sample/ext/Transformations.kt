package com.genesys.glideimageview.sample.ext

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.withSign

class GrayscaleTransformation : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val result = pool.get(toTransform.width, toTransform.height, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        Canvas(result).drawBitmap(toTransform, 0f, 0f, paint)
        return result
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    override fun equals(other: Any?): Boolean = other is GrayscaleTransformation

    override fun hashCode(): Int = ID.hashCode()

    private companion object {
        const val ID = "com.genesys.glideimageview.sample.GrayscaleTransformation"
        val ID_BYTES: ByteArray = ID.toByteArray(Charsets.UTF_8)
    }
}

class SquircleTransformation(private val curvature: Float) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        val result = pool.get(width, height, Bitmap.Config.ARGB_8888)
        result.setHasAlpha(true)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        Canvas(result).drawPath(superellipse(width.toFloat(), height.toFloat()), paint)
        return result
    }

    private fun superellipse(width: Float, height: Float): Path {
        val a = width / 2f
        val b = height / 2f
        val exponent = 2.0 / curvature
        return Path().apply {
            for (step in 0..SEGMENTS) {
                val angle = step * 2.0 * PI / SEGMENTS
                val x = a * abs(cos(angle)).pow(exponent).withSign(cos(angle))
                val y = b * abs(sin(angle)).pow(exponent).withSign(sin(angle))
                val px = (a + x).toFloat()
                val py = (b + y).toFloat()
                if (step == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        messageDigest.update(curvature.toBits().toString().toByteArray(Charsets.UTF_8))
    }

    override fun equals(other: Any?): Boolean =
        other is SquircleTransformation && other.curvature == curvature

    override fun hashCode(): Int = ID.hashCode() * 31 + curvature.hashCode()

    private companion object {
        const val SEGMENTS = 180
        const val ID = "com.genesys.glideimageview.sample.SquircleTransformation"
        val ID_BYTES: ByteArray = ID.toByteArray(Charsets.UTF_8)
    }
}

class BorderTransformation(
    private val widthPx: Float,
    private val color: Int,
    private val radiusPx: Float
) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val result = pool.get(toTransform.width, toTransform.height, Bitmap.Config.ARGB_8888)
        result.setHasAlpha(true)
        val canvas = Canvas(result)
        canvas.drawBitmap(toTransform, 0f, 0f, null)

        val inset = widthPx / 2f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = widthPx
            color = this@BorderTransformation.color
        }
        canvas.drawRoundRect(
            inset,
            inset,
            toTransform.width - inset,
            toTransform.height - inset,
            radiusPx,
            radiusPx,
            paint
        )
        return result
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        messageDigest.update("$widthPx|$color|$radiusPx".toByteArray(Charsets.UTF_8))
    }

    override fun equals(other: Any?): Boolean = other is BorderTransformation &&
        other.widthPx == widthPx && other.color == color && other.radiusPx == radiusPx

    override fun hashCode(): Int = ID.hashCode() * 31 + widthPx.hashCode() + color + radiusPx.hashCode()

    private companion object {
        const val ID = "com.genesys.glideimageview.sample.BorderTransformation"
        val ID_BYTES: ByteArray = ID.toByteArray(Charsets.UTF_8)
    }
}
