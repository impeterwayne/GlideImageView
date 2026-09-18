package com.genesys.glideimageview

import android.content.Context
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GlideImageViewTest {

    private lateinit var context: Context
    private lateinit var imageView: GlideImageView

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        imageView = GlideImageView(context)
    }

    @Test
    fun testDefaultCacheOptions() {
        assertNull(imageView.cacheType)
        assertFalse(imageView.skipMemoryCache)
        assertNull(imageView.signature)
    }

    @Test
    fun testCacheTypeProperty() {
        imageView.cacheType = CacheType.NONE
        assertEquals(CacheType.NONE, imageView.cacheType)
        assertEquals(CacheType.NONE, imageView.options.cacheType)
        assertEquals(DiskCacheStrategy.NONE, imageView.cacheType?.strategy)

        imageView.cacheType = CacheType.ALL
        assertEquals(CacheType.ALL, imageView.cacheType)
        assertEquals(DiskCacheStrategy.ALL, imageView.cacheType?.strategy)
    }

    @Test
    fun testSkipMemoryCacheProperty() {
        imageView.skipMemoryCache = true
        assertTrue(imageView.skipMemoryCache)
        assertTrue(imageView.options.skipMemoryCache)

        imageView.skipMemoryCache = false
        assertFalse(imageView.skipMemoryCache)
        assertFalse(imageView.options.skipMemoryCache)
    }

    @Test
    fun testSignatureProperty() {
        val customKey = ObjectKey("custom_test_key")
        imageView.signature = customKey
        assertEquals(customKey, imageView.signature)
        assertEquals(customKey, imageView.options.signature)
    }

    @Test
    fun testXmlAttributesInflation() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(R.attr.glideCacheType, "none")
            .addAttribute(R.attr.glideSkipMemoryCache, "true")
            .addAttribute(R.attr.glideSrc, "@android:drawable/ic_menu_camera")
            .build()

        val view = GlideImageView(context, attrs)
        assertEquals(CacheType.NONE, view.cacheType)
        assertTrue(view.skipMemoryCache)
    }


    @Test
    fun testAllCacheTypeEnumsInXml() {
        val expectations = listOf(
            "all" to CacheType.ALL,
            "none" to CacheType.NONE,
            "data" to CacheType.DATA,
            "resource" to CacheType.RESOURCE,
            "automatic" to CacheType.AUTOMATIC
        )
        for ((enumStr, expectedType) in expectations) {
            val attrs = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.glideCacheType, enumStr)
                .build()
            val view = GlideImageView(context, attrs)
            assertEquals("Failed for enum $enumStr", expectedType, view.cacheType)
            assertEquals(
                "Failed strategy for enum $enumStr",
                expectedType.strategy,
                view.cacheType?.strategy
            )
        }
    }


    @Test
    fun testSkipMemoryCacheAttributeInXml() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(R.attr.glideSkipMemoryCache, "true")
            .build()
        val view = GlideImageView(context, attrs)
        assertTrue(view.skipMemoryCache)
    }
}
