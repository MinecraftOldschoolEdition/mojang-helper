# Server Auth Restoration

Target files:

- `uberbukkit-mcose/src/main/java/net/minecraft/server/NetLoginHandler.java`
- `uberbukkit-mcose/src/main/java/net/minecraft/server/ThreadLoginVerifier.java`
- `uberbukkit-mcose/src/main/java/com/legacyminecraft/poseidon/util/SessionAPI.java`

```java
// NetLoginHandler: online-mode users not in the cracked allowlist go through
// modern Mojang auth. The beta protocol still receives old packets; the crypto
// exchange exists to generate the Mojang serverId proof.
if (this.server.onlineMode && !CrackedAllowlist.get().contains(packet2handshake.a)) {
    this.modernAuthEnabled = true;
    this.verifyToken = new byte[4];
    new SecureRandom().nextBytes(this.verifyToken);
    KeyPair keyPair = CryptoHelper.getServerKeyPair();
    this.serverId = "";
    this.networkManager.queue(new Packet253EncryptionRequest(this.serverId, keyPair.getPublic(), this.verifyToken));
    return;
}
```

```java
// ThreadLoginVerifier: retry transient Mojang errors and only accept a 200
// hasJoined response whose username and, when present, IP match the login.
SessionAPI.ModernSessionResponse response =
    SessionAPI.hasJoinedModern(playerName, serverId, clientIP);
```
