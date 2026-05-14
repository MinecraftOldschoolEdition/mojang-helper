package net.mcose.friendsapi.adapter;

/**
 * Minimal send hook used by protocol examples.
 *
 * In client-source this maps to NetClientHandler.addToSendQueue(...). On the
 * server it maps to NetServerHandler.sendPacket(...). Keeping it this small is
 * deliberate: the library never needs to know about Minecraft packet classes.
 */
public interface PacketTransport {
    void send(CustomPayload payload);
}
