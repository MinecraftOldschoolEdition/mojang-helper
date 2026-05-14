package net.mcose.friendsapi.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Binary codecs for server-assisted friend verification.
 *
 * These payloads let an online-mode legacy server prove that two Mojang UUIDs
 * have mutually claimed each other without requiring both clients to be on a
 * modern protocol stack.
 */
public final class FriendsVerificationProtocol {
    private FriendsVerificationProtocol() {
    }

    public static byte[] createQueryPayload(String friendUuid) {
        return writeUtf(friendUuid);
    }

    public static QueryPayload readQueryPayload(byte[] payload) {
        return new QueryPayload(readUtf(payload));
    }

    public static byte[] createOnlinePayload(String friendUuid, boolean online, String name) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            out.writeUTF(friendUuid == null ? "" : friendUuid);
            out.writeBoolean(online);
            if (online) {
                out.writeUTF(name == null ? "" : name);
            }
            out.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static OnlinePayload readOnlinePayload(byte[] payload) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
            String friendUuid = in.readUTF();
            boolean online = in.readBoolean();
            String name = online && in.available() > 0 ? in.readUTF() : "";
            return new OnlinePayload(friendUuid, online, name);
        } catch (Exception e) {
            return new OnlinePayload("", false, "");
        }
    }

    public static byte[] createClaimPayload(String friendUuid, String signature, long addedAt, String publicKey) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            out.writeUTF(friendUuid == null ? "" : friendUuid);
            out.writeUTF(signature == null ? "" : signature);
            out.writeLong(addedAt);
            out.writeUTF(publicKey == null ? "" : publicKey);
            out.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static ClaimPayload readClaimPayload(byte[] payload) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
            return new ClaimPayload(in.readUTF(), in.readUTF(), in.readLong(), in.readUTF());
        } catch (Exception e) {
            return new ClaimPayload("", "", 0L, "");
        }
    }

    public static byte[] createConfirmPayload(String friendUuid, boolean mutualVerified) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            out.writeUTF(friendUuid == null ? "" : friendUuid);
            out.writeBoolean(mutualVerified);
            out.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static ConfirmPayload readConfirmPayload(byte[] payload) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
            return new ConfirmPayload(in.readUTF(), in.readBoolean());
        } catch (Exception e) {
            return new ConfirmPayload("", false);
        }
    }

    private static byte[] writeUtf(String value) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            out.writeUTF(value == null ? "" : value);
            out.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private static String readUtf(byte[] payload) {
        try {
            return new DataInputStream(new ByteArrayInputStream(payload)).readUTF();
        } catch (Exception e) {
            return "";
        }
    }

    public static final class QueryPayload {
        public final String friendUuid;
        public QueryPayload(String friendUuid) { this.friendUuid = friendUuid == null ? "" : friendUuid; }
    }

    public static final class OnlinePayload {
        public final String friendUuid;
        public final boolean online;
        public final String name;
        public OnlinePayload(String friendUuid, boolean online, String name) {
            this.friendUuid = friendUuid == null ? "" : friendUuid;
            this.online = online;
            this.name = name == null ? "" : name;
        }
    }

    public static final class ClaimPayload {
        public final String friendUuid;
        public final String signature;
        public final long addedAt;
        public final String publicKey;
        public ClaimPayload(String friendUuid, String signature, long addedAt, String publicKey) {
            this.friendUuid = friendUuid == null ? "" : friendUuid;
            this.signature = signature == null ? "" : signature;
            this.addedAt = addedAt;
            this.publicKey = publicKey == null ? "" : publicKey;
        }
    }

    public static final class ConfirmPayload {
        public final String friendUuid;
        public final boolean mutualVerified;
        public ConfirmPayload(String friendUuid, boolean mutualVerified) {
            this.friendUuid = friendUuid == null ? "" : friendUuid;
            this.mutualVerified = mutualVerified;
        }
    }
}
