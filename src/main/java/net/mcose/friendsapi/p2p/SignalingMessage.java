package net.mcose.friendsapi.p2p;

import org.json.JSONObject;

/**
 * Opaque client-to-client message carried by Mojang/Oldschool signaling.
 */
public final class SignalingMessage {
    public static final String JOIN_REQUEST = "join_request";
    public static final String JOIN_ACCEPTED = "join_accepted";
    public static final String JOIN_REJECTED = "join_rejected";
    public static final String OFFER = "offer";
    public static final String ANSWER = "answer";
    public static final String ICE = "ice";

    private final String type;
    private final String sessionId;
    private final JSONObject payload;

    public SignalingMessage(String type, String sessionId, JSONObject payload) {
        this.type = type == null ? "" : type;
        this.sessionId = sessionId == null ? "" : sessionId;
        this.payload = payload == null ? new JSONObject() : payload;
    }

    public static SignalingMessage joinRequest(String sessionId) {
        return new SignalingMessage(JOIN_REQUEST, sessionId, null);
    }

    public static SignalingMessage joinAccepted(String sessionId) {
        return new SignalingMessage(JOIN_ACCEPTED, sessionId, null);
    }

    public static SignalingMessage joinRejected(String sessionId) {
        return new SignalingMessage(JOIN_REJECTED, sessionId, null);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("sessionId", sessionId);
        json.put("payload", payload);
        return json;
    }

    public static SignalingMessage fromJson(JSONObject json) {
        if (json == null) {
            return new SignalingMessage("", "", null);
        }
        return new SignalingMessage(
                json.optString("type", ""),
                json.optString("sessionId", ""),
                json.optJSONObject("payload"));
    }

    public String getType() { return type; }
    public String getSessionId() { return sessionId; }
    public JSONObject getPayload() { return payload; }
}
