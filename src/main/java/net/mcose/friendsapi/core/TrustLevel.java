package net.mcose.friendsapi.core;

/**
 * Friend trust state carried over from the client backport.
 *
 * ONE_SIDED means "we added them"; MUTUAL means both users proved they added
 * one another; SUSPICIOUS means the signed list or peer claim stopped matching.
 */
public enum TrustLevel {
    ONE_SIDED("friends.oneSided", 0xFFFF55),
    MUTUAL("friends.mutual", 0x55FF55),
    SUSPICIOUS("friends.suspicious", 0xFF5555);

    private final String translationKey;
    private final int color;

    TrustLevel(String translationKey, int color) {
        this.translationKey = translationKey;
        this.color = color;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public int getColor() {
        return color;
    }

    public boolean isMutual() {
        return this == MUTUAL;
    }

    public boolean isSuspicious() {
        return this == SUSPICIOUS;
    }

    public static TrustLevel fromName(String value) {
        if (value == null) {
            return ONE_SIDED;
        }
        for (TrustLevel level : values()) {
            if (level.name().equalsIgnoreCase(value)) {
                return level;
            }
        }
        return ONE_SIDED;
    }
}
