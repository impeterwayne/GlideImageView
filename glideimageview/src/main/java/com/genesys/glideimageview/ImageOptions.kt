package com.genesys.glideimageview

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import com.bumptech.glide.load.Transformation

data class ImageOptions(
    @param:DrawableRes val placeholder: Int = NO_RESOURCE,
    @param:DrawableRes val error: Int = NO_RESOURCE,
    @param:DrawableRes val fallback: Int = NO_RESOURCE,
    val crossFade: Boolean = false,
    val crossFadeDurationMs: Int = DEFAULT_CROSS_FADE_MS,
    val shapes: List<Shape> = emptyList(),
    val transformations: List<Transformation<Bitmap>> = emptyList(),
    val decorators: List<RequestDecorator> = emptyList()
) {

    fun withShape(shape: Shape): ImageOptions = copy(shapes = shapes + shape)

    fun withoutShapes(predicate: (Shape) -> Boolean): ImageOptions =
        copy(shapes = shapes.filterNot(predicate))

    fun withShapeReplacing(shape: Shape): ImageOptions =
        copy(shapes = shapes.filterNot { it::class == shape::class } + shape)

    fun withTransformation(transformation: Transformation<Bitmap>): ImageOptions =
        copy(transformations = transformations + transformation)

    fun withDecorator(decorator: RequestDecorator): ImageOptions =
        copy(decorators = decorators + decorator)

    companion object {
        const val NO_RESOURCE: Int = 0
        const val DEFAULT_CROSS_FADE_MS: Int = 300
    }
}
