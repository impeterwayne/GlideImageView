# GlideImageView Documentation

Welcome to the `GlideImageView` technical documentation.

## Documentation Index

- **[Extending GlideImageView](EXTENSIONS.md)**  
  Architectural seams and customization guides:
  - Custom `ModelResolver`s (domain models, authenticated endpoints)
  - Composable `Shape`s (circle, rounded corners, squircle, border, grayscale)
  - `RequestDecorator`s for fine-grained Glide options
  - Swappable `RequestManagerFactory`
  - `OnLoadListener` telemetry and lifecycle callbacks
  - Subclassing `GlideImageView`
  - Recycler view integration patterns

- **[Caching & Memory Strategy](CACHING.md)**  
  Comprehensive caching configuration:
  - Disk cache strategies (`CacheType`: `ALL`, `NONE`, `DATA`, `RESOURCE`, `AUTOMATIC`)
  - In-memory bitmap cache controls (`skipMemoryCache`)
  - Packaged drawable resource handling and APK duplication avoidance
  - Cache key invalidation with `ObjectKey` and `ApplicationVersionSignature`
  - Configuration precedence order

---

For basic usage, installation, and XML attribute reference, see the main [README.md](../README.md).
