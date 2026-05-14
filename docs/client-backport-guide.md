# Client Backport Guide

The client integration has four key hooks.

## Startup

Create the session through `SessionBootstrap`, assign it to the legacy
`Minecraft.session`, then initialize friends after the session exists:

```java
SessionBootstrap.ResolvedSession resolved = SessionBootstrap.resolveFromCommandLine(args);
this.session = new Session(resolved.username, resolved.sessionId);
FriendsManager.getInstance().initialize(this);
FriendsManager.getInstance().onSessionReady();
```

In this reference repo the same idea is:

```java
GameSession session = SessionBootstrap.resolveFromCommandLine(args);
FriendsManager manager = new FriendsManager();
manager.onSessionReady(session);
```

## Social UI

The production client wires Social from the main menu, pause menu, players/chat
screen, and the `O` key. Keep text-entry safe: route gameplay key handling from
`Minecraft`, and GUI key interception from `GuiScreen.handleInput()` only when
the focused screen is not a text field.

## World Join/Leave

Call the friends manager when connection state changes:

```java
FriendsManager.getInstance().onWorldJoin();
FriendsManager.getInstance().onMultiplayerJoin(serverAddress, serverName);
FriendsManager.getInstance().onWorldLeave();
```

Those hooks update presence and stop any active P2P bridge when leaving a world.

## Packet Handling

The friends list relies on Mojang service data now. Keep the normal mod payload
handling for features such as skin-part sync.
