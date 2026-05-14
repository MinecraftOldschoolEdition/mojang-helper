# Architecture

The b1.7.3 backport is split into three layers:

1. **Modern identity**: the old `Session` string carries modern launcher data as
   `token:<accessToken>:<uuid>`. `auth.SessionBootstrap` creates that value and
   `adapter.GameSession` reads it.
2. **Friends and presence**: `mojang.YggdrasilFriendsService` talks to
   `api.minecraftservices.com`, while `core.FriendsManager` mirrors the modern
   friend list into a signed local cache for legacy trust metadata.
3. **Legacy integration**: custom payloads bridge old client/server protocol
   gaps: `MCOSE|F*` for friend verification and `MCOSE|SKINPARTS` for model
   layer/cape preference sync.

The reference library deliberately avoids direct references to `Minecraft`,
`Packet250CustomPayload`, `RenderEngine`, and UberBukkit server classes. Those
dependencies are represented by tiny adapters and patchpoint snippets.

## Why There Is A Local Signed Cache

Mojang's friends endpoint tells the client who the user is friends with. It does
not store beta-server mutual-verification proofs, peer public keys, or local
tamper state. The cache in `core` exists for those extra legacy concerns.

The integrity flow is:

- Serialize friend metadata without `contentHash` or `fileSignature`.
- Canonicalize the JSON with sorted keys.
- Hash with SHA-256.
- Sign that exact canonical string with Ed25519.
- On load, recalculate hash first, then verify the signature.

## Why P2P Is Adapter-Based

The production client uses `dev.onvoid.webrtc`. This repo keeps the session,
message, and TCP bridge logic, but exposes WebRTC as interfaces. That lets the
reference code compile as a normal Java 8 library and keeps native WebRTC setup
out of the reusable API.
