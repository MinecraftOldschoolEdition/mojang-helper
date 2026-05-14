# MCOSE Friends API Backport Reference Kit

This repository documents and packages the original glue used to backport
modern Minecraft friends, presence, Mojang session auth, P2P friends-world
joining, and alternate player-model support into the Minecraft Oldschool
Edition b1.7.3 engine overhaul.

It is intentionally a **reference kit**, not a drop-in replacement for the
client or UberBukkit server. The reusable parts are compiled as a Java 8
library under `net.mcose.friendsapi.*`; engine and server integration points
are documented as annotated patch snippets in `examples/patchpoints/`.

## What Is Included

- `core`: signed friend lists, trust state, canonical JSON, key storage, and
  tamper detection.
- `mojang`: a Java 8 port of the 1.22 friends and presence service shape.
- `p2p`: signaling messages, modern session identity, and WebRTC/TCP bridge
  adapter points.
- `auth`: restored Mojang join and hasJoined flow helpers for b1.7.3-era auth.
- `skins`: modern skin profile parsing, 64x32 to 64x64 conversion, slim/classic
  model metadata, cape source preference, and model-part masks.
- `protocol`: stable `MCOSE|F*` and `MCOSE|SKINPARTS` custom payload codecs.
- `docs`: design notes explaining how this was wired into the real engine.

## Verify

```sh
./gradlew test
./gradlew compileJava
git diff --check
```

## Source Notes

The implementation was derived from the MCOSE backport in:

- Client source: `/home/eric/Developer/client-source`
- Server source: `/home/eric/Developer/uberbukkit-mcose`

Only original helper/glue code and documentation are copied here. Large
decompiled engine/server classes are represented by integration snippets.
