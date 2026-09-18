package com.genesys.glideimageview.sample.ext

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.genesys.glideimageview.GlideImageView
import com.genesys.glideimageview.OnLoadListener
import java.util.concurrent.atomic.AtomicInteger

object LoadStats : OnLoadListener {

    val started = AtomicInteger()
    val succeeded = AtomicInteger()
    val failed = AtomicInteger()
    val fromCache = AtomicInteger()

    private val observers = mutableListOf<() -> Unit>()

    override fun onLoadStarted(view: GlideImageView) {
        started.incrementAndGet()
        notifyObservers()
    }

    override fun onResourceReady(
        view: GlideImageView,
        resource: Drawable,
        dataSource: DataSource
    ) {
        succeeded.incrementAndGet()
        if (dataSource != DataSource.REMOTE) fromCache.incrementAndGet()
        notifyObservers()
    }

    override fun onLoadFailed(view: GlideImageView, error: GlideException?) {
        failed.incrementAndGet()
        notifyObservers()
    }

    fun observe(onChange: () -> Unit) {
        observers += onChange
        onChange()
    }

    fun stopObserving(onChange: () -> Unit) {
        observers -= onChange
    }

    fun summary(): String =
        "started ${started.get()} · ok ${succeeded.get()} · failed ${failed.get()} · " +
            "served from cache ${fromCache.get()}"

    private fun notifyObservers() {
        observers.toList().forEach { it() }
    }
}
