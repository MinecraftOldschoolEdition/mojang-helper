package net.mcose.friendsapi.mojang;

import org.json.JSONObject;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class MojangParsingTest {
    @Test
    public void parsesFriendBuckets() {
        JSONObject json = new JSONObject()
                .put("friends", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("profileId", "11111111222233334444555555555555")
                                .put("name", "Alpha")))
                .put("incomingRequests", new org.json.JSONArray())
                .put("outgoingRequests", new org.json.JSONArray());

        FriendData data = YggdrasilFriendsService.parseFriendData(json);
        assertEquals(1, data.friends().size());
        assertEquals("Alpha", data.friends().get(0).name());
        assertEquals(UUID.fromString("11111111-2222-3333-4444-555555555555"), data.friends().get(0).profileId());
    }

    @Test
    public void parsesPresenceJoinInfo() {
        JSONObject json = new JSONObject()
                .put("presence", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("profileId", "11111111222233334444555555555555")
                                .put("pmid", "aaaaaaaa222233334444555555555555")
                                .put("status", "ONLINE")
                                .put("joinInfo", new JSONObject()
                                        .put("value", "p2p:abc")
                                        .put("invites", new org.json.JSONArray()
                                                .put("bbbbbbbb222233334444555555555555")))));

        PresenceResponse response = YggdrasilFriendsService.parsePresenceResponse(json);
        assertEquals(1, response.presence().size());
        assertEquals(PresenceStatus.ONLINE, response.presence().get(0).status());
        assertEquals("p2p:abc", response.presence().get(0).joinInfo().value());
        assertEquals(1, response.presence().get(0).joinInfo().invites().size());
    }
}
