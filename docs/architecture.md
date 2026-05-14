# Architecture

The b1.7.3 backport is split into three layers:

1. **Modern identity**: the old `Session` string carries modern launcher data as
   `token:<accessToken>:<uuid>`. `auth.SessionBootstrap` creates that value and
   `adapter.GameSession` reads it.
2. **Friends and presence**: `mojang.YggdrasilFriendsService` talks to
   `api.minecraftservices.com`, while `core.FriendsManager` keeps an in-memory
   mirror for old-engine UI and join logic.
3. **Legacy integration**: custom payloads bridge old client/server protocol
   gaps for `MCOSE|SKINPARTS` model layer/cape preference sync.

The reference library deliberately avoids direct references to `Minecraft`,
`Packet250CustomPayload`, `RenderEngine`, and UberBukkit server classes. Those
dependencies are represented by tiny adapters and patchpoint snippets.

## Friend Data Ownership

Mojang's friends endpoint is the source of truth for accepted friends and
requests. The old signed local cache and beta-server mutual-proof layer have
been removed from this reference kit; clients should refresh from Mojang
instead of storing friend trust metadata offline.

## Why P2P Is Adapter-Based

The production client uses `dev.onvoid.webrtc`. This repo keeps the session,
message, and TCP bridge logic, but exposes WebRTC as interfaces. That lets the
reference code compile as a normal Java 8 library and keeps native WebRTC setup
out of the reusable API.
