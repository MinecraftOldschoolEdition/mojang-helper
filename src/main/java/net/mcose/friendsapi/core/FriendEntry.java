package net.mcose.friendsapi.core;

import org.json.JSONObject;

/**
 * One friend row from the signed local cache.
 *
 * The Mojang friends service is authoritative for the modern list, but the
 * b1.7.3 backport also keeps local trust metadata so old servers can verify
 * mutual friendship through custom payloads.
 */
public final class FriendEntry {
    private final String friendUuid;
    private String lastKnownName;
    private final long addedAt;
    private boolean mutualVerified;
    private long mutualVerifiedAt;
    private String friendPublicKey;
    private long friendPublicKeyReceivedAt;
    private String ourSignatureForThem;
    private String theirSignatureForUs;
    private TrustLevel trustLevel;
    private String notes;
    private FriendPresence presence;

    public FriendEntry(String friendUuid, String lastKnownName) {
        this(friendUuid, lastKnownName, System.currentTimeMillis());
    }

    public FriendEntry(String friendUuid, String lastKnownName, long addedAt) {
        this.friendUuid = normalizeUuid(friendUuid);
        this.lastKnownName = lastKnownName == null ? "" : lastKnownName;
        this.addedAt = addedAt;
        this.trustLevel = TrustLevel.ONE_SIDED;
        this.presence = new FriendPresence();
    }

    public FriendEntry(JSONObject json) {
        this.friendUuid = normalizeUuid(json.optString("friendUuid", ""));
        this.lastKnownName = json.optString("lastKnownName", "");
        this.addedAt = json.optLong("addedAt", System.currentTimeMillis());
        this.mutualVerified = json.optBoolean("mutualVerified", false);
        this.mutualVerifiedAt = json.optLong("mutualVerifiedAt", 0L);
        this.friendPublicKey = nullIfEmpty(json.optString("friendPublicKey", null));
        this.friendPublicKeyReceivedAt = json.optLong("friendPublicKeyReceivedAt", 0L);
        this.ourSignatureForThem = nullIfEmpty(json.optString("ourSignatureForThem", null));
        this.theirSignatureForUs = nullIfEmpty(json.optString("theirSignatureForUs", null));
        this.trustLevel = TrustLevel.fromName(json.optString("trustLevel", TrustLevel.ONE_SIDED.name()));
        this.notes = nullIfEmpty(json.optString("notes", null));
        this.presence = new FriendPresence();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("friendUuid", friendUuid);
        json.put("lastKnownName", lastKnownName);
        json.put("addedAt", addedAt);
        json.put("mutualVerified", mutualVerified);
        json.put("mutualVerifiedAt", mutualVerifiedAt);
        json.put("friendPublicKey", friendPublicKey == null ? JSONObject.NULL : friendPublicKey);
        json.put("friendPublicKeyReceivedAt", friendPublicKeyReceivedAt);
        json.put("ourSignatureForThem", ourSignatureForThem == null ? JSONObject.NULL : ourSignatureForThem);
        json.put("theirSignatureForUs", theirSignatureForUs == null ? JSONObject.NULL : theirSignatureForUs);
        json.put("trustLevel", trustLevel.name());
        json.put("notes", notes == null ? JSONObject.NULL : notes);
        return json;
    }

    public void markMutualVerified() {
        this.mutualVerified = true;
        this.mutualVerifiedAt = System.currentTimeMillis();
        this.trustLevel = TrustLevel.MUTUAL;
    }

    public void markSuspicious() {
        this.mutualVerified = false;
        this.trustLevel = TrustLevel.SUSPICIOUS;
    }

    public void markOneSided() {
        this.mutualVerified = false;
        this.trustLevel = TrustLevel.ONE_SIDED;
    }

    public String getFriendUuid() { return friendUuid; }
    public String getLastKnownName() { return lastKnownName; }
    public long getAddedAt() { return addedAt; }
    public boolean isMutualVerified() { return mutualVerified; }
    public long getMutualVerifiedAt() { return mutualVerifiedAt; }
    public String getFriendPublicKey() { return friendPublicKey; }
    public long getFriendPublicKeyReceivedAt() { return friendPublicKeyReceivedAt; }
    public String getOurSignatureForThem() { return ourSignatureForThem; }
    public String getTheirSignatureForUs() { return theirSignatureForUs; }
    public TrustLevel getTrustLevel() { return trustLevel; }
    public String getNotes() { return notes; }
    public FriendPresence getPresence() { return presence; }

    public void setLastKnownName(String lastKnownName) { this.lastKnownName = lastKnownName == null ? "" : lastKnownName; }
    public void setFriendPublicKey(String friendPublicKey) {
        this.friendPublicKey = nullIfEmpty(friendPublicKey);
        this.friendPublicKeyReceivedAt = this.friendPublicKey == null ? 0L : System.currentTimeMillis();
    }
    public void setOurSignatureForThem(String ourSignatureForThem) { this.ourSignatureForThem = nullIfEmpty(ourSignatureForThem); }
    public void setTheirSignatureForUs(String theirSignatureForUs) { this.theirSignatureForUs = nullIfEmpty(theirSignatureForUs); }
    public void setTrustLevel(TrustLevel trustLevel) { this.trustLevel = trustLevel == null ? TrustLevel.ONE_SIDED : trustLevel; }
    public void setNotes(String notes) { this.notes = nullIfEmpty(notes); }
    public void setPresence(FriendPresence presence) { this.presence = presence == null ? new FriendPresence() : presence; }

    static String normalizeUuid(String value) {
        String trimmed = nullIfEmpty(value);
        return trimmed == null ? "" : trimmed.replace("-", "").toLowerCase(java.util.Locale.ROOT);
    }

    private static String nullIfEmpty(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }
}
