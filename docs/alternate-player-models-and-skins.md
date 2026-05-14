# Alternate Player Models And Skins

Modern skin support needed three pieces.

## Texture Profile Parsing

The session server profile contains a Base64 `textures` property. The parser
extracts:

- skin URL
- cape URL
- `metadata.model = slim`
- texture hash
- whether Mojang returned a signed property

## 64x32 To 64x64 Upgrade

Legacy skins are upgraded using the modern copy-rect map, then opaque regions
are normalized. This prevents old hat/overlay alpha quirks from corrupting the
new arm/leg layer layout.

## Slim/Classic Rendering

The production client uses an `AlexModelArms` wrapper so the renderer can switch
between 4px Steve arms and 3px Alex arms without replacing the whole player
model. This reference kit exposes the metadata and model-part bits; the actual
renderer patch lives in `examples/patchpoints/player-rendering-models.md`.

## Model-Part Sync

The local settings mask is sent over `MCOSE|SKINPARTS`. Remote clients update
their cached `PlayerTextureInfo` and hide/show cape, jacket, sleeves, pants
legs, and hat layers accordingly.
