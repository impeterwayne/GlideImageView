package com.genesys.glideimageview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.AttributeSet
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
import java.util.concurrent.CopyOnWriteArrayList

open class GlideImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.glideImageViewStyle
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
            if (value == null) applyNullSource() else reload()
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
        buildRequest(requestManager().load(resolved)).into(this)
    }

    open fun clear() {
        if (!isInEditMode) requestManager().clear(this)
        isSourceAssigned = false
        setImageDrawable(null)
        dispatch { it.onCleared(this) }
    }

    private fun applyNullSource() {
        clear()
        isSourceAssigned = true
        if (options.fallback != ImageOptions.NO_RESOURCE) setImageResource(options.fallback)
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

    protected open fun buildRequest(request: RequestBuilder<Drawable>): RequestBuilder<Drawable> {
        var result = request
        val current = options

        if (current.placeholder != ImageOptions.NO_RESOURCE) {
            result = result.placeholder(current.placeholder)
        }
        if (current.error != ImageOptions.NO_RESOURCE) result = result.error(current.error)
        if (current.fallback != ImageOptions.NO_RESOURCE) result = result.fallback(current.fallback)

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

        result = result.listener(glideListener)

        GlideImageViewConfig.decorators.forEach { result = it.decorate(this, result) }
        current.decorators.forEach { result = it.decorate(this, result) }
        return result
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
            is String -> runCatching {
                context.assets.open(DefaultModelResolver.assetPath(model)).use {
                    setImageBitmap(BitmapFactory.decodeStream(it))
                }
            }.onFailure { renderPreviewFallback() }

            else -> renderPreviewFallback()
        }
    }

    protected open fun renderPreviewFallback() {
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
            attrs, R.styleable.GlideImageView, defStyleAttr, R.style.Widget_GlideImageView
        )
        try {
            val declaresShape = typed.hasValue(R.styleable.GlideImageView_glideCircle) ||
                typed.hasValue(R.styleable.GlideImageView_glideRadius)

            options = defaults.copy(
                placeholder = typed.getResourceId(
                    R.styleable.GlideImageView_glidePlaceholder, defaults.placeholder
                ),
                error = typed.getResourceId(R.styleable.GlideImageView_glideError, defaults.error),
                fallback = typed.getResourceId(
                    R.styleable.GlideImageView_glideFallback, defaults.fallback
                ),
                crossFade = typed.getBoolean(
                    R.styleable.GlideImageView_glideCrossFade, defaults.crossFade
                ),
                crossFadeDurationMs = typed.getInt(
                    R.styleable.GlideImageView_glideCrossFadeDuration, defaults.crossFadeDurationMs
                ),
                shapes = if (declaresShape) xmlShapes(typed) else defaults.shapes
            )
            typed.getString(R.styleable.GlideImageView_glideSrc)?.let { source = it }
        } finally {
            typed.recycle()
        }
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
