package net.mcose.friendsapi.auth;

import org.json.JSONObject;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;

/**
 * Mojang session authentication helpers for b1.7.3 modern-auth restoration.
 *
 * Client-side flow:
 * 1. Server sends Packet253EncryptionRequest with public key and verify token.
 * 2. Client computes the Mojang serverId hash with the shared secret.
 * 3. Client POSTs /session/minecraft/join with access token, selected profile,
 *    and serverId.
 * 4. Server verifies with /session/minecraft/hasJoined before completing login.
 */
public final class ModernAuthHelper {
    private static final String JOIN_URL = "https://sessionserver.mojang.com/session/minecraft/join";
    private static final String HAS_JOINED_URL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";

    private ModernAuthHelper() {
    }

    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        return keyGen.generateKey();
    }

    public static String generateServerId(String baseServerId, PublicKey publicKey, SecretKey secretKey) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        sha.update((baseServerId == null ? "" : baseServerId).getBytes("ISO_8859_1"));
        sha.update(secretKey.getEncoded());
        sha.update(publicKey.getEncoded());
        return new BigInteger(sha.digest()).toString(16);
    }

    public static String createJoinRequestJson(String accessToken, String profileId, String serverId) {
        JSONObject json = new JSONObject();
        json.put("accessToken", accessToken);
        json.put("selectedProfile", normalizeProfileId(profileId));
        json.put("serverId", serverId);
        return json.toString();
    }

    public static void authenticateWithMojang(String accessToken, String profileId, String serverId) throws Exception {
        byte[] body = createJoinRequestJson(accessToken, profileId, serverId).getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) new URL(JOIN_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Minecraft Client");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setDoOutput(true);
        OutputStream out = connection.getOutputStream();
        try {
            out.write(body);
        } finally {
            out.close();
        }
        int responseCode = connection.getResponseCode();
        if (responseCode != 204) {
            throw new IllegalStateException("Mojang join failed with HTTP " + responseCode + ": " + readResponse(connection));
        }
    }

    public static ModernSessionResponse hasJoined(String username, String serverId, String ip) {
        try {
            StringBuilder route = new StringBuilder(HAS_JOINED_URL);
            route.append("?username=").append(java.net.URLEncoder.encode(username, "UTF-8"));
            route.append("&serverId=").append(java.net.URLEncoder.encode(serverId, "UTF-8"));
            if (ip != null && ip.length() > 0 && !"127.0.0.1".equals(ip) && !"localhost".equalsIgnoreCase(ip)) {
                route.append("&ip=").append(java.net.URLEncoder.encode(ip, "UTF-8"));
            }
            HttpURLConnection connection = (HttpURLConnection) new URL(route.toString()).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "MCOSE-Friends-API/0.1");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int code = connection.getResponseCode();
            if (code == 204) {
                return new ModernSessionResponse(204, "", "", "");
            }
            if (code != 200) {
                return new ModernSessionResponse(code, "", "", "");
            }
            String response = readResponse(connection);
            JSONObject json = response.length() == 0 ? new JSONObject() : new JSONObject(response);
            return new ModernSessionResponse(
                    code,
                    json.optString("name", ""),
                    json.optString("id", ""),
                    json.optString("ip", ""));
        } catch (Exception e) {
            return new ModernSessionResponse(-1, "", "", "");
        }
    }

    public static boolean isRetryableStatusCode(int responseCode) {
        return responseCode == -1
                || responseCode == 408
                || responseCode == 429
                || responseCode == 500
                || responseCode == 502
                || responseCode == 503
                || responseCode == 504;
    }

    private static String normalizeProfileId(String profileId) {
        return profileId == null ? "" : profileId.replace("-", "");
    }

    private static String readResponse(HttpURLConnection connection) {
        try {
            InputStream stream = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
            if (stream == null) {
                return "";
            }
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
        } catch (Exception e) {
            return "";
        }
    }

    public static final class ModernSessionResponse {
        private final int responseCode;
        private final String username;
        private final String uuid;
        private final String ip;

        public ModernSessionResponse(int responseCode, String username, String uuid, String ip) {
            this.responseCode = responseCode;
            this.username = username == null ? "" : username;
            this.uuid = uuid == null ? "" : uuid;
            this.ip = ip == null ? "" : ip;
        }

        public int getResponseCode() { return responseCode; }
        public String getUsername() { return username; }
        public String getUuid() { return uuid; }
        public String getIp() { return ip; }
    }
}
