# Security And Limitations

## What Is Protected

- Local friend metadata is tamper-evident through canonical SHA-256 plus
  Ed25519 signatures.
- Mojang session auth proves the connecting username/profile to online-mode
  servers.
- Friend verification uses Mojang UUIDs, not display names.

## What Is Not Protected

- Local key obfuscation is not encryption.
- P2P signaling metadata still depends on the chosen signaling service.
- This reference kit does not validate Mojang texture signatures; it preserves
  the `secureProfile` flag for render/UI decisions.
- The WebRTC layer is adapter-based here, so TURN/STUN policy and native WebRTC
  hardening belong to the embedding client.

## Compatibility Notes

- Keep all public wire names stable. Changing `MCOSE|F*` or `MCOSE|SKINPARTS`
  breaks interoperability with existing MCOSE clients/servers.
- Keep Java 8 compatibility. Legacy launchers and beta-era mod packs commonly
  run under Java 8 runtimes.
- Do not block the render or network thread on Mojang friends refreshes.
