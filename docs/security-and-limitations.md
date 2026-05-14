# Security And Limitations

## What Is Protected

- Mojang's friends service is authoritative for accepted friends and requests.
- Mojang session auth proves the connecting username/profile to online-mode
  servers.
- Friend lookup uses Mojang UUIDs, not display names.

## What Is Not Protected

- P2P signaling metadata still depends on the chosen signaling service.
- This reference kit does not validate Mojang texture signatures; it preserves
  the `secureProfile` flag for render/UI decisions.
- The WebRTC layer is adapter-based here, so TURN/STUN policy and native WebRTC
  hardening belong to the embedding client.

## Compatibility Notes

- Keep public wire names stable. Changing `MCOSE|SKINPARTS` breaks
  interoperability with existing MCOSE clients/servers that support model-part
  sync.
- Keep Java 8 compatibility. Legacy launchers and beta-era mod packs commonly
  run under Java 8 runtimes.
- Do not block the render or network thread on Mojang friends refreshes.
