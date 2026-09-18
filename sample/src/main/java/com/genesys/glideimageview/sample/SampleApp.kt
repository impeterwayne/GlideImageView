package com.genesys.glideimageview.sample

import android.app.Application
import com.genesys.glideimageview.GlideImageViewConfig
import com.genesys.glideimageview.ImageOptions
import com.genesys.glideimageview.sample.ext.AvatarResolver
import com.genesys.glideimageview.sample.ext.DiskCacheDecorator
import com.genesys.glideimageview.sample.ext.LoadStats

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        GlideImageViewConfig.defaults = ImageOptions(
            placeholder = R.drawable.placeholder_image,
            error = R.drawable.error_image,
            crossFade = true
        )

        GlideImageViewConfig.modelResolvers += AvatarResolver

        GlideImageViewConfig.decorators += DiskCacheDecorator

        GlideImageViewConfig.listeners += LoadStats
    }
}
