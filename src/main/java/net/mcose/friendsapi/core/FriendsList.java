package net.mcose.friendsapi.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Signed local cache for friend trust metadata.
 *
 * The official 1.22 friends service owns the current social graph. This file
 * owns the extra proof data needed by legacy servers: public keys, peer claims,
 * and mutual-verification timestamps. The unsigned canonical JSON is the value
 * that gets hashed and signed.
 */
public final class FriendsList {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    private final int schemaVersion;
    private final String ownerUuid;
    private String ownerPublicKey;
    private final long createdAt;
    private long lastLoadedAt;
    private final List<FriendEntry> friends;
    private String contentHash;
    private String fileSignature;

    public FriendsList(String ownerUuid) {
        this.schemaVersion = CURRENT_SCHEMA_VERSION;
        this.ownerUuid = FriendEntry.normalizeUuid(ownerUuid);
        this.createdAt = System.currentTimeMillis();
        this.lastLoadedAt = this.createdAt;
        this.friends = new ArrayList<FriendEntry>();
    }

    public FriendsList(JSONObject json) {
        this.schemaVersion = json.optInt("schemaVersion", CURRENT_SCHEMA_VERSION);
        this.ownerUuid = FriendEntry.normalizeUuid(json.optString("ownerUuid", ""));
        this.ownerPublicKey = nullIfEmpty(json.optString("ownerPublicKey", null));
        this.createdAt = json.optLong("createdAt", System.currentTimeMillis());
        this.lastLoadedAt = json.optLong("lastLoadedAt", this.createdAt);
        this.contentHash = nullIfEmpty(json.optString("contentHash", null));
        this.fileSignature = nullIfEmpty(json.optString("fileSignature", null));
        this.friends = new ArrayList<FriendEntry>();

        JSONArray array = json.optJSONArray("friends");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject entry = array.optJSONObject(i);
                if (entry != null) {
                    this.friends.add(new FriendEntry(entry));
                }
            }
        }
        sortFriends();
    }

    public JSONObject toJson(boolean includeIntegrityFields) {
        JSONObject json = new JSONObject();
        json.put("schemaVersion", schemaVersion);
        json.put("ownerUuid", ownerUuid);
        json.put("ownerPublicKey", ownerPublicKey == null ? JSONObject.NULL : ownerPublicKey);
        json.put("createdAt", createdAt);
        json.put("lastLoadedAt", lastLoadedAt);

        JSONArray array = new JSONArray();
        for (FriendEntry friend : sortedCopy()) {
            array.put(friend.toJson());
        }
        json.put("friends", array);

        if (includeIntegrityFields) {
            json.put("contentHash", contentHash == null ? JSONObject.NULL : contentHash);
            json.put("fileSignature", fileSignature == null ? JSONObject.NULL : fileSignature);
        }
        return json;
    }

    public String toUnsignedCanonicalJson() {
        return CanonicalJson.stringify(toJson(false));
    }

    public String toSignedCanonicalJson() {
        return CanonicalJson.stringify(toJson(true));
    }

    public boolean addFriend(FriendEntry friend) {
        if (friend == null || friend.getFriendUuid().length() == 0 || hasFriend(friend.getFriendUuid())) {
            return false;
        }
        friends.add(friend);
        sortFriends();
        return true;
    }

    public boolean removeFriend(String friendUuid) {
        String normalized = FriendEntry.normalizeUuid(friendUuid);
        for (int i = 0; i < friends.size(); i++) {
            if (friends.get(i).getFriendUuid().equals(normalized)) {
                friends.remove(i);
                return true;
            }
        }
        return false;
    }

    public FriendEntry getFriend(String friendUuid) {
        String normalized = FriendEntry.normalizeUuid(friendUuid);
        for (FriendEntry friend : friends) {
            if (friend.getFriendUuid().equals(normalized)) {
                return friend;
            }
        }
        return null;
    }

    public boolean hasFriend(String friendUuid) {
        return getFriend(friendUuid) != null;
    }

    public int getMutualFriendCount() {
        int count = 0;
        for (FriendEntry friend : friends) {
            if (friend.isMutualVerified()) {
                count++;
            }
        }
        return count;
    }

    public List<FriendEntry> getFriendsByTrustLevel(TrustLevel level) {
        List<FriendEntry> out = new ArrayList<FriendEntry>();
        for (FriendEntry friend : friends) {
            if (friend.getTrustLevel() == level) {
                out.add(friend);
            }
        }
        return Collections.unmodifiableList(out);
    }

    public int getSchemaVersion() { return schemaVersion; }
    public String getOwnerUuid() { return ownerUuid; }
    public String getOwnerPublicKey() { return ownerPublicKey; }
    public long getCreatedAt() { return createdAt; }
    public long getLastLoadedAt() { return lastLoadedAt; }
    public int getFriendCount() { return friends.size(); }
    public String getContentHash() { return contentHash; }
    public String getFileSignature() { return fileSignature; }

    public List<FriendEntry> getFriends() {
        return Collections.unmodifiableList(sortedCopy());
    }

    public void setOwnerPublicKey(String ownerPublicKey) { this.ownerPublicKey = nullIfEmpty(ownerPublicKey); }
    public void setContentHash(String contentHash) { this.contentHash = nullIfEmpty(contentHash); }
    public void setFileSignature(String fileSignature) { this.fileSignature = nullIfEmpty(fileSignature); }
    public void updateLastLoadedAt() { this.lastLoadedAt = System.currentTimeMillis(); }

    private void sortFriends() {
        Collections.sort(friends, FRIEND_ORDER);
    }

    private List<FriendEntry> sortedCopy() {
        List<FriendEntry> copy = new ArrayList<FriendEntry>(friends);
        Collections.sort(copy, FRIEND_ORDER);
        return copy;
    }

    private static final Comparator<FriendEntry> FRIEND_ORDER = new Comparator<FriendEntry>() {
        public int compare(FriendEntry a, FriendEntry b) {
            return a.getFriendUuid().compareTo(b.getFriendUuid());
        }
    };

    private static String nullIfEmpty(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }
}
