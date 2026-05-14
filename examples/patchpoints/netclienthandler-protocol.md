# NetClientHandler Protocol Hooks

Target file:

- `minecraft/src/net/minecraft/src/NetClientHandler.java`

```java
// After the mod protocol HELLO_ACK, remember whether the server supports skin
// part sync. Only then send the local model-part mask.
this.supportsSkinPartSync = this.modProtocolNegotiated
    && ModProtocol.supportsFeatureBits(helloInfo.featureBits)
    && (helloInfo.featureBits & ModProtocol.FEATURE_SKIN_PARTS_SYNC) != 0;

if (this.modProtocolNegotiated) {
    this.sendLocalSkinPartMaskFromSettings();
}
```

```java
// Incoming skin-part snapshot or broadcast.
if (ModProtocol.CHANNEL_SKIN_PARTS.equals(packet.channel)) {
    ModProtocol.SkinPartsInfo skinPartsInfo = ModProtocol.readSkinPartsPayload(packet.data);
    this.mc.skinManager.updatePlayerModelPartMask(skinPartsInfo.username, skinPartsInfo.modelPartMask);
    return;
}
```

```java
// Friends verification packets are handled before generic custom payloads.
if (FriendsRelayVerifier.isFriendsServerChannel(packet.channel)) {
    FriendsManager.getInstance().getRelayVerifier().handleServerPacket(packet);
    return;
}
```
