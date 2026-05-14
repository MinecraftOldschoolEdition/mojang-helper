# Server Auth Restoration

Beta 1.7.3 predates the modern encrypted login/session-server flow. The MCOSE
server restores the important parts while keeping the old protocol usable.

## Login Handshake

When `online-mode=true`, `NetLoginHandler` sends an encryption request using
the server public key and a verify token. The client responds, both sides derive
the Mojang `serverId`, and the client calls `/join`.

The server does not enable link-layer encryption in the beta stream; it uses the
cryptographic exchange only to prove the Mojang session.

## Session Verification

`ThreadLoginVerifier` calls:

```text
https://sessionserver.mojang.com/session/minecraft/hasJoined
```

with `username`, `serverId`, and usually the connecting IP. It retries transient
Mojang failures and can do a no-IP fallback when the primary response is
inconclusive.

## Offline/Cracked Boundary

Offline mode and cracked allowlist users should not hit Mojang. They fall back
to the existing offline UUID behavior. Online-mode users not in the allowlist
must complete modern session verification before the player entity is accepted.
