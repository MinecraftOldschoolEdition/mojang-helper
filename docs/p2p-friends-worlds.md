# P2P Friends Worlds

Friends-world joining is a bridge, not a rewritten network stack.

## Host Flow

1. The player opens a local UberBukkit-backed world.
2. The client starts signaling with its modern session.
3. Friends see presence with a join target or receive an invite.
4. Accepted join requests create a WebRTC data channel.
5. The host bridge forwards WebRTC bytes to the local server TCP port.

## Guest Flow

1. The guest clicks Join or accepts an invite.
2. The guest opens a localhost TCP bridge endpoint.
3. The normal `GuiConnecting -> NetClientHandler` path connects to that local
   endpoint.
4. The bridge forwards vanilla beta traffic over WebRTC.

## Why This Works

The game client still believes it is using normal multiplayer. P2P is only the
transport under the socket, which keeps gameplay and packet parsing unchanged.
