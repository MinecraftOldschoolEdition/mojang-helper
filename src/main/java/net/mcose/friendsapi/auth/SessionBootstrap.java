package net.mcose.friendsapi.auth;

import net.mcose.friendsapi.adapter.GameSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared launcher/session bootstrap for old clients.
 *
 * Original integration source: net.minecraft.src.SessionBootstrap. The key
 * behavior is preserving the old positional username/session CLI while adding
 * modern --accessToken/--uuid support and packing them into token:<token>:<uuid>.
 */
public final class SessionBootstrap {
    private static final String SESSION_PLACEHOLDER = "-";

    private SessionBootstrap() {
    }

    public static GameSession resolveFromCommandLine(String[] args) {
        Map<String, String> values = collectBaseValues();
        return resolve(values, args, generateDefaultUsername(), SESSION_PLACEHOLDER);
    }

    public static GameSession resolve(Map<String, String> baseValues, String[] args, String defaultUsername, String missingSessionValue) {
        Map<String, String> values = new HashMap<String, String>();
        if (baseValues != null) {
            values.putAll(baseValues);
        }
        parseCommandLineArgs(args, values);

        String username = firstUsable(values.get("username"), defaultUsername);
        String sessionId = resolveSessionId(values);
        if (!isUsable(sessionId)) {
            sessionId = missingSessionValue;
        }
        return new GameSession(username, sessionId);
    }

    public static String redactSessionForLog(String sessionId) {
        String value = trimToNull(sessionId);
        if (value == null) {
            return SESSION_PLACEHOLDER;
        }
        return value.length() > 20 ? value.substring(0, 20) + "..." : value;
    }

    private static Map<String, String> collectBaseValues() {
        Map<String, String> values = new HashMap<String, String>();
        putFallback(values, "username", System.getProperty("minecraft.username"));
        putFallback(values, "session", System.getProperty("minecraft.session"));
        putFallback(values, "sessionid", System.getProperty("minecraft.sessionid"));
        putFallback(values, "accessToken", System.getProperty("minecraft.accessToken"));
        putFallback(values, "uuid", System.getProperty("minecraft.uuid"));

        putFallback(values, "username", System.getenv("MINECRAFT_USERNAME"));
        putFallback(values, "session", System.getenv("MINECRAFT_SESSION"));
        putFallback(values, "sessionid", System.getenv("MINECRAFT_SESSIONID"));
        putFallback(values, "accessToken", System.getenv("MINECRAFT_ACCESS_TOKEN"));
        putFallback(values, "uuid", System.getenv("MINECRAFT_UUID"));
        return values;
    }

    private static void parseCommandLineArgs(String[] args, Map<String, String> values) {
        if (args == null) {
            return;
        }
        int positionalIndex = 0;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!isUsable(arg)) {
                continue;
            }

            if (arg.startsWith("--username=")) {
                putOverride(values, "username", arg.substring("--username=".length()));
            } else if ("--username".equals(arg) && i + 1 < args.length) {
                putOverride(values, "username", args[++i]);
            } else if (arg.startsWith("--session=")) {
                putOverride(values, "session", arg.substring("--session=".length()));
            } else if ("--session".equals(arg) && i + 1 < args.length) {
                putOverride(values, "session", args[++i]);
            } else if (arg.startsWith("--sessionid=")) {
                putOverride(values, "sessionid", arg.substring("--sessionid=".length()));
            } else if ("--sessionid".equals(arg) && i + 1 < args.length) {
                putOverride(values, "sessionid", args[++i]);
            } else if (arg.startsWith("--accessToken=")) {
                putOverride(values, "accessToken", arg.substring("--accessToken=".length()));
            } else if ("--accessToken".equals(arg) && i + 1 < args.length) {
                putOverride(values, "accessToken", args[++i]);
            } else if (arg.startsWith("--uuid=")) {
                putOverride(values, "uuid", arg.substring("--uuid=".length()));
            } else if ("--uuid".equals(arg) && i + 1 < args.length) {
                putOverride(values, "uuid", args[++i]);
            } else if (!arg.startsWith("--")) {
                if (positionalIndex == 0 && !isUsable(values.get("username"))) {
                    putFallback(values, "username", arg);
                } else if (positionalIndex == 1 && !hasUsableExplicitSession(values)) {
                    putFallback(values, "session", arg);
                }
                positionalIndex++;
            }
        }
    }

    private static String resolveSessionId(Map<String, String> values) {
        String explicitSession = firstUsable(values.get("session"), values.get("sessionid"));
        if (isExplicitSessionValid(explicitSession)) {
            return explicitSession;
        }

        String accessToken = trimToNull(values.get("accessToken"));
        String normalizedUuid = GameSession.normalizeUuid(values.get("uuid"));
        if (isUsable(accessToken) && isUsable(normalizedUuid)) {
            return GameSession.toModernSessionId(accessToken, normalizedUuid);
        }

        return isUsable(explicitSession) ? explicitSession : null;
    }

    private static boolean isExplicitSessionValid(String sessionId) {
        if (!isUsable(sessionId)) {
            return false;
        }
        if (!sessionId.startsWith("token:")) {
            return true;
        }
        GameSession session = new GameSession("Player", sessionId);
        return session.hasModernToken();
    }

    private static boolean hasUsableExplicitSession(Map<String, String> values) {
        return isUsable(values.get("session")) || isUsable(values.get("sessionid"));
    }

    private static void putFallback(Map<String, String> values, String key, String value) {
        if (!isUsable(values.get(key)) && isUsable(value)) {
            values.put(key, trimToNull(value));
        }
    }

    private static void putOverride(Map<String, String> values, String key, String value) {
        if (isUsable(value)) {
            values.put(key, trimToNull(value));
        }
    }

    private static String firstUsable(String primary, String fallback) {
        return isUsable(primary) ? trimToNull(primary) : fallback;
    }

    private static boolean isUsable(String value) {
        return trimToNull(value) != null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }

    private static String generateDefaultUsername() {
        return "Player" + (System.currentTimeMillis() % 1000L);
    }
}
