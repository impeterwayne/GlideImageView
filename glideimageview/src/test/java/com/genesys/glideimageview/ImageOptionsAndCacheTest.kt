package com.genesys.glideimageview

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageOptionsAndCacheTest {

    @Test
    fun testCacheTypeFromId() {
        assertEquals(CacheType.ALL, CacheType.fromId(0))
        assertEquals(CacheType.NONE, CacheType.fromId(1))
        assertEquals(CacheType.DATA, CacheType.fromId(2))
        assertEquals(CacheType.RESOURCE, CacheType.fromId(3))
        assertEquals(CacheType.AUTOMATIC, CacheType.fromId(4))
        assertEquals(CacheType.DATA, CacheType.fromId(5))
        assertEquals(CacheType.RESOURCE, CacheType.fromId(6))
        assertEquals(CacheType.AUTOMATIC, CacheType.fromId(99))
    }

    @Test
    fun testCacheTypeStrategies() {
        assertEquals(DiskCacheStrategy.ALL, CacheType.ALL.strategy)
        assertEquals(DiskCacheStrategy.NONE, CacheType.NONE.strategy)
        assertEquals(DiskCacheStrategy.DATA, CacheType.DATA.strategy)
        assertEquals(DiskCacheStrategy.RESOURCE, CacheType.RESOURCE.strategy)
        assertEquals(DiskCacheStrategy.AUTOMATIC, CacheType.AUTOMATIC.strategy)
    }

    @Test
    fun testImageOptionsDefaults() {
        val options = ImageOptions()
        assertNull(options.cacheType)
        assertFalse(options.skipMemoryCache)
        assertNull(options.signature)
    }

    @Test
    fun testImageOptionsWithMethods() {
        val key = ObjectKey("my_custom_key")
        val options = ImageOptions()
            .withCacheType(CacheType.NONE)
            .withSkipMemoryCache(true)
            .withSignature(key)

        assertEquals(CacheType.NONE, options.cacheType)
        assertTrue(options.skipMemoryCache)
        assertEquals(key, options.signature)

        val updatedType = options.withCacheType(CacheType.DATA)
        assertEquals(CacheType.DATA, updatedType.cacheType)
        assertEquals(DiskCacheStrategy.DATA, updatedType.cacheType?.strategy)

        val resetMemory = updatedType.withSkipMemoryCache(false)
        assertFalse(resetMemory.skipMemoryCache)
    }
}
