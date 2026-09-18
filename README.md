# GlideImageView

[![JitPack](https://jitpack.io/v/impeterwayne/GlideImageView.svg)](https://jitpack.io/#impeterwayne/GlideImageView)
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg)](https://android-arsenal.com/api?level=23)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Glide-backed `ImageView` for Android designed for declarative image loading in XML via `app:glideSrc`, featuring **instant Android Studio Layout Editor preview** for assets and drawables, and zero-boilerplate loading. When dynamic runtime behavior or custom models are needed, it extends seamlessly in Kotlin without modifying library code.

```xml
<com.genesys.glideimageview.GlideImageView
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:scaleType="centerCrop"
    app:glideSrc="images/banner.webp"
    app:glidePlaceholder="@drawable/placeholder" />
```

---

## Installation

### 1. Add JitPack repository

Add JitPack to your `settings.gradle` or `settings.gradle.kts`:

<details open>
<summary><b>Groovy (settings.gradle)</b></summary>

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
</details>

<details>
<summary><b>Kotlin DSL (settings.gradle.kts)</b></summary>

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```
</details>

### 2. Add dependency

Add the library to your app module `build.gradle` or `build.gradle.kts`:

<details open>
<summary><b>Groovy (build.gradle)</b></summary>

```groovy
dependencies {
    implementation 'com.github.impeterwayne:GlideImageView:1.0.0'
}
```
</details>

<details>
<summary><b>Kotlin DSL (build.gradle.kts)</b></summary>

```kotlin
dependencies {
    implementation("com.github.impeterwayne:GlideImageView:1.0.0")
}
```
</details>

---

## XML Attributes

| Attribute | Format | Description |
|---|---|---|
| `app:glideSrc` | `string\|reference` | **Primary feature:** drawable resource (`@drawable/...`), asset path (`images/...`), remote URL, file, or URI |
| `app:glidePlaceholder` | `reference` | Drawable resource shown while loading |
| `app:glideError` | `reference` | Drawable resource shown when the load fails |
| `app:glideCacheType` | `enum` | Disk cache strategy: `all`, `none`, `data`, `resource`, `automatic` |
| `app:glideSkipMemoryCache` | `boolean` | Whether to skip Glide's in-memory bitmap pool/cache (default: false) |
| `app:glideRadius` | `dimension` | Corner radius (adds `Shape.RoundedCorners`) |
| `app:glideCircle` | `boolean` | Circular crop (adds `Shape.Circle`) |
| `app:glideCrossFade` | `boolean` | Enables crossfade transition |
| `app:glideCrossFadeDuration` | `integer` | Crossfade duration in milliseconds (default: 300ms) |

---

## `app:glideSrc` with Drawables & Assets

The flagship feature of `GlideImageView` is `app:glideSrc`, bringing declarative, zero-boilerplate image loading straight into your layout files—with first-class support for drawable resources and asset sources.

### 1. Live Layout Editor Preview
Standard image loaders in Android leave views as blank grey or white boxes in Android Studio design mode.

With `app:glideSrc`, **drawables and assets render live inside the Layout Editor**:

```xml
<!-- Previews immediately in the Android Studio Layout Editor -->
<com.genesys.glideimageview.GlideImageView
    android:id="@+id/bannerImage"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:scaleType="centerCrop"
    app:glideSrc="@drawable/placeholder_image"
    app:glidePlaceholder="@drawable/placeholder_image" />
```

- **Drawable resources (`@drawable/...`, `@mipmap/...`)**: Direct references to app drawables and mipmaps preview live in design mode and can be composed with shapes (`app:glideCircle`, `app:glideRadius`).
- **Asset paths (`images/...`)**: Decoded directly from `src/main/assets/` during edit mode (`isInEditMode`), so you see actual design assets right inside Android Studio without running the app.
- **Remote URLs (`https://...`)**: Safely display `app:glidePlaceholder` during edit mode.

### 2. Supported Sources
While drawable resources and asset paths provide the best Layout Editor experience, `app:glideSrc` transparently accepts any source format:

| Format | Example | Layout Editor Preview |
|---|---|---|
| **Drawable resource** | `@drawable/sample_banner` | **Yes** (renders drawable resource) |
| **Asset path** | `images/sample_banner.webp` | **Yes** (renders asset bitmap live) |
| **Resource URI** | `android.resource://com.example.app/drawable/logo` | **Yes** (renders drawable resource) |
| **Remote URL** | `https://example.com/photo.jpg` | Shows placeholder |
| **Local file** | `/sdcard/photo.jpg` or `file:///storage/...` | Shows placeholder |
| **Content URI** | `content://media/external/images/media/1` | Shows placeholder |
| **Data URI** | `data:image/png;base64,...` | Shows placeholder |

### 3. Process-Wide & Style Defaults
Configure standard placeholders and error states once process-wide via `GlideImageViewConfig.defaults`, keeping layout XML clean:

```kotlin
GlideImageViewConfig.defaults = ImageOptions(
    placeholder = R.drawable.placeholder_image,
    error = R.drawable.error_image,
    crossFade = true
)
```

You can also define reusable XML styles and apply them directly to any view using standard `style="@style/..."`:

```xml
<com.genesys.glideimageview.GlideImageView
    style="@style/Widget.MyApp.Thumbnail"
    android:layout_width="80dp"
    android:layout_height="80dp"
    app:glideSrc="images/avatar.webp" />
```

### 4. Caching & Memory Options (XML & Kotlin)
Control disk and memory caching declaratively in XML or programmatically:

```xml
<com.genesys.glideimageview.GlideImageView
    android:layout_width="match_parent"
    android:layout_height="160dp"
    app:glideSrc="@drawable/placeholder_image"
    app:glideCacheType="none"
    app:glideSkipMemoryCache="true" />
```

- **`app:glideCacheType`**: Choose from `all`, `none`, `data`, `resource`, or `automatic`.
- **`app:glideSkipMemoryCache`**: Set to `true` to bypass Glide's in-memory bitmap cache.

In Kotlin:
```kotlin
// Per-view properties
image.cacheType = CacheType.NONE
image.skipMemoryCache = true

// Or via ImageOptions
image.options = image.options
    .withCacheType(CacheType.DATA)
    .withSkipMemoryCache(true)
```

**Drawable resources are not cached by default.** When the resolved model is a packaged
resource — a resource id (including `@drawable/…` and `@mipmap/…` sources), a `Drawable`, or an
`android.resource://` uri — the request is built with `DiskCacheStrategy.NONE` and
`skipMemoryCache(true)`. The bytes already live in the APK, so caching them only duplicates what
the resource system holds.

Set a cache strategy and yours wins — nothing is forced on you:

```kotlin
image.cacheType = CacheType.RESOURCE  
```

Or app-wide via `GlideImageViewConfig.defaults`. `RequestDecorator`s still run afterwards and can
override either way.

### 5. Cache Signatures
No signature is applied unless you ask for one — cache keying is left to Glide's defaults. Set one explicitly when you need to invalidate on your own terms:

```kotlin
image.signature = ObjectKey(user.avatarUpdatedAt)

// Or via ImageOptions
image.options = image.options.withSignature(ObjectKey(file.lastModified()))
```

For drawable resources specifically, note that Glide keys on the numeric resource id, and aapt can reassign ids between builds. If you disk-cache local resources and ship frequent updates, add Glide's own
`ApplicationVersionSignature.obtain(context)` — either per view, or globally through a `RequestDecorator`.

> **For in-depth details on disk/memory caching and signature invalidation, see [docs/CACHING.md](docs/CACHING.md).**

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

## Extension Points

Everything in the library is built on top of three composable seams:

1. **`ModelResolver`** — Intercept and rewrite what gets loaded (domain models like `Avatar`, bearer-token headers, custom schemes).
2. **`Shape`** — Composable bitmap transformations (`Circle`, `RoundedCorners`, `Squircle`, `Border`, `Grayscale`, or any Glide transformation).
3. **`RequestDecorator`** — Fine-grained Glide request tuning (`diskCacheStrategy`, `thumbnail`, `priority`, dimension `override`).

Additionally, `GlideImageView` provides swappable **`RequestManagerFactory`** support and global or per-view **`OnLoadListener`** telemetry.

**For complete documentation, architectural details, and copy-paste recipes, see [docs/EXTENSIONS.md](docs/EXTENSIONS.md).**

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


## Other Notes

- **Layout-Editor Preview:** Asset sources (`images/...`) and drawable resources render immediately in Android Studio; network sources fall back to the configured placeholder.
- **Extensions & Lifecycle:** See [docs/EXTENSIONS.md](docs/EXTENSIONS.md) for custom `ModelResolver`s, `Shape`s, `RequestDecorator`s, and swappable `RequestManagerFactory`.
- **Subclassing:** `resolveModel`, `buildRequest`, `buildTransformations`, `scaleTypeTransformation`, `renderPreview`, and `requestManager` are all `protected open` (documented in [docs/EXTENSIONS.md](docs/EXTENSIONS.md#6-subclassing)).
