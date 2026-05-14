package net.mcose.friendsapi.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Versioned MCOSE mod-handshake helpers.
 *
 * This is the tiny subset the friends/model backport needs from the broader
 * engine protocol: feature negotiation plus the skin-parts sync payload.
 */
public final class ModProtocol {
    public static final int PROTOCOL_VERSION = 4;
    public static final int PROTOCOL_VERSION_LEGACY = 1;
    public static final int PROTOCOL_VERSION_ITEMS = 2;
    public static final int PROTOCOL_VERSION_EXPERIMENTAL = 5;
    private static final int FEATURE_BITS_INTRODUCED_IN = 2;

    public static final int FEATURE_CHUNK_ZSTD = 1 << 0;
    public static final int FEATURE_ITEM_STACK_V2 = 1 << 1;
    public static final int FEATURE_ITEM_COMPONENTS = 1 << 2;
    public static final int FEATURE_REGIONCORE_ITEMS = 1 << 3;
    public static final int FEATURE_ENTITY_WIRE_V2 = 1 << 4;
    public static final int FEATURE_ENTITY_DATA_V2 = 1 << 5;
    public static final int FEATURE_REGIONCORE_ENTITIES = 1 << 6;
    public static final int FEATURE_SKIN_PARTS_SYNC = 1 << 7;

    public static final String CHANNEL_HELLO = "MCOSE|MOD_HELLO";
    public static final String CHANNEL_HELLO_ACK = "MCOSE|MOD_HELLO_ACK";
    public static final String CHANNEL_SKIN_PARTS = "MCOSE|SKINPARTS";

    private ModProtocol() {
    }

    public static byte[] createHelloAckPayload(int protocolVersion, int negotiatedFeatures) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            out.writeInt(protocolVersion);
            if (supportsFeatureBits(protocolVersion)) {
                out.writeInt(negotiatedFeatures);
            }
            out.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static HelloInfo readHelloInfo(byte[] payload) {
        if (payload == null || payload.length < 4) {
            return new HelloInfo(-1, 0);
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
            int version = in.readInt();
            int featureBits = 0;
            if (supportsFeatureBits(version) && in.available() >= 4) {
                featureBits = in.readInt();
            }
            return new HelloInfo(version, featureBits);
        } catch (Exception e) {
            return new HelloInfo(-1, 0);
        }
    }

    public static boolean supportsFeatureBits(int version) {
        return version >= FEATURE_BITS_INTRODUCED_IN;
    }

    public static boolean isSupportedVersion(int version) {
        return version == PROTOCOL_VERSION
                || version == PROTOCOL_VERSION_LEGACY
                || version == PROTOCOL_VERSION_ITEMS
                || version == 3
                || version == PROTOCOL_VERSION_EXPERIMENTAL;
    }

    public static byte[] createSkinPartsPayload(String username, int modelPartMask) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            out.writeUTF(username == null ? "" : username);
            out.writeByte(modelPartMask & 0xFF);
            out.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static SkinPartsInfo readSkinPartsPayload(byte[] payload) {
        if (payload == null || payload.length == 0) {
            return new SkinPartsInfo("", 0x7F);
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
            String username = in.readUTF();
            int mask = in.readUnsignedByte();
            return new SkinPartsInfo(username, mask);
        } catch (Exception e) {
            return new SkinPartsInfo("", 0x7F);
        }
    }

    public static final class HelloInfo {
        public final int version;
        public final int featureBits;

        public HelloInfo(int version, int featureBits) {
            this.version = version;
            this.featureBits = featureBits;
        }
    }

    public static final class SkinPartsInfo {
        public final String username;
        public final int modelPartMask;

        public SkinPartsInfo(String username, int modelPartMask) {
            this.username = username == null ? "" : username;
            this.modelPartMask = modelPartMask & 0xFF;
        }
    }
}
