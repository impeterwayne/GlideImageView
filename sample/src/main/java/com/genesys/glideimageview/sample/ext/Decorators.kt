package com.genesys.glideimageview.sample.ext

import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.genesys.glideimageview.RequestDecorator

fun thumbnailDecorator(scale: Float = 0.1f) = RequestDecorator { _, request ->
    request.thumbnail(request.clone().sizeMultiplier(scale).listener(null))
}

val DiskCacheDecorator = RequestDecorator { _, request ->
    request.diskCacheStrategy(DiskCacheStrategy.ALL)
}

val ExactSizeDecorator = RequestDecorator { view, request ->
    if (view.width > 0 && view.height > 0) request.override(view.width, view.height) else request
}

fun priorityDecorator(priority: Priority) = RequestDecorator { _, request ->
    request.priority(priority)
}
