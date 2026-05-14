package net.mcose.friendsapi.core;

/**
 * Presence data displayed by the b1.7.3 Social screen.
 *
 * Mojang presence has its own statuses; this class is the client-facing shape
 * after visibility filtering and join-info mapping.
 */
public final class FriendPresence {
    public enum Status {
        ONLINE(0x55FF55),
        AWAY(0xFFFF55),
        OFFLINE(0x888888);

        private final int color;

        Status(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }

    public enum VisibilityMode {
        ONLINE,
        AWAY,
        INVISIBLE,
        DISABLED
    }

    public enum Activity {
        IN_MENUS,
        SINGLEPLAYER,
        MULTIPLAYER,
        PLAYING
    }

    private Status status;
    private Activity activity;
    private String serverAddress;
    private String serverName;
    private long lastSeen;
    private boolean canJoin;
    private String p2pPmid;
    private boolean p2pJoinable;
    private boolean p2pInvited;

    public FriendPresence() {
        this(Status.OFFLINE, Activity.IN_MENUS, null, null, 0L);
    }

    public FriendPresence(Status status, Activity activity, String serverAddress, String serverName, long lastSeen) {
        this.status = status == null ? Status.OFFLINE : status;
        this.activity = activity == null ? Activity.IN_MENUS : activity;
        this.serverAddress = serverAddress;
        this.serverName = serverName;
        this.lastSeen = lastSeen;
    }

    public Status getStatus() { return status; }
    public Activity getActivity() { return activity; }
    public String getServerAddress() { return serverAddress; }
    public String getServerName() { return serverName; }
    public long getLastSeen() { return lastSeen; }
    public boolean canJoin() { return canJoin || p2pJoinable; }
    public String getP2pPmid() { return p2pPmid; }
    public boolean isP2pJoinable() { return p2pJoinable; }
    public boolean isP2pInvited() { return p2pInvited; }

    public void setStatus(Status status) { this.status = status == null ? Status.OFFLINE : status; }
    public void setActivity(Activity activity) { this.activity = activity == null ? Activity.IN_MENUS : activity; }
    public void setServerAddress(String serverAddress) { this.serverAddress = serverAddress; }
    public void setServerName(String serverName) { this.serverName = serverName; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
    public void setCanJoin(boolean canJoin) { this.canJoin = canJoin; }
    public void setP2pPmid(String p2pPmid) { this.p2pPmid = p2pPmid; }
    public void setP2pJoinable(boolean p2pJoinable) { this.p2pJoinable = p2pJoinable; }
    public void setP2pInvited(boolean p2pInvited) { this.p2pInvited = p2pInvited; }
}
