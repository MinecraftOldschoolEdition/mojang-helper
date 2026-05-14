# Client Startup And Session Readiness

Target files:

- `minecraft/src/net/minecraft/client/Minecraft.java`
- `minecraft/src/net/minecraft/src/Session.java`
- `minecraft/src/net/minecraft/src/SessionBootstrap.java`

```java
// Resolve legacy positional args, launcher wrapper fields, env vars, and
// modern accessToken/uuid flags through one path. The important output is a
// normal b1.7.3 Session whose sessionId may be token:<accessToken>:<uuid>.
SessionBootstrap.ResolvedSession resolved = SessionBootstrap.resolveFromCommandLine(args);
this.session = new Session(resolved.username, resolved.sessionId);

// Initialize after Minecraft has a data dir and session object. Do not do
// Mojang friends calls before the session exists, because the service needs the
// access token embedded in sessionId.
net.minecraft.src.friends.FriendsManager.getInstance().initialize(this);
net.minecraft.src.friends.FriendsManager.getInstance().onSessionReady();
```
