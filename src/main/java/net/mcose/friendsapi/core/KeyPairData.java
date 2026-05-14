package net.mcose.friendsapi.core;

import org.json.JSONObject;

/**
 * Persisted Ed25519 key pair metadata.
 *
 * The private key value is Base64-encoded raw Ed25519 private bytes. The
 * original client obfuscated this at rest; this reference kit keeps the hook
 * explicit but does not pretend obfuscation is cryptographic protection.
 */
public final class KeyPairData {
    public static final int CURRENT_KEY_VERSION = 1;

    private final String ownerUuid;
    private final String publicKey;
    private String privateKey;
    private final String algorithm;
    private final long createdAt;
    private final int keyVersion;
    private boolean obfuscated;

    public KeyPairData(String ownerUuid, String publicKey, String privateKey, String algorithm) {
        this.ownerUuid = FriendEntry.normalizeUuid(ownerUuid);
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.algorithm = algorithm == null ? "Ed25519" : algorithm;
        this.createdAt = System.currentTimeMillis();
        this.keyVersion = CURRENT_KEY_VERSION;
    }

    public KeyPairData(JSONObject json) {
        this.ownerUuid = FriendEntry.normalizeUuid(json.optString("ownerUuid", ""));
        this.publicKey = json.optString("publicKey", "");
        this.privateKey = json.optString("privateKey", "");
        this.algorithm = json.optString("algorithm", "Ed25519");
        this.createdAt = json.optLong("createdAt", System.currentTimeMillis());
        this.keyVersion = json.optInt("keyVersion", CURRENT_KEY_VERSION);
        this.obfuscated = json.optBoolean("obfuscated", false);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("ownerUuid", ownerUuid);
        json.put("publicKey", publicKey);
        json.put("privateKey", privateKey);
        json.put("algorithm", algorithm);
        json.put("createdAt", createdAt);
        json.put("keyVersion", keyVersion);
        json.put("obfuscated", obfuscated);
        return json;
    }

    public boolean isValid() {
        return ownerUuid.length() == 32
                && publicKey != null && publicKey.length() > 0
                && privateKey != null && privateKey.length() > 0
                && "Ed25519".equalsIgnoreCase(algorithm);
    }

    public boolean matchesOwner(String expectedUuid) {
        return ownerUuid.equals(FriendEntry.normalizeUuid(expectedUuid));
    }

    public String getOwnerUuid() { return ownerUuid; }
    public String getPublicKey() { return publicKey; }
    public String getPrivateKey() { return privateKey; }
    public String getAlgorithm() { return algorithm; }
    public long getCreatedAt() { return createdAt; }
    public int getKeyVersion() { return keyVersion; }
    public boolean isObfuscated() { return obfuscated; }

    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public void setObfuscated(boolean obfuscated) { this.obfuscated = obfuscated; }
}
