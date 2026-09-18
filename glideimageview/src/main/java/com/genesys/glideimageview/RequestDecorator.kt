package com.genesys.glideimageview

import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder

fun interface RequestDecorator {
    fun decorate(view: GlideImageView, request: RequestBuilder<Drawable>): RequestBuilder<Drawable>
}
