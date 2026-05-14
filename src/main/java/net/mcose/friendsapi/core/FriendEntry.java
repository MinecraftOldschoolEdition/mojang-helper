package net.mcose.friendsapi.core;

/**
 * Engine-neutral runtime row for a Mojang friend.
 */
public final class FriendEntry {
    private final String friendUuid;
    private String lastKnownName;
    private final long addedAt;
    private FriendPresence presence;

    public FriendEntry(String friendUuid, String lastKnownName) {
        this(friendUuid, lastKnownName, System.currentTimeMillis());
    }

    public FriendEntry(String friendUuid, String lastKnownName, long addedAt) {
        this.friendUuid = normalizeUuid(friendUuid);
        this.lastKnownName = lastKnownName == null ? "" : lastKnownName;
        this.addedAt = addedAt;
        this.presence = new FriendPresence();
    }

    public String getFriendUuid() { return friendUuid; }
    public String getLastKnownName() { return lastKnownName; }
    public long getAddedAt() { return addedAt; }
    public FriendPresence getPresence() { return presence; }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName == null ? "" : lastKnownName;
    }

    public void setPresence(FriendPresence presence) {
        this.presence = presence == null ? new FriendPresence() : presence;
    }

    public static String normalizeUuid(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? "" : trimmed.replace("-", "").toLowerCase(java.util.Locale.ROOT);
    }
}
