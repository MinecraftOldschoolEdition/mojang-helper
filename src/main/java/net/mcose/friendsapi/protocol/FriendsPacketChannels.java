package net.mcose.friendsapi.protocol;

/**
 * Stable custom payload channel names used by the friends backport.
 */
public final class FriendsPacketChannels {
    public static final String PREFIX = "MCOSE|F";

    public static final String HELLO = PREFIX + "HELLO";
    public static final String CLAIM = PREFIX + "CLAIM";
    public static final String ACK = PREFIX + "ACK";
    public static final String RESULT = PREFIX + "RESULT";

    public static final String QUERY = PREFIX + "QUERY";
    public static final String ONLINE = PREFIX + "ONLINE";
    public static final String VERIFY = PREFIX + "VERIFY";
    public static final String CONFIRM = PREFIX + "CONFIRM";
    public static final String SERVER_CLAIM = PREFIX + "CLAIM";
    public static final String CHECK = PREFIX + "CHECK";

    public static final int PROTOCOL_VERSION = 1;
    public static final long MAX_TIMESTAMP_AGE_MS = 5L * 60L * 1000L;

    private FriendsPacketChannels() {
    }

    public static boolean isFriendsChannel(String channel) {
        return channel != null && channel.startsWith(PREFIX);
    }
}
