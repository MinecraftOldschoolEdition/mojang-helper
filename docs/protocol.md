# Protocol

## Skin Parts

`MCOSE|SKINPARTS` payload:

```text
writeUTF(username)
writeByte(modelPartMask)
```

Mask bits:

- `0x01`: cape
- `0x02`: jacket
- `0x04`: left sleeve
- `0x08`: right sleeve
- `0x10`: left pants leg
- `0x20`: right pants leg
- `0x40`: hat
- `0x80`: prefer Mojang official cape source

## Mod Handshake Feature Bit

`FEATURE_SKIN_PARTS_SYNC = 1 << 7`. The client only sends skin-part updates
after mod protocol negotiation confirms that the server supports this feature.
