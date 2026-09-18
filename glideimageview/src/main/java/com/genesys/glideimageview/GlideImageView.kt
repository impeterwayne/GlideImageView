package com.genesys.glideimageview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.concurrent.CopyOnWriteArrayList

open class GlideImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var isReady = false

    private var lastScaleType: ScaleType? = null

    private var isSourceAssigned = false

    private val listeners = CopyOnWriteArrayList<OnLoadListener>()

    @JvmField
    val modelResolvers: MutableList<ModelResolver> = CopyOnWriteArrayList()

    var requestManagerFactory: RequestManagerFactory? = null

    var options: ImageOptions = GlideImageViewConfig.defaults
        set(value) {
            if (field == value) return
            field = value
            reload()
        }

    var source: Any? = null
        set(value) {
            if (isSourceAssigned && field == value) return
            isSourceAssigned = true
            field = value
            if (value == null) {
                clear()
                isSourceAssigned = true
            } else {
                reload()
            }
        }

    var shapes: List<Shape>
        get() = options.shapes
        set(value) {
            options = options.copy(shapes = value)
        }

    var transformations: List<Transformation<Bitmap>>
        get() = options.transformations
        set(value) {
            options = options.copy(transformations = value)
        }

    var decorators: List<RequestDecorator>
        get() = options.decorators
        set(value) {
            options = options.copy(decorators = value)
        }

    var cacheType: CacheType?
        get() = options.cacheType
        set(value) {
            options = options.copy(cacheType = value)
        }

    var skipMemoryCache: Boolean
        get() = options.skipMemoryCache
        set(value) {
            options = options.copy(skipMemoryCache = value)
        }

    var signature: Key?
        get() = options.signature
        set(value) {
            options = options.copy(signature = value)
        }

    private val glideListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>,
            isFirstResource: Boolean
        ): Boolean {
            dispatch { it.onLoadFailed(this@GlideImageView, e) }
            return false
        }

        override fun onResourceReady(
            resource: Drawable,
            model: Any,
            target: Target<Drawable>?,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            dispatch { it.onResourceReady(this@GlideImageView, resource, dataSource) }
            return false
        }
    }

    init {
        isReady = true
        lastScaleType = scaleType
        applyAttributes(attrs, defStyleAttr)
    }

    fun load(model: Any?) {
        source = model
    }

    fun load(@DrawableRes resourceId: Int) {
        source = resourceId
    }

    fun loadAsset(assetPath: String?) {
        source = assetPath?.let { DefaultModelResolver.assetUri(it) }
    }

    fun updateOptions(block: ImageOptions.() -> ImageOptions) {
        options = options.block()
    }

    fun addOnLoadListener(listener: OnLoadListener) {
        listeners.addIfAbsent(listener)
    }

    fun addOnLoadListener(
        onStarted: (() -> Unit)? = null,
        onReady: ((Drawable) -> Unit)? = null,
        onFailed: ((GlideException?) -> Unit)? = null,
        onCleared: (() -> Unit)? = null
    ): OnLoadListener = object : OnLoadListener {
        override fun onLoadStarted(view: GlideImageView) {
            onStarted?.invoke()
        }

        override fun onResourceReady(
            view: GlideImageView,
            resource: Drawable,
            dataSource: DataSource
        ) {
            onReady?.invoke(resource)
        }

        override fun onLoadFailed(view: GlideImageView, error: GlideException?) {
            onFailed?.invoke(error)
        }

        override fun onCleared(view: GlideImageView) {
            onCleared?.invoke()
        }
    }.also(::addOnLoadListener)

    fun removeOnLoadListener(listener: OnLoadListener) {
        listeners.remove(listener)
    }

    fun clearOnLoadListeners() {
        listeners.clear()
    }

    fun reload() {
        if (!isReady) return
        val model = source ?: return
        if (isInEditMode) {
            renderPreview(model)
            return
        }
        val resolved = resolveModel(model)
        if (resolved == null) {
            clear()
            return
        }
        dispatch { it.onLoadStarted(this) }
        buildRequest(requestManager().load(resolved), resolved).into(this)
    }

    open fun clear() {
        if (!isInEditMode) requestManager().clear(this)
        isSourceAssigned = false
        setImageDrawable(null)
        dispatch { it.onCleared(this) }
    }

    protected open fun requestManager(): RequestManager =
        (requestManagerFactory ?: GlideImageViewConfig.requestManagerFactory).create(this)

    protected open fun resolveModel(source: Any): Any? {
        modelResolvers.forEach { resolver -> resolver.resolve(context, source)?.let { return it } }
        GlideImageViewConfig.modelResolvers.forEach { resolver ->
            resolver.resolve(context, source)?.let { return it }
        }
        return DefaultModelResolver.resolve(context, source)
    }

    protected open fun buildRequest(
        request: RequestBuilder<Drawable>,
        model: Any? = null
    ): RequestBuilder<Drawable> {
        var result = request
        val current = options

        if (current.placeholder != ImageOptions.NO_RESOURCE) {
            result = result.placeholder(current.placeholder)
        }
        if (current.error != ImageOptions.NO_RESOURCE) result = result.error(current.error)

        val transforms = buildTransformations()
        result = when (transforms.size) {
            0 -> result
            1 -> result.transform(transforms.first())
            else -> result.transform(MultiTransformation(transforms))
        }

        result = if (current.crossFade) {
            val factory = DrawableCrossFadeFactory.Builder(current.crossFadeDurationMs)
                .setCrossFadeEnabled(true)
                .build()
            result.transition(DrawableTransitionOptions.withCrossFade(factory))
        } else {
            result.dontAnimate()
        }

        current.signature?.let { result = result.signature(it) }

        val explicitStrategy = current.cacheType?.strategy
        val bypassCache = explicitStrategy == null && isLocalResource(model)

        val diskStrategy = explicitStrategy ?: DiskCacheStrategy.NONE.takeIf { bypassCache }
        if (diskStrategy != null) {
            result = result.diskCacheStrategy(diskStrategy)
        }
        if (current.skipMemoryCache || bypassCache) {
            result = result.skipMemoryCache(true)
        }

        GlideImageViewConfig.decorators.forEach { result = it.decorate(this, result) }
        current.decorators.forEach { result = it.decorate(this, result) }

        return result.addListener(glideListener)
    }

    protected open fun isLocalResource(model: Any?): Boolean = when (model) {
        is Int -> model != ImageOptions.NO_RESOURCE
        is Drawable -> true
        else -> model?.toString().orEmpty().startsWith("android.resource://", ignoreCase = true)
    }

    protected open fun buildTransformations(): List<Transformation<Bitmap>> {
        val contributed = options.shapes.mapNotNull { shape ->
            shape.transformation(this)?.let { shape to it }
        }
        if (contributed.isEmpty() && options.transformations.isEmpty()) return emptyList()

        return buildList {
            if (contributed.any { it.first.appliesScaleType }) {
                scaleTypeTransformation()?.let(::add)
            }
            contributed.forEach { add(it.second) }
            addAll(options.transformations)
        }
    }

    protected open fun scaleTypeTransformation(): Transformation<Bitmap>? = when (scaleType) {
        ScaleType.CENTER_CROP -> CenterCrop()
        ScaleType.FIT_CENTER, ScaleType.FIT_START, ScaleType.FIT_END -> FitCenter()
        ScaleType.CENTER_INSIDE -> CenterInside()
        else -> null
    }

    protected open fun renderPreview(model: Any) {
        when (model) {
            is Int -> setImageResource(model)
            is Drawable -> setImageDrawable(model)
            is String -> {
                if (model.startsWith("@drawable/") || model.startsWith("@mipmap/")) {
                    val defType = if (model.startsWith("@mipmap/")) "mipmap" else "drawable"
                    val name = model.substringAfter('/')
                    val resId = runCatching {
                        context.resources.getIdentifier(name, defType, context.packageName)
                    }.getOrDefault(0)
                    if (resId != 0) {
                        setImageResource(resId)
                        return
                    }
                }
                runCatching {
                    context.assets.open(DefaultModelResolver.assetPath(model)).use {
                        setImageBitmap(BitmapFactory.decodeStream(it))
                    }
                }.onFailure { renderPreviewPlaceholder() }
            }

            else -> renderPreviewPlaceholder()
        }
    }

    protected open fun renderPreviewPlaceholder() {
        if (options.placeholder != ImageOptions.NO_RESOURCE) setImageResource(options.placeholder)
    }

    override fun setScaleType(scaleType: ScaleType) {
        val changed = isReady && scaleType != lastScaleType
        lastScaleType = scaleType
        super.setScaleType(scaleType)
        if (changed && options.shapes.any { it.appliesScaleType && it.transformation(this) != null }) {
            reload()
        }
    }

    private fun applyAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        val defaults = options
        val typed = context.obtainStyledAttributes(
            attrs, R.styleable.GlideImageView, defStyleAttr, 0
        )
        try {
            val declaresShape = typed.hasValue(R.styleable.GlideImageView_glideCircle) ||
                typed.hasValue(R.styleable.GlideImageView_glideRadius)

            val cacheTypeId = typed.getInt(R.styleable.GlideImageView_glideCacheType, -1)
            val parsedCacheType =
                if (cacheTypeId >= 0) CacheType.fromId(cacheTypeId) else defaults.cacheType

            val skipMemory = typed.getBoolean(
                R.styleable.GlideImageView_glideSkipMemoryCache,
                defaults.skipMemoryCache
            )

            options = defaults.copy(
                placeholder = typed.getResourceId(
                    R.styleable.GlideImageView_glidePlaceholder, defaults.placeholder
                ),
                error = typed.getResourceId(R.styleable.GlideImageView_glideError, defaults.error),
                crossFade = typed.getBoolean(
                    R.styleable.GlideImageView_glideCrossFade, defaults.crossFade
                ),
                crossFadeDurationMs = typed.getInt(
                    R.styleable.GlideImageView_glideCrossFadeDuration, defaults.crossFadeDurationMs
                ),
                shapes = if (declaresShape) xmlShapes(typed) else defaults.shapes,
                cacheType = parsedCacheType,
                skipMemoryCache = skipMemory
            )
            resolveXmlSource(typed)?.let { source = it }
        } finally {
            typed.recycle()
        }
    }

    private fun resolveXmlSource(typed: TypedArray): Any? {
        val attrId = R.styleable.GlideImageView_glideSrc
        if (!typed.hasValue(attrId)) return null

        if (isInEditMode) {
            runCatching { typed.getDrawable(attrId) }.getOrNull()?.let {
                return it
            }
        }

        val drawable = runCatching { typed.getDrawable(attrId) }.getOrNull()
        val resId = typed.getResourceId(attrId, 0)
        if (drawable != null && resId != 0) {
            return resId
        }

        if (resId != 0) {
            val typeName = runCatching { context.resources.getResourceTypeName(resId) }.getOrNull()
            if (typeName == "drawable" || typeName == "mipmap") {
                return resId
            }
        }

        val rawString = typed.getString(attrId) ?: return null
        if (rawString.startsWith("@drawable/") || rawString.startsWith("@mipmap/")) {
            val defType = if (rawString.startsWith("@mipmap/")) "mipmap" else "drawable"
            val name = rawString.substringAfter('/')
            val id = runCatching { context.resources.getIdentifier(name, defType, context.packageName) }.getOrDefault(0)
            if (id != 0) return id
        }

        return rawString
    }

    private fun xmlShapes(typed: TypedArray): List<Shape> = buildList {
        if (typed.getBoolean(R.styleable.GlideImageView_glideCircle, false)) add(Shape.Circle)
        val radius = typed.getDimensionPixelSize(R.styleable.GlideImageView_glideRadius, 0)
        if (radius > 0) add(Shape.RoundedCorners(radius))
    }

    private inline fun dispatch(block: (OnLoadListener) -> Unit) {
        listeners.forEach(block)
        GlideImageViewConfig.listeners.forEach(block)
    }

    companion object {
        @JvmStatic
        fun assetUri(path: String): String = DefaultModelResolver.assetUri(path)
    }
}
