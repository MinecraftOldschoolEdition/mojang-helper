# Server Friends Verification

Target files:

- `uberbukkit-mcose/src/main/java/net/minecraft/server/MinecraftServer.java`
- `uberbukkit-mcose/src/main/java/net/minecraft/server/FriendsVerificationHandler.java`
- `uberbukkit-mcose/src/main/java/net/minecraft/server/NetServerHandler.java`
- `uberbukkit-mcose/src/main/java/net/minecraft/server/ServerConfigurationManager.java`

```java
// MinecraftServer field.
public final FriendsVerificationHandler friendsVerificationHandler =
    new FriendsVerificationHandler(this);
```

```java
// NetServerHandler custom payload path.
if (minecraftServer.friendsVerificationHandler.handlePacket(this.player, packet250custompayload)) {
    return;
}
```

```java
// ServerConfigurationManager join/leave hooks keep online status and pending
// claim notifications accurate.
this.server.friendsVerificationHandler.onPlayerJoin(entityplayer);
this.server.friendsVerificationHandler.onPlayerLeave(entityplayer);
```
