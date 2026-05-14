package net.mcose.friendsapi.skins;

/**
 * Mirrors modern player model-part bits used by the backport.
 *
 * Bits 0-6 are visible body/cape layers. Bit 7 is an MCOSE extension used only
 * for sync packets: it tells old clients to prefer Mojang's official cape over
 * an Oldschool custom cape assignment.
 */
public final class SkinModelPartMask {
    public static final int CAPE = 0x01;
    public static final int JACKET = 0x02;
    public static final int LEFT_SLEEVE = 0x04;
    public static final int RIGHT_SLEEVE = 0x08;
    public static final int LEFT_PANTS_LEG = 0x10;
    public static final int RIGHT_PANTS_LEG = 0x20;
    public static final int HAT = 0x40;
    public static final int CAPE_SOURCE_OFFICIAL = 0x80;

    public static final int ALL_VISIBLE = CAPE
            | JACKET
            | LEFT_SLEEVE
            | RIGHT_SLEEVE
            | LEFT_PANTS_LEG
            | RIGHT_PANTS_LEG
            | HAT;

    public static final int SYNC_MASK = ALL_VISIBLE | CAPE_SOURCE_OFFICIAL;

    private SkinModelPartMask() {
    }

    public static boolean isEnabled(int mask, int bit) {
        return (mask & bit) != 0;
    }

    public static int normalizedForSync(int mask) {
        return mask & SYNC_MASK;
    }
}
