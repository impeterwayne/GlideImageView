package com.genesys.glideimageview

import com.bumptech.glide.load.engine.DiskCacheStrategy

enum class CacheType(val strategy: DiskCacheStrategy) {
    ALL(DiskCacheStrategy.ALL),
    NONE(DiskCacheStrategy.NONE),
    DATA(DiskCacheStrategy.DATA),
    RESOURCE(DiskCacheStrategy.RESOURCE),
    AUTOMATIC(DiskCacheStrategy.AUTOMATIC);

    companion object {
        @JvmStatic
        fun fromId(id: Int): CacheType = when (id) {
            0 -> ALL
            1 -> NONE
            2, 5 -> DATA
            3, 6 -> RESOURCE
            4 -> AUTOMATIC
            else -> AUTOMATIC
        }
    }
}
