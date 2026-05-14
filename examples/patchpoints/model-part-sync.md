# Model-Part Sync Settings

Target files:

- `GameSettings.java`
- `GuiSkinCustomization.java`
- `NetClientHandler.java`
- `SkinManager.java`

```java
// GameSettings keeps modern layer toggles in options.txt.
public int getSkinModelPartMask() {
    int mask = 0;
    if (skinPartCape) mask |= PlayerTextureInfo.MODEL_PART_CAPE;
    if (skinPartJacket) mask |= PlayerTextureInfo.MODEL_PART_JACKET;
    if (skinPartLeftSleeve) mask |= PlayerTextureInfo.MODEL_PART_LEFT_SLEEVE;
    if (skinPartRightSleeve) mask |= PlayerTextureInfo.MODEL_PART_RIGHT_SLEEVE;
    if (skinPartLeftPantsLeg) mask |= PlayerTextureInfo.MODEL_PART_LEFT_PANTS_LEG;
    if (skinPartRightPantsLeg) mask |= PlayerTextureInfo.MODEL_PART_RIGHT_PANTS_LEG;
    if (skinPartHat) mask |= PlayerTextureInfo.MODEL_PART_HAT;
    return mask;
}
```

```java
// NetClientHandler sends settings after negotiation and whenever the skin
// customization screen changes.
byte[] payload = ModProtocol.createSkinPartsPayload(this.mc.session.username, modelPartMask);
this.addToSendQueue(new Packet250CustomPayload(ModProtocol.CHANNEL_SKIN_PARTS, payload));
```
