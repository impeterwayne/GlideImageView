package com.genesys.glideimageview

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException

interface OnLoadListener {

    fun onLoadStarted(view: GlideImageView) {}

    fun onResourceReady(view: GlideImageView, resource: Drawable, dataSource: DataSource) {}

    fun onLoadFailed(view: GlideImageView, error: GlideException?) {}

    fun onCleared(view: GlideImageView) {}
}
