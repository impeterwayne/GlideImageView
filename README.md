# GlideImageView

A Glide-backed `ImageView` for Android designed for declarative image loading in XML via `app:glideSrc`, featuring **instant Android Studio Layout Editor preview** for assets and zero-boilerplate loading. When dynamic runtime behavior or custom models are needed, it extends seamlessly in Kotlin without modifying library code.

```xml
<com.genesys.glideimageview.GlideImageView
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:scaleType="centerCrop"
    app:glideSrc="images/banner.webp"
    app:glidePlaceholder="@drawable/placeholder" />
```

---

## XML Attributes

| Attribute | Format | Description |
|---|---|---|
| `app:glideSrc` | `string` | **Primary feature:** asset path (`images/...`), `@string/...`, remote URL, file, or URI |
| `app:glidePlaceholder` | `reference` | Drawable resource shown while loading |
| `app:glideError` | `reference` | Drawable resource shown when the load fails |
| `app:glideFallback` | `reference` | Drawable resource shown when the source is `null` |
| `app:glideRadius` | `dimension` | Corner radius (adds `Shape.RoundedCorners`) |
| `app:glideCircle` | `boolean` | Circular crop (adds `Shape.Circle`) |
| `app:glideCrossFade` | `boolean` | Enables crossfade transition |
| `app:glideCrossFadeDuration` | `integer` | Crossfade duration in milliseconds (default: 300ms) |
| `?attr/glideImageViewStyle` | `reference` | Theme-level default style applied to all `GlideImageView`s |

---

## Feature Spotlight: `app:glideSrc` with Assets

The flagship feature of `GlideImageView` is `app:glideSrc`, bringing declarative, zero-boilerplate image loading straight into your layout files—with first-class support for asset sources.

### 1. Live Layout Editor Preview
Standard image loaders in Android leave views as blank grey or white boxes in Android Studio design mode.

With `app:glideSrc`, **asset paths render live inside the Layout Editor**:

```xml
<!-- Previews immediately in the Android Studio Layout Editor -->
<com.genesys.glideimageview.GlideImageView
    android:id="@+id/bannerImage"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:scaleType="centerCrop"
    app:glideSrc="images/sample_banner.webp"
    app:glidePlaceholder="@drawable/placeholder" />
```

- **Asset paths (`images/...`)**: Decoded directly from `src/main/assets/` during edit mode (`isInEditMode`), so you see actual design assets right inside Android Studio without running the app.
- **Resource strings (`@string/...`)**: Fully supported for referencing asset paths without hardcoding strings in layouts.
- **Remote URLs (`https://...`)**: Safely display `app:glidePlaceholder` during edit mode.

### 2. Zero Activity / Fragment Boilerplate
You no longer need to write `Glide.with(context).load(...).into(imageView)` in your Activity, Fragment, or ViewHolder just to display an image. Specifying `app:glideSrc` in XML automatically handles model resolution, request building, placeholder display, and lifecycle loading.

```xml
<!-- Defined cleanly in XML using strings.xml -->
<com.genesys.glideimageview.GlideImageView
    android:id="@+id/avatarImage"
    android:layout_width="80dp"
    android:layout_height="80dp"
    app:glideSrc="@string/sample_asset_avatar"
    app:glidePlaceholder="@drawable/avatar_placeholder" />
```

### 3. Supported Sources
While asset paths provide the best Layout Editor experience, `app:glideSrc` transparently accepts any source format:

| Format | Example | Layout Editor Preview |
|---|---|---|
| **Asset path** | `images/sample_banner.webp` | **Yes** (renders asset bitmap live) |
| **String resource** | `@string/sample_asset_banner` | **Yes** (resolves string to asset bitmap) |
| **Resource URI** | `android.resource://com.example.app/drawable/logo` | **Yes** (renders drawable resource) |
| **Remote URL** | `https://example.com/photo.jpg` | Shows placeholder |
| **Local file** | `/sdcard/photo.jpg` or `file:///storage/...` | Shows placeholder |
| **Content URI** | `content://media/external/images/media/1` | Shows placeholder |
| **Data URI** | `data:image/png;base64,...` | Shows placeholder |

### 4. Theme-Level Defaults
Configure standard placeholders and error states once in your application theme, keeping layout XML clean:

```xml
<style name="Theme.MyApp" parent="Theme.Material3.DayNight">
    <item name="glideImageViewStyle">@style/Widget.MyApp.GlideImageView</item>
</style>

<style name="Widget.MyApp.GlideImageView" parent="Widget.GlideImageView">
    <item name="glidePlaceholder">@drawable/placeholder</item>
    <item name="glideError">@drawable/error</item>
    <item name="glideFallback">@drawable/fallback</item>
</style>
```

---

## Programmatic Loading (Kotlin)

When sources need to change dynamically at runtime (e.g., in adapters or upon user interaction):

```kotlin
// Load an asset source (same engine as app:glideSrc)
image.loadAsset("images/banner.webp")

// Or load any model supported by Glide or custom ModelResolvers
image.load("https://example.com/photo.jpg") // URL
image.load(R.drawable.avatar)               // Drawable resource
image.load(Avatar(userId = 42))             // Domain model

// Clear load and reset view
image.clear()
```

---

## The Three Extension Points

Everything in the library is built on top of three composable seams, running in this order:

### 1. `ModelResolver` — what gets loaded

`source` is `Any?`, so anything Glide understands works unchanged. A resolver sits in front of it and can rewrite the source into a different model: add auth headers, teach the view a new scheme, or map a domain object onto a URL.

```kotlin
class AuthHeaderResolver(private val token: () -> String?) : ModelResolver {
    override fun resolve(context: Context, source: Any): Any? {
        if (source !is String || !source.startsWith("http")) return null // pass to the next one
        return GlideUrl(source, LazyHeaders.Builder()
            .addHeader("Authorization", "Bearer ${token() ?: return null}")
            .build())
    }
}

GlideImageViewConfig.modelResolvers += AuthHeaderResolver(tokenStore::current) // whole app
image.modelResolvers += AuthHeaderResolver(tokenStore::current)                // one view
```

**Resolution order:** Per-view resolvers → `GlideImageViewConfig.modelResolvers` → `DefaultModelResolver` (handles known URI schemes, `/sdcard/...` file paths, and relative asset paths).

### 2. `Shape` — how the bitmap is transformed

Shapes are a **list**, not a flag, so they compose. `appliesScaleType` controls whether the view's `ScaleType` is baked into the bitmap first—true for masks, false for effects and transformations that scale on their own.

```kotlin
data class SquircleShape(val curvature: Float = 4f) : Shape {
    override fun transformation(view: ImageView) = SquircleTransformation(curvature)
}

image.shapes = listOf(SquircleShape(), GrayscaleShape, BorderShape(3f, Color.BLUE))
image.shapes += Shape.of(BlurTransformation(12))   // wrap any Glide Transformation
```

Built in: `Shape.Original`, `Shape.Circle`, `Shape.RoundedCorners(px)`.
`ImageOptions.transformations` takes raw Glide transformations when you do not need a named shape.

### 3. `RequestDecorator` — everything else on the request

The escape hatch for what `ImageOptions` deliberately does not model: `diskCacheStrategy`, `signature`, `thumbnail`, `override`, `priority`, `format`, `timeout`.

```kotlin
GlideImageViewConfig.decorators += RequestDecorator { _, r -> r.diskCacheStrategy(ALL) }
image.decorators += RequestDecorator { v, r -> r.override(v.width, v.height) }
```

Global decorators run first, then the view's own.

---

## Configuration Precedence Order

| Precedence | Level | How | Scope |
|---|---|---|---|
| 1 (Lowest) | `GlideImageViewConfig.defaults` | `ImageOptions(...)` in `Application.onCreate` | Every view in the process |
| 2 | Theme style | `<item name="glideImageViewStyle">@style/...` | Every view using that theme |
| 3 | XML attributes | `app:glideSrc="…"`, `app:glidePlaceholder="…"` | One view |
| 4 (Highest) | Runtime code | `image.load(...)`, `image.updateOptions { ... }` | One view |

---

## Load Callbacks

Multiple listeners per view, each method optional, and the originating view is passed in so one listener can serve many views:

```kotlin
image.addOnLoadListener(object : OnLoadListener {
    override fun onLoadStarted(view: GlideImageView) { … }
    override fun onResourceReady(view: GlideImageView, resource: Drawable, dataSource: DataSource) { … }
    override fun onLoadFailed(view: GlideImageView, error: GlideException?) { … }
    override fun onCleared(view: GlideImageView) { … }
})

// Kotlin shorthand (returns listener for subsequent removal)
val listener = image.addOnLoadListener(onReady = { … }, onFailed = { … })

// Process-wide listener — for analytics, error telemetry, or performance funnels
GlideImageViewConfig.listeners += LoadStats
```

`onLoadStarted` fires when the request is submitted, before cache lookup, ensuring status indicators never need to be set optimistically by the caller.

---

## In a RecyclerView

```kotlin
override fun onBindViewHolder(holder: VH, position: Int) = holder.image.load(items[position])
override fun onViewRecycled(holder: VH) = holder.image.clear()
```

`clear()` cancels any in-flight request and resets the view state, ensuring re-binding recycled rows cleanly reloads rather than displaying stale bitmaps or leaving cells blank.

---

## Other Notes

- **Layout-Editor Preview:** Asset sources (`images/...`) and drawable resources render immediately in Android Studio; network sources fall back to the configured placeholder.
- **Swappable `Glide.with`:** Set `GlideImageViewConfig.requestManagerFactory` globally or `image.requestManagerFactory` per view.
- **Subclassing:** `resolveModel`, `buildRequest`, `buildTransformations`, `scaleTypeTransformation`, `renderPreview`, and `requestManager` are all `protected open`.

---

## Project Structure

```
GlideExt/
├── glideimageview/                     # Android library module
│   └── src/main/java/com/genesys/glideimageview/
│       ├── GlideImageView.kt           # the view & XML attribute handling
│       ├── ImageOptions.kt             # immutable render options
│       ├── Shape.kt                    # transformation seam
│       ├── ModelResolver.kt            # source seam + DefaultModelResolver
│       ├── RequestDecorator.kt         # raw-request seam
│       ├── OnLoadListener.kt           # callbacks
│       └── GlideImageViewConfig.kt     # process-wide configuration
└── sample/                             # demo app (5 tabs)
    └── src/main/java/com/genesys/glideimageview/sample/
        ├── SampleApp.kt                # installs app-wide configuration
        ├── ext/                        # custom shapes, resolvers, decorators
        └── demo/                       # XML Attributes · Shapes · List (1000) · Extensions · Playground
```

- **XML Attributes tab:** Demonstrates declarative XML image loading via `app:glideSrc` (direct asset paths with live layout editor preview, `@string/...` resources, circular & rounded shapes, remote URLs, and error/null fallbacks) with zero Kotlin `load()` calls.
- **Playground tab:** Live interactive test bench for XML attributes, `app:glideSrc`, corner radius slider, shapes, and scale types.
- **Extensions tab:** Domain models (`Avatar`), bearer-token auth headers, squircle mask, and decorators implemented outside the library module.
- **List (1000) tab:** 1,000 cells testing recycling, fast flings, request cancellation, and telemetry.

---

## Setup

```groovy
// settings.gradle
include ':glideimageview'

// app/build.gradle
dependencies {
    implementation project(':glideimageview')
}
```

```bash
./gradlew :glideimageview:assembleDebug   # build library AAR
./gradlew :sample:assembleDebug           # build sample APK
```

Outputs: `glideimageview/build/outputs/aar/` and `sample/build/outputs/apk/debug/`.
