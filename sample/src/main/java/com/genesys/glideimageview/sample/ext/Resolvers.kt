package com.genesys.glideimageview.sample.ext

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.genesys.glideimageview.ModelResolver

data class Avatar(val userId: Int, val sizePx: Int = 200)

object AvatarResolver : ModelResolver {
    override fun resolve(context: Context, source: Any): Any? {
        if (source !is Avatar) return null
        return "https://picsum.photos/seed/user-${source.userId}/${source.sizePx}/${source.sizePx}"
    }
}

class AuthHeaderResolver(
    private val tokenProvider: () -> String?
) : ModelResolver {

    override fun resolve(context: Context, source: Any): Any? {
        if (source !is String || !source.startsWith("http")) return null
        val token = tokenProvider() ?: return null
        val headers = LazyHeaders.Builder()
            .addHeader("Authorization", "Bearer $token")
            .addHeader("X-Client", "glideimageview-sample")
            .build()
        return GlideUrl(source, headers)
    }
}
