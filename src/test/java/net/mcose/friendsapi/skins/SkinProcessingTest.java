package net.mcose.friendsapi.skins;

import org.json.JSONObject;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.*;

public class SkinProcessingTest {
    @Test
    public void legacySkinExpandsToModernCanvas() {
        BufferedImage legacy = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        legacy.setRGB(4, 16, 0x12345678);
        PlayerTextureInfo info = new PlayerTextureInfo("Player");

        BufferedImage processed = SkinImageProcessor.processSkinImage(legacy, info);
        assertEquals(64, processed.getWidth());
        assertEquals(64, processed.getHeight());
        assertEquals("classic", info.getSkinVariant());
        assertEquals(0xFF, (processed.getRGB(4, 16) >>> 24) & 255);
    }

    @Test
    public void profileParserFindsSlimSkinAndCape() {
        JSONObject textures = new JSONObject()
                .put("textures", new JSONObject()
                        .put("SKIN", new JSONObject()
                                .put("url", "https://textures.minecraft.net/texture/skinhash")
                                .put("metadata", new JSONObject().put("model", "slim")))
                        .put("CAPE", new JSONObject()
                                .put("url", "https://textures.minecraft.net/texture/capehash")));
        String encoded = Base64.getEncoder().encodeToString(textures.toString().getBytes(StandardCharsets.UTF_8));
        JSONObject profile = new JSONObject()
                .put("id", "11111111222233334444555555555555")
                .put("properties", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("name", "textures")
                                .put("value", encoded)
                                .put("signature", "sig")));

        PlayerTextureInfo info = SkinProfileParser.parseProfile("Player", profile.toString());
        assertTrue(info.isSlimModel());
        assertTrue(info.isSecureProfile());
        assertEquals("skinhash", info.getSkinTextureHash());
        assertEquals("https://textures.minecraft.net/texture/capehash", info.resolveCapeUrlForRender());
    }
}
