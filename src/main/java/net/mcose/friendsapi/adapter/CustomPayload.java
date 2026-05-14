package net.mcose.friendsapi.adapter;

import java.util.Arrays;

/**
 * Engine-neutral stand-in for Packet250CustomPayload.
 *
 * The real b1.7.3 implementation sends these through NetClientHandler and
 * NetServerHandler. The reference kit keeps only the stable wire contract:
 * channel name plus raw byte payload.
 */
public final class CustomPayload {
    private final String channel;
    private final byte[] data;

    public CustomPayload(String channel, byte[] data) {
        this.channel = channel == null ? "" : channel;
        this.data = data == null ? new byte[0] : Arrays.copyOf(data, data.length);
    }

    public String getChannel() {
        return channel;
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}
