package net.mcose.friendsapi.auth;

import net.mcose.friendsapi.adapter.GameSession;
import org.json.JSONObject;
import org.junit.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SessionBootstrapTest {
    @Test
    public void commandLineAccessTokenAndUuidBecomeModernSession() {
        GameSession session = SessionBootstrap.resolve(
                new HashMap<String, String>(),
                new String[] {
                        "--username", "eric",
                        "--accessToken", "abc",
                        "--uuid", "11111111-2222-3333-4444-555555555555"
                },
                "Player",
                "-");

        assertEquals("eric", session.getUsername());
        assertEquals("abc", session.getAccessToken());
        assertEquals("11111111222233334444555555555555", session.getProfileId());
    }

    @Test
    public void explicitSessionWinsOverTokenPieces() {
        Map<String, String> base = new HashMap<String, String>();
        base.put("session", "legacy-session");
        base.put("accessToken", "abc");
        base.put("uuid", "11111111222233334444555555555555");
        GameSession session = SessionBootstrap.resolve(base, new String[] {"Steve"}, "Player", "-");
        assertEquals("legacy-session", session.getSessionId());
    }

    @Test
    public void joinRequestJsonUsesUndashedProfileId() {
        String json = ModernAuthHelper.createJoinRequestJson("token", "11111111-2222-3333-4444-555555555555", "server");
        JSONObject parsed = new JSONObject(json);
        assertEquals("11111111222233334444555555555555", parsed.getString("selectedProfile"));
    }

    @Test
    public void serverIdHashIsStableForInputs() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair pair = generator.generateKeyPair();
        SecretKey secret = new SecretKeySpec(new byte[16], "AES");

        String first = ModernAuthHelper.generateServerId("", pair.getPublic(), secret);
        String second = ModernAuthHelper.generateServerId("", pair.getPublic(), secret);
        assertEquals(first, second);
    }
}
