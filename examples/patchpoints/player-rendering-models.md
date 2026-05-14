# Player Rendering And Alternate Models

Target files:

- `ModelPlayer.java`
- `AlexModelArms.java`
- `RenderPlayer.java`
- `SkinManager.java`
- `PlayerTextureInfo.java`

```java
// RenderPlayer chooses a model variant from SkinManager metadata.
PlayerTextureInfo textureInfo = SkinManager.instance.getPlayerTextureInfo(player.username);
boolean isSlim = textureInfo != null && textureInfo.isSlimModel();
ModelPlayer selectedModel = isSlim ? this.modelSlim : this.modelClassic;
selectedModel.setAlex(isSlim);
```

```java
// Model layers are visibility-controlled by the synced modern mask.
model.bipedHeadwear.showModel = isModelPartEnabled(info, PlayerTextureInfo.MODEL_PART_HAT);
model.bipedBodyWear.showModel = isModelPartEnabled(info, PlayerTextureInfo.MODEL_PART_JACKET);
model.bipedLeftArmwear.showModel = isModelPartEnabled(info, PlayerTextureInfo.MODEL_PART_LEFT_SLEEVE);
model.bipedRightArmwear.showModel = isModelPartEnabled(info, PlayerTextureInfo.MODEL_PART_RIGHT_SLEEVE);
```

```java
// SkinManager upgrades old 64x32 skins before upload, so the renderer can
// always use 64x64 UVs for modern and legacy players.
BufferedImage processedImage = processSkinImage(rawImage, info);
```
