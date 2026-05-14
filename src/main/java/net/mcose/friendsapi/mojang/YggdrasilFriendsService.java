package net.mcose.friendsapi.mojang;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Java 8 compatible port of Mojang's 1.22 YggdrasilFriendsService shape.
 *
 * Original integration source: net.minecraft.src.friends.mojang in
 * client-source. This version keeps the route/DTO/result-code behavior while
 * remaining independent from Minecraft classes.
 */
public class YggdrasilFriendsService implements FriendsService {
    private static final String SERVICES_HOST = "https://api.minecraftservices.com";
    private static final long REQUEST_COOLDOWN_MS = 10000L;
    private static final int TIMEOUT_MS = 10000;

    private final String accessToken;
    private final Proxy proxy;
    private final String routeFriends;
    private final String routePrivileges;
    private final String routePresence;
    private final AtomicBoolean requestPending = new AtomicBoolean(false);

    private String friendsEtag;
    private FriendData friendsCache = FriendData.empty();
    private String presenceEtag;
    private PresenceResponse presenceCache = PresenceResponse.empty();
    private long requestCooldownUntil;

    public YggdrasilFriendsService(String accessToken) {
        this(accessToken, Proxy.NO_PROXY);
    }

    public YggdrasilFriendsService(String accessToken, Proxy proxy) {
        this.accessToken = accessToken;
        this.proxy = proxy == null ? Proxy.NO_PROXY : proxy;
        this.routeFriends = SERVICES_HOST + "/friends";
        this.routePrivileges = SERVICES_HOST + "/player/attributes";
        this.routePresence = SERVICES_HOST + "/presence";
    }

    public ResultCode getFriendData(Consumer<FriendData> consumer) {
        if (!requestPending.compareAndSet(false, true)) {
            while (requestPending.get()) {
                Thread.yield();
            }
            consumer.accept(friendsCache);
            return ResultCode.SUCCESS;
        }

        try {
            if (canMakeRequest()) {
                requestCooldownUntil = System.currentTimeMillis() + REQUEST_COOLDOWN_MS;
                return requestFriendData(consumer);
            }
            consumer.accept(friendsCache);
            return ResultCode.SUCCESS;
        } finally {
            requestPending.set(false);
        }
    }

    public ResultCode removeFriend(UUID id) { return putFriendAction(null, id, "REMOVE"); }
    public ResultCode acceptIncomingFriendRequest(UUID id) { return putFriendAction(null, id, "ADD"); }
    public ResultCode declineIncomingFriendRequest(UUID id) { return putFriendAction(null, id, "REMOVE"); }
    public ResultCode sendFriendRequest(String name) { return putFriendAction(name, null, "ADD"); }
    public ResultCode sendFriendRequest(UUID id) { return putFriendAction(null, id, "ADD"); }
    public ResultCode revokeOutgoingFriendRequest(UUID id) { return putFriendAction(null, id, "REMOVE"); }

    public ResultCode updateFriendSettings(boolean friendsListEnabled, boolean allowInvites) {
        JSONObject friendsPreferences = new JSONObject();
        friendsPreferences.put("friends", friendsListEnabled ? "ENABLED" : "DISABLED");
        friendsPreferences.put("acceptInvites", allowInvites ? "ENABLED" : "DISABLED");

        JSONObject request = new JSONObject();
        request.put("friendsPreferences", friendsPreferences);

        try {
            HttpResult result = send("POST", routePrivileges, request.toString(), null);
            return result.statusCode < 400 ? ResultCode.SUCCESS : mapHttpStatus(result.statusCode);
        } catch (IOException e) {
            return ResultCode.CONNECTION_ISSUE;
        } catch (Exception e) {
            return ResultCode.ERROR;
        }
    }

    public PresenceResponse presence(String status, JoinInfoUpdate joinInfoUpdate) {
        PresenceStatus presenceStatus = parsePresenceStatus(status);
        JSONObject request = new JSONObject();
        request.put("status", presenceStatus.name());
        if (joinInfoUpdate != null) {
            JSONObject joinInfo = new JSONObject();
            if (joinInfoUpdate.value() != null) {
                joinInfo.put("value", joinInfoUpdate.value());
            }
            JSONArray invites = new JSONArray();
            for (UUID invite : joinInfoUpdate.invites()) {
                invites.put(toUndashedUuid(invite));
            }
            joinInfo.put("invites", invites);
            request.put("joinInfo", joinInfo);
        }

        try {
            HttpResult result = send("POST", routePresence, request.toString(), presenceEtag);
            if (result.statusCode == HttpURLConnection.HTTP_NOT_MODIFIED || result.body == null || result.body.length() == 0) {
                if (result.etag != null) {
                    presenceEtag = result.etag;
                }
                return presenceCache;
            }
            if (result.statusCode >= 400) {
                mapHttpStatus(result.statusCode);
                return presenceCache;
            }
            presenceEtag = result.etag;
            presenceCache = parsePresenceResponse(new JSONObject(result.body));
            return presenceCache;
        } catch (Exception e) {
            return presenceCache;
        }
    }

    public static FriendData parseFriendData(JSONObject root) {
        if (root == null) {
            return FriendData.empty();
        }
        return new FriendData(
                parseFriendArray(root.optJSONArray("friends")),
                parseFriendArray(root.optJSONArray("incomingRequests")),
                parseFriendArray(root.optJSONArray("outgoingRequests")));
    }

    public static PresenceResponse parsePresenceResponse(JSONObject root) {
        if (root == null) {
            return PresenceResponse.empty();
        }
        JSONArray array = root.optJSONArray("presence");
        if (array == null) {
            array = root.optJSONArray("statuses");
        }
        if (array == null) {
            return PresenceResponse.empty();
        }

        List<PresenceStatusDto> presence = new ArrayList<PresenceStatusDto>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.optJSONObject(i);
            if (entry == null) {
                continue;
            }
            UUID profileId = parseUuid(entry.optString("profileId", null));
            UUID pmid = parseUuid(entry.optString("pmid", null));
            PresenceStatus status = parsePresenceStatus(entry.optString("status", null));
            PresenceStatusDto.JoinInfo joinInfo = parseJoinInfo(entry.optJSONObject("joinInfo"));
            Instant lastUpdated = parseInstant(entry.optString("lastUpdated", null));
            presence.add(new PresenceStatusDto(profileId, pmid, status, joinInfo, lastUpdated));
        }
        return new PresenceResponse(presence);
    }

    public static UUID parseUuid(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replace("-", "");
        if (normalized.length() != 32) {
            return null;
        }
        String dashed = normalized.substring(0, 8) + "-"
                + normalized.substring(8, 12) + "-"
                + normalized.substring(12, 16) + "-"
                + normalized.substring(16, 20) + "-"
                + normalized.substring(20);
        try {
            return UUID.fromString(dashed);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String toUndashedUuid(UUID uuid) {
        return uuid == null ? null : uuid.toString().replace("-", "");
    }

    private ResultCode requestFriendData(Consumer<FriendData> consumer) {
        try {
            HttpResult result = send("GET", routeFriends, null, friendsEtag);
            if (result.statusCode == HttpURLConnection.HTTP_NOT_MODIFIED || result.body == null || result.body.length() == 0) {
                consumer.accept(friendsCache);
                return ResultCode.SUCCESS;
            }
            if (result.statusCode >= 400) {
                consumer.accept(friendsCache);
                return mapHttpStatus(result.statusCode);
            }
            friendsEtag = result.etag;
            friendsCache = parseFriendData(new JSONObject(result.body));
            consumer.accept(friendsCache);
            return ResultCode.SUCCESS;
        } catch (IOException e) {
            consumer.accept(friendsCache);
            return ResultCode.CONNECTION_ISSUE;
        } catch (Exception e) {
            consumer.accept(friendsCache);
            return ResultCode.ERROR;
        }
    }

    private ResultCode putFriendAction(String name, UUID profileId, String updateType) {
        JSONObject request = new JSONObject();
        if (name != null && name.trim().length() > 0) {
            request.put("name", name.trim());
        }
        if (profileId != null) {
            request.put("profileId", toUndashedUuid(profileId));
        }
        request.put("updateType", updateType);

        try {
            HttpResult result = send("PUT", routeFriends, request.toString(), null);
            if (result.statusCode >= 400) {
                return mapHttpStatus(result.statusCode);
            }
            requestCooldownUntil = 0L;
            friendsEtag = null;
            if (result.body != null && result.body.length() > 0) {
                friendsCache = parseFriendData(new JSONObject(result.body));
            }
            return ResultCode.SUCCESS;
        } catch (IOException e) {
            return ResultCode.CONNECTION_ISSUE;
        } catch (Exception e) {
            return ResultCode.ERROR;
        }
    }

    private boolean canMakeRequest() {
        return requestCooldownUntil == 0L || System.currentTimeMillis() > requestCooldownUntil;
    }

    private HttpResult send(String method, String route, String body, String etag) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(route).openConnection(proxy);
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", "MCOSE-Friends-API/0.1");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        if (etag != null) {
            connection.setRequestProperty("If-None-Match", etag);
        }
        if (body != null) {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            OutputStream out = connection.getOutputStream();
            try {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            } finally {
                out.close();
            }
        }

        int status = connection.getResponseCode();
        InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String responseBody = stream == null ? "" : readFully(stream);
        return new HttpResult(status, responseBody, connection.getHeaderField("ETag"));
    }

    private static String readFully(InputStream stream) throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            stream.close();
        }
    }

    private static ResultCode mapHttpStatus(int status) {
        if (status == 403) {
            return ResultCode.NOT_ALLOWED;
        }
        if (status == 404) {
            return ResultCode.NOT_FOUND;
        }
        if (status == 408 || status == 429) {
            return ResultCode.REQUEST_LIMITED;
        }
        if (status >= 500) {
            return ResultCode.CONNECTION_ISSUE;
        }
        return ResultCode.ERROR;
    }

    private static List<FriendDto> parseFriendArray(JSONArray array) {
        if (array == null) {
            return Collections.emptyList();
        }
        List<FriendDto> out = new ArrayList<FriendDto>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.optJSONObject(i);
            if (entry == null) {
                continue;
            }
            UUID profileId = parseUuid(entry.optString("profileId", entry.optString("id", null)));
            String name = entry.optString("name", "");
            if (profileId != null) {
                out.add(new FriendDto(profileId, name));
            }
        }
        return out;
    }

    private static PresenceStatusDto.JoinInfo parseJoinInfo(JSONObject joinInfo) {
        if (joinInfo == null) {
            return null;
        }
        String value = joinInfo.optString("value", null);
        Set<UUID> invites = new java.util.LinkedHashSet<UUID>();
        JSONArray inviteArray = joinInfo.optJSONArray("invites");
        if (inviteArray != null) {
            for (int i = 0; i < inviteArray.length(); i++) {
                UUID uuid = parseUuid(inviteArray.optString(i, null));
                if (uuid != null) {
                    invites.add(uuid);
                }
            }
        }
        return new PresenceStatusDto.JoinInfo(value, invites);
    }

    private static PresenceStatus parsePresenceStatus(String status) {
        if (status == null) {
            return PresenceStatus.OFFLINE;
        }
        try {
            return PresenceStatus.valueOf(status.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return PresenceStatus.OFFLINE;
        }
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static final class HttpResult {
        final int statusCode;
        final String body;
        final String etag;

        HttpResult(int statusCode, String body, String etag) {
            this.statusCode = statusCode;
            this.body = body;
            this.etag = etag;
        }
    }
}
