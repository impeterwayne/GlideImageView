package com.genesys.glideimageview

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Pins the request-building contract of [GlideImageView.buildRequest]: `ImageOptions` is applied
 * first, then `GlideImageViewConfig.decorators`, then the per-view `decorators` — last write wins —
 * with the internal load listener attached after everything else.
 *
 * The view's [RequestManagerFactory] is replaced with a mock so the real load path runs end to end
 * without booting Glide. `RETURNS_SELF` keeps the builder chain on a single mock, so every call made
 * while building the request is recorded in order.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RequestBuildOrderTest {

    private lateinit var context: Context
    private lateinit var imageView: GlideImageView
    private lateinit var request: RequestBuilder<Drawable>

    @Before
    fun setUp() {
        GlideImageViewConfig.reset()
        context = RuntimeEnvironment.getApplication()

        request = mock(defaultAnswer = Answers.RETURNS_SELF)
        val manager = mock<RequestManager>()
        whenever(manager.load(any<Any>())).thenReturn(request)

        imageView = GlideImageView(context)
        imageView.requestManagerFactory = RequestManagerFactory { manager }
    }

    @After
    fun tearDown() {
        GlideImageViewConfig.reset()
    }

    /** Assigning a source is what triggers `reload()` -> `buildRequest()`. */
    private fun load() {
        imageView.source = android.R.drawable.ic_menu_camera
    }

    private fun cacheDecorator(strategy: DiskCacheStrategy) =
        RequestDecorator { _, request -> request.diskCacheStrategy(strategy) }

    // region ImageOptions is applied exactly once

    @Test
    fun cacheStrategyFromOptionsIsAppliedOnce() {
        imageView.cacheType = CacheType.NONE

        load()

        verify(request, times(1)).diskCacheStrategy(DiskCacheStrategy.NONE)
    }

    @Test
    fun skipMemoryCacheFromOptionsIsAppliedOnce() {
        imageView.skipMemoryCache = true

        load()

        verify(request, times(1)).skipMemoryCache(true)
    }

    // endregion

    // region Decorator precedence

    @Test
    fun globalDecoratorOverridesCacheStrategyFromOptions() {
        imageView.cacheType = CacheType.NONE
        GlideImageViewConfig.decorators += cacheDecorator(DiskCacheStrategy.ALL)

        load()

        val order = inOrder(request)
        order.verify(request).diskCacheStrategy(DiskCacheStrategy.NONE)
        order.verify(request).diskCacheStrategy(DiskCacheStrategy.ALL)
        // The options value must not be re-applied after the decorator has spoken.
        verify(request, times(1)).diskCacheStrategy(DiskCacheStrategy.NONE)
    }

    @Test
    fun perViewDecoratorOverridesCacheStrategyFromOptions() {
        imageView.cacheType = CacheType.NONE
        imageView.decorators = listOf(cacheDecorator(DiskCacheStrategy.ALL))

        load()

        val order = inOrder(request)
        order.verify(request).diskCacheStrategy(DiskCacheStrategy.NONE)
        order.verify(request).diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    @Test
    fun perViewDecoratorsRunAfterGlobalDecorators() {
        GlideImageViewConfig.decorators += cacheDecorator(DiskCacheStrategy.DATA)
        imageView.decorators = listOf(cacheDecorator(DiskCacheStrategy.RESOURCE))

        load()

        val order = inOrder(request)
        order.verify(request).diskCacheStrategy(DiskCacheStrategy.DATA)
        order.verify(request).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
    }

    @Test
    fun decoratorsRunInRegistrationOrder() {
        GlideImageViewConfig.decorators += cacheDecorator(DiskCacheStrategy.DATA)
        GlideImageViewConfig.decorators += cacheDecorator(DiskCacheStrategy.RESOURCE)

        load()

        val order = inOrder(request)
        order.verify(request).diskCacheStrategy(DiskCacheStrategy.DATA)
        order.verify(request).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
    }

    // endregion

    // region Load listener

    @Test
    fun loadListenerIsAttachedAfterAllDecorators() {
        GlideImageViewConfig.decorators += cacheDecorator(DiskCacheStrategy.DATA)
        imageView.decorators = listOf(cacheDecorator(DiskCacheStrategy.RESOURCE))

        load()

        val order = inOrder(request)
        order.verify(request).diskCacheStrategy(DiskCacheStrategy.DATA)
        order.verify(request).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        order.verify(request).addListener(anyOrNull())
    }

    @Test
    fun loadListenerUsesAddListenerSoDecoratorListenersSurvive() {
        load()

        // listener() clears Glide's listener list; addListener() appends to it.
        verify(request, never()).listener(anyOrNull())
        verify(request, times(1)).addListener(anyOrNull())
    }

    // endregion

    // region Local resources bypass the cache by default

    @Test
    fun drawableResourceBypassesDiskAndMemoryCacheByDefault() {
        load()

        verify(request, times(1)).diskCacheStrategy(DiskCacheStrategy.NONE)
        verify(request, times(1)).skipMemoryCache(true)
    }

    @Test
    fun remoteModelKeepsGlideDefaultsWhenNoCacheOptionIsSet() {
        imageView.source = "https://example.com/a.png"

        verify(request, never()).diskCacheStrategy(any())
        verify(request, never()).skipMemoryCache(any())
    }

    @Test
    fun explicitCacheTypeWinsOverTheResourceBypass() {
        imageView.cacheType = CacheType.ALL

        load()

        verify(request, times(1)).diskCacheStrategy(DiskCacheStrategy.ALL)
        verify(request, never()).diskCacheStrategy(DiskCacheStrategy.NONE)
        verify(request, never()).skipMemoryCache(any())
    }

    @Test
    fun explicitResourceCacheTypeWinsOverTheResourceBypass() {
        imageView.cacheType = CacheType.RESOURCE

        load()

        verify(request, times(1)).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        verify(request, never()).diskCacheStrategy(DiskCacheStrategy.NONE)
        verify(request, never()).skipMemoryCache(any())
    }

    // endregion

    // region Signatures

    @Test
    fun noSignatureIsAppliedByDefault() {
        load()

        verify(request, never()).signature(any<Key>())
    }

    @Test
    fun explicitSignatureIsApplied() {
        imageView.signature = ObjectKey("v2")

        load()

        verify(request, times(1)).signature(ObjectKey("v2"))
    }

    // endregion
}
