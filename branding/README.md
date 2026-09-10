# BigBridz branding assets

Sources (from your uploads):
- `logo_source.png` — logo wordmark (JPEG bytes)
- `splash_source.png` — splash composition (JPEG bytes)

Generated:
- `logo_1024.png` — master square
- `play_store_512.png` — Play Store high-res icon
- `logo_512_transparent.png` / `logo_256.png` — in-app
- `ic_launcher_foreground_*.png` — adaptive icon layers

Android launcher mipmaps + drawable densities are under `app/src/main/res/`.
Compose splash/logo are under `composeApp/src/commonMain/composeResources/drawable/`.

Regenerate:
```
php branding/generate_icons.php
```
