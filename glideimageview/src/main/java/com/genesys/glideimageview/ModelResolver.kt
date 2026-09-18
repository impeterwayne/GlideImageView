package com.genesys.glideimageview

import android.content.Context
import java.io.File

fun interface ModelResolver {
    fun resolve(context: Context, source: Any): Any?
}

object DefaultModelResolver : ModelResolver {

    const val ASSET_SCHEME: String = "file:///android_asset/"

    private val KNOWN_SCHEMES = listOf(
        "http://", "https://", "content://", "file://", "android.resource://", "data:"
    )

    override fun resolve(context: Context, source: Any): Any? = when {
        source !is String -> source
        source.isBlank() -> null
        source.startsWith("@drawable/") || source.startsWith("@mipmap/") -> {
            val defType = if (source.startsWith("@mipmap/")) "mipmap" else "drawable"
            val name = source.substringAfter('/')
            val resId = context.resources.getIdentifier(name, defType, context.packageName)
            if (resId != 0) resId else null
        }
        KNOWN_SCHEMES.any { source.startsWith(it, ignoreCase = true) } -> source
        source.startsWith('/') -> File(source)
        else -> assetUri(source)
    }

    @JvmStatic
    fun assetUri(path: String): String = ASSET_SCHEME + path.trimStart('/')

    @JvmStatic
    fun assetPath(uri: String): String = uri.removePrefix(ASSET_SCHEME).trimStart('/')
}
