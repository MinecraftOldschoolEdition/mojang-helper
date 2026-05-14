package net.mcose.friendsapi.p2p;

import net.mcose.friendsapi.adapter.GameSession;

import java.util.UUID;

/**
 * Modern identity extracted from the legacy Session string.
 */
public final class ModernSessionInfo {
    private final String username;
    private final String accessToken;
    private final UUID profileId;

    public ModernSessionInfo(String username, String accessToken, UUID profileId) {
        this.username = username;
        this.accessToken = accessToken;
        this.profileId = profileId;
    }

    public static ModernSessionInfo fromSession(GameSession session) {
        if (session == null || !session.hasModernToken()) {
            return null;
        }
        return new ModernSessionInfo(
                session.getUsername(),
                session.getAccessToken(),
                UUID.fromString(dash(session.getProfileId())));
    }

    public String getUsername() { return username; }
    public String getAccessToken() { return accessToken; }
    public UUID getProfileId() { return profileId; }

    private static String dash(String uuid) {
        return uuid.substring(0, 8) + "-"
                + uuid.substring(8, 12) + "-"
                + uuid.substring(12, 16) + "-"
                + uuid.substring(16, 20) + "-"
                + uuid.substring(20);
    }
}
