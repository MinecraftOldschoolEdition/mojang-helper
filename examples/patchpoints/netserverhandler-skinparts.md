# NetServerHandler Skin-Part Sync

Target files:

- `uberbukkit-mcose/src/main/java/net/minecraft/server/NetServerHandler.java`
- `uberbukkit-mcose/src/main/java/net/minecraft/server/network/ModProtocol.java`

```java
public boolean supportsSkinPartSync() {
    return this.modProtocolNegotiated
        && (this.negotiatedModFeatures & ModProtocol.FEATURE_SKIN_PARTS_SYNC) != 0;
}
```

```java
// Client -> server update. Store the sender's mask and broadcast it to other
// modded clients. Mask is capped at 0x7F on the server because cape-source
// preference is a client rendering hint.
ModProtocol.SkinPartsInfo skinPartsInfo = ModProtocol.readSkinPartsPayload(packet.data);
int modelPartMask = skinPartsInfo.modelPartMask & 0x7F;
this.player.setSkinModelPartMask(modelPartMask);
this.broadcastSkinPartMask(this.player.name, modelPartMask);
```

```java
// Snapshot sent after mod handshake so a joining client learns every online
// player's current model-part mask.
for (EntityPlayer onlinePlayer : server.serverConfigurationManager.players) {
    this.sendPacket(new Packet250CustomPayload(
        ModProtocol.CHANNEL_SKIN_PARTS,
        ModProtocol.createSkinPartsPayload(onlinePlayer.name, onlinePlayer.getSkinModelPartMask())));
}
```
