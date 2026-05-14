package net.mcose.friendsapi.skins;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Parser for Mojang sessionserver profile texture properties.
 *
 * The signed profile response has a `properties` array. The entry named
 * `textures` contains Base64 JSON with SKIN/CAPE URLs and optional
 * metadata.model = slim. We only need the parsed result; signature validation
 * remains the launcher's/sessionserver responsibility in this reference kit.
 */
public final class SkinProfileParser {
    private SkinProfileParser() {
    }

    public static PlayerTextureInfo parseProfile(String username, String profileJson) {
        JSONObject root = new JSONObject(profileJson);
        PlayerTextureInfo info = new PlayerTextureInfo(username);
        info.setProfileId(root.optString("id", null));
        JSONArray properties = root.optJSONArray("properties");
        if (properties == null) {
            return info;
        }

        for (int i = 0; i < properties.length(); i++) {
            JSONObject property = properties.optJSONObject(i);
            if (property == null || !"textures".equals(property.optString("name"))) {
                continue;
            }
            String encoded = property.optString("value", "");
            if (encoded.length() == 0) {
                continue;
            }
            JSONObject textures = new JSONObject(new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8));
            JSONObject textureRoot = textures.optJSONObject("textures");
            if (textureRoot == null) {
                continue;
            }
            JSONObject skin = textureRoot.optJSONObject("SKIN");
            if (skin != null) {
                String url = skin.optString("url", null);
                info.setSkinUrl(url);
                info.setSkinTextureHash(extractTextureHash(url));
                JSONObject metadata = skin.optJSONObject("metadata");
                if (metadata != null && "slim".equals(metadata.optString("model"))) {
                    info.setSkinVariant("slim");
                } else {
                    info.setSkinVariant("classic");
                }
            }
            JSONObject cape = textureRoot.optJSONObject("CAPE");
            if (cape != null) {
                String capeUrl = cape.optString("url", null);
                info.setOfficialCapeUrl(capeUrl);
                info.setCapeUrl(capeUrl);
            }
            info.setSecureProfile(property.has("signature"));
        }
        return info;
    }

    public static String extractTextureHash(String url) {
        if (url == null) {
            return null;
        }
        int slash = url.lastIndexOf('/');
        String hash = slash >= 0 ? url.substring(slash + 1) : url;
        return hash.length() == 0 ? null : hash;
    }
}
