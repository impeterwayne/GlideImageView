# GlideExt — GlideImageView

A Glide-backed `ImageView` for Android, built so the things you will need next can be added
**without editing the library**.

```kotlin
image.load("images/banner.webp")        // asset, previews in the layout editor
image.load("https://example.com/a.jpg") // URL
image.load(R.drawable.avatar)           // drawable resource
image.load(Avatar(userId = 42))         // your own model type
image.shapes = listOf(Shape.Circle, GrayscaleShape)
image.decorators += RequestDecorator { _, r -> r.diskCacheStrategy(ALL) }
```

---

## The three extension points

Everything else in the library is built on top of these, and they run in this order.

### 1. `ModelResolver` — what gets loaded

`source` is `Any?`, so anything Glide understands works unchanged. A resolver sits in front of it
and can rewrite the source into a different model: add auth headers, teach the view a new scheme,
or map a domain object onto a URL.

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

Order: per-view resolvers → `GlideImageViewConfig.modelResolvers` → `DefaultModelResolver`, which
handles known URI schemes, absolute paths (`/sdcard/…` → `File`) and, as the fallback, a path
relative to `src/main/assets`.

### 2. `Shape` — how the bitmap is transformed

Shapes are a **list**, not a flag, so they compose. `appliesScaleType` says whether the shape needs
the view's `ScaleType` baked into the bitmap first — true for masks, false for effects and for
transformations that scale on their own.

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

The escape hatch for what `ImageOptions` deliberately does not model: `diskCacheStrategy`,
`signature`, `thumbnail`, `override`, `priority`, `format`, `timeout`.

```kotlin
GlideImageViewConfig.decorators += RequestDecorator { _, r -> r.diskCacheStrategy(ALL) }
image.decorators += RequestDecorator { v, r -> r.override(v.width, v.height) }
```

Global decorators run first, then the view's own.

---

## Configuration, in precedence order

| Level | How | Scope |
|---|---|---|
| `GlideImageViewConfig.defaults` | `ImageOptions(...)` in `Application.onCreate` | every view in the process |
| Theme style | `<item name="glideImageViewStyle">@style/Widget.MyApp.Image</item>` | every view using that theme |
| XML attributes | `app:glidePlaceholder="…"` | one view |
| Runtime | `image.updateOptions { copy(...) }` | one view |

```xml
<style name="Theme.MyApp" parent="Theme.Material3.DayNight">
    <item name="glideImageViewStyle">@style/Widget.MyApp.Image</item>
</style>

<style name="Widget.MyApp.Image" parent="Widget.GlideImageView">
    <item name="glidePlaceholder">@drawable/placeholder</item>
    <item name="glideError">@drawable/error</item>
    <item name="glideCrossFade">true</item>
</style>
```

### XML attributes

| Attribute | Format | Description |
|---|---|---|
| `app:glideSrc` | `string` | Asset path (`images/a.webp`), absolute path, or a URI (`http`, `https`, `content`, `file`, `android.resource`, `data`) |
| `app:glidePlaceholder` | `reference` | Shown while loading |
| `app:glideError` | `reference` | Shown when the load fails |
| `app:glideFallback` | `reference` | Shown when the source is `null` |
| `app:glideRadius` | `dimension` | Adds `Shape.RoundedCorners` |
| `app:glideCircle` | `boolean` | Adds `Shape.Circle` |
| `app:glideCrossFade` | `boolean` | Crossfade transition |
| `app:glideCrossFadeDuration` | `integer` | Crossfade duration in ms |
| `?attr/glideImageViewStyle` | `reference` | Theme-level default style for every `GlideImageView` |

---

## Load callbacks

Multiple listeners per view, each method optional, and the originating view is passed in so one
listener can serve many views.

```kotlin
image.addOnLoadListener(object : OnLoadListener {
    override fun onLoadStarted(view: GlideImageView) { … }
    override fun onResourceReady(view: GlideImageView, resource: Drawable, dataSource: DataSource) { … }
    override fun onLoadFailed(view: GlideImageView, error: GlideException?) { … }
    override fun onCleared(view: GlideImageView) { … }
})

// Kotlin shorthand; returns the listener so it can be removed later
val listener = image.addOnLoadListener(onReady = { … }, onFailed = { … })

// One listener for every load in the app — analytics, error funnels
GlideImageViewConfig.listeners += LoadStats
```

`onLoadStarted` fires when the request is submitted, before any cache lookup, so a status label
never has to be set optimistically by the caller.

---

## In a RecyclerView

```kotlin
override fun onBindViewHolder(holder: VH, position: Int) = holder.image.load(items[position])
override fun onViewRecycled(holder: VH) = holder.image.clear()
```

`clear()` cancels the in-flight request and marks the view unloaded, so re-binding the same model
into a recycled row reloads it rather than leaving the cell blank.

---

## Other notes

- **Layout-editor preview.** Asset sources and drawable resources render in the IDE; anything else
  falls back to the placeholder.
- **`Glide.with` is swappable.** `GlideImageViewConfig.requestManagerFactory`, or
  `image.requestManagerFactory` per view.
- **Subclassing still works** — `resolveModel`, `buildRequest`, `buildTransformations`,
  `scaleTypeTransformation`, `renderPreview` and `requestManager` are all `protected open`. It is
  rarely what you want: a subclass has to be named in every layout file, while the seams above also
  reach views you do not own.

---

## Project structure

```
GlideExt/
├── glideimageview/                     # Android library
│   └── src/main/java/com/genesys/glideimageview/
│       ├── GlideImageView.kt           # the view
│       ├── ImageOptions.kt             # immutable render options
│       ├── Shape.kt                    # transformation seam
│       ├── ModelResolver.kt            # source seam + DefaultModelResolver
│       ├── RequestDecorator.kt         # raw-request seam
│       ├── OnLoadListener.kt           # callbacks
│       └── GlideImageViewConfig.kt     # process-wide configuration
└── sample/                             # demo app, five tabs
    └── src/main/java/com/genesys/glideimageview/sample/
        ├── SampleApp.kt                # installs the app-wide configuration
        ├── ext/                        # custom shapes, resolvers, decorators
        └── demo/                       # Basics · Shapes · List (1000) · Extensions · Playground
```

The **Extensions** tab is the one worth reading: every case on it — an
`Avatar` domain model, bearer-token headers, a squircle mask, thumbnail/override/priority
decorators, a different `RequestManager` — is implemented in the sample module. The library knows
about none of them.

The **List (1000)** tab scrolls 1000 cells across three different source kinds to exercise
recycling, cancellation and the app-wide load listener at once.

---

## Setup

```groovy
// settings.gradle
include ':glideimageview'

// app/build.gradle
dependencies { implementation project(':glideimageview') }
```

```bash
./gradlew :glideimageview:assembleDebug   # AAR
./gradlew :sample:assembleDebug           # sample APK
```

Outputs: `glideimageview/build/outputs/aar/` and `sample/build/outputs/apk/debug/`.
