# Mojang Friends And Presence

`mojang.YggdrasilFriendsService` mirrors the 1.22 service shape in Java 8.

## Friends

The client uses `/friends` for:

- accepted friends
- incoming requests
- outgoing requests
- add/remove/revoke/accept/decline actions

The service keeps an ETag and a short request cooldown. That matches the
backport behavior: UI refreshes are allowed, but repeated screens cannot spam
the endpoint every frame.

## Preferences

The social options screen updates `/player/attributes` with:

```json
{
  "friendsPreferences": {
    "friends": "ENABLED",
    "acceptInvites": "ENABLED"
  }
}
```

## Presence

Presence posts include status and optional join info:

```json
{
  "status": "ONLINE",
  "joinInfo": {
    "value": "p2p:<opaque>",
    "invites": ["<undashed profile uuid>"]
  }
}
```

The old client maps that into `FriendPresence`: online/away/offline color,
activity text, joinable state, and P2P PMID.
