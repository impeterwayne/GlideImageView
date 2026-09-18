package com.genesys.glideimageview

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import java.util.concurrent.CopyOnWriteArrayList

fun interface RequestManagerFactory {
    fun create(view: View): RequestManager

    companion object {
        @JvmField
        val Default: RequestManagerFactory = RequestManagerFactory { Glide.with(it) }
    }
}

object GlideImageViewConfig {

    @JvmStatic
    @Volatile
    var defaults: ImageOptions = ImageOptions()

    @JvmField
    val modelResolvers: MutableList<ModelResolver> = CopyOnWriteArrayList()

    @JvmField
    val decorators: MutableList<RequestDecorator> = CopyOnWriteArrayList()

    @JvmField
    val listeners: MutableList<OnLoadListener> = CopyOnWriteArrayList()

    @JvmStatic
    @Volatile
    var requestManagerFactory: RequestManagerFactory = RequestManagerFactory.Default

    @JvmStatic
    fun reset() {
        defaults = ImageOptions()
        modelResolvers.clear()
        decorators.clear()
        listeners.clear()
        requestManagerFactory = RequestManagerFactory.Default
    }
}
