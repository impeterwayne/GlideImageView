package com.genesys.glideimageview.sample

import com.genesys.glideimageview.sample.ext.Avatar

object SampleImages {

    const val REMOTE = "https://picsum.photos/seed/glideext/900/500"
    const val REMOTE_SQUARE = "https://picsum.photos/seed/glideext-square/500/500"
    const val BROKEN = "https://invalid.example.domain/broken_image.jpg"

    const val BANNER = "images/sample_banner.webp"
    const val AVATAR = "images/sample_avatar.webp"
    const val ICON = "images/sample_icon.webp"

    val ASSETS = listOf(BANNER, AVATAR, ICON)

    fun feed(size: Int): List<Any> = List(size) { index ->
        when (index % 3) {
            0 -> Avatar(userId = index)
            1 -> ASSETS[(index / 3) % ASSETS.size]
            else -> "https://picsum.photos/seed/feed-$index/200/200"
        }
    }
}
