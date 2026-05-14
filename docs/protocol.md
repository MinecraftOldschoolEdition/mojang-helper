# Protocol

## Friends Channels

- `MCOSE|FHELLO`: client-to-client hello and public key exchange
- `MCOSE|FCLAIM`: signed claim that one UUID added another
- `MCOSE|FACK`: claim accepted/rejected
- `MCOSE|FRESULT`: final mutual-verification result
- `MCOSE|FQUERY`: ask a server whether a friend UUID is online
- `MCOSE|FONLINE`: server response for online query
- `MCOSE|FVERIFY`: request server-assisted verification
- `MCOSE|FCONFIRM`: server verification result
- `MCOSE|FCHECK`: unauthenticated/menu-side claim check

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
