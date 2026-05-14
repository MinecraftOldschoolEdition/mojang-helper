package net.mcose.friendsapi.adapter;

import java.util.Locale;

/**
 * Small adapter-friendly representation of the legacy Minecraft Session object.
 *
 * Original integration source: net.minecraft.src.Session plus SessionBootstrap
 * in client-source. The b1.7.3 client only had a username/session string; the
 * backport restores the modern launcher convention where sessionId is shaped as
 * token:<accessToken>:<uuid>. Keeping that convention here lets old call sites
 * stay simple while still carrying the modern identity needed by Mojang APIs.
 */
public final class GameSession {
    private final String username;
    private final String sessionId;

    public GameSession(String username, String sessionId) {
        this.username = trimToNull(username);
        this.sessionId = trimToNull(sessionId);
    }

    public String getUsername() {
        return username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean hasModernToken() {
        return getAccessToken() != null && getProfileId() != null;
    }

    public String getAccessToken() {
        String[] parts = splitModernToken();
        return parts == null ? null : parts[1];
    }

    public String getProfileId() {
        String[] parts = splitModernToken();
        return parts == null ? null : normalizeUuid(parts[2]);
    }

    public static String toModernSessionId(String accessToken, String profileId) {
        String token = trimToNull(accessToken);
        String uuid = normalizeUuid(profileId);
        if (token == null || uuid == null) {
            return null;
        }
        return "token:" + token + ":" + uuid;
    }

    public static String normalizeUuid(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        String normalized = trimmed.replace("-", "").toLowerCase(Locale.ROOT);
        return normalized.length() == 32 ? normalized : null;
    }

    private String[] splitModernToken() {
        if (sessionId == null || !sessionId.startsWith("token:")) {
            return null;
        }
        String[] parts = sessionId.split(":", -1);
        if (parts.length < 3 || trimToNull(parts[1]) == null || normalizeUuid(parts[2]) == null) {
            return null;
        }
        return parts;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }
}
