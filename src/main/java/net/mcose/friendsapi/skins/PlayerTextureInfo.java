package net.mcose.friendsapi.skins;

/**
 * Engine-neutral texture/profile state for one player.
 *
 * Original integration source: net.minecraft.src.PlayerTextureInfo. Render
 * code reads this to decide classic vs slim arms, layer visibility, cape source,
 * and whether a secure Mojang profile was returned.
 */
public final class PlayerTextureInfo {
    private final String username;
    private String skinUrl;
    private String capeUrl;
    private String officialCapeUrl;
    private String oldschoolCapeUrl;
    private boolean hasCustomCapeAssignment;
    private String skinVariant = "classic";
    private boolean secureProfile;
    private String profileId;
    private String skinTextureHash;
    private int modelPartMask = SkinModelPartMask.ALL_VISIBLE;

    public PlayerTextureInfo(String username) {
        this.username = username == null ? "" : username;
    }

    public String getUsername() { return username; }
    public String getSkinUrl() { return skinUrl; }
    public String getCapeUrl() { return capeUrl; }
    public String getOfficialCapeUrl() { return officialCapeUrl; }
    public String getOldschoolCapeUrl() { return oldschoolCapeUrl; }
    public boolean hasCustomCapeAssignment() { return hasCustomCapeAssignment; }
    public String getSkinVariant() { return skinVariant; }
    public boolean isSlimModel() { return "slim".equals(skinVariant); }
    public boolean isSecureProfile() { return secureProfile; }
    public String getProfileId() { return profileId; }
    public String getSkinTextureHash() { return skinTextureHash; }
    public int getModelPartMask() { return modelPartMask; }

    public void setSkinUrl(String skinUrl) { this.skinUrl = emptyToNull(skinUrl); }
    public void setCapeUrl(String capeUrl) { this.capeUrl = emptyToNull(capeUrl); }
    public void setOfficialCapeUrl(String officialCapeUrl) { this.officialCapeUrl = emptyToNull(officialCapeUrl); }
    public void setOldschoolCapeUrl(String oldschoolCapeUrl) { this.oldschoolCapeUrl = emptyToNull(oldschoolCapeUrl); }
    public void setCustomCapeAssignment(boolean hasCustomCapeAssignment) { this.hasCustomCapeAssignment = hasCustomCapeAssignment; }
    public void setSkinVariant(String skinVariant) { this.skinVariant = "slim".equals(skinVariant) ? "slim" : "classic"; }
    public void setSecureProfile(boolean secureProfile) { this.secureProfile = secureProfile; }
    public void setProfileId(String profileId) { this.profileId = emptyToNull(profileId); }
    public void setSkinTextureHash(String skinTextureHash) { this.skinTextureHash = emptyToNull(skinTextureHash); }
    public void setModelPartMask(int modelPartMask) { this.modelPartMask = SkinModelPartMask.normalizedForSync(modelPartMask); }

    public boolean isModelPartEnabled(int modelPartBit) {
        return SkinModelPartMask.isEnabled(modelPartMask, modelPartBit);
    }

    public boolean prefersOfficialCapeSource() {
        return SkinModelPartMask.isEnabled(modelPartMask, SkinModelPartMask.CAPE_SOURCE_OFFICIAL);
    }

    public String resolveCapeUrlForRender() {
        if (prefersOfficialCapeSource()) {
            if (officialCapeUrl != null) {
                return officialCapeUrl;
            }
            if (oldschoolCapeUrl != null || hasCustomCapeAssignment) {
                return null;
            }
            return capeUrl;
        }
        if (oldschoolCapeUrl != null) {
            return oldschoolCapeUrl;
        }
        if (officialCapeUrl != null) {
            return officialCapeUrl;
        }
        return capeUrl;
    }

    private static String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }
}
