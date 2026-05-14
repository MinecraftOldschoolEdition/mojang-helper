package net.mcose.friendsapi.protocol;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProtocolCodecTest {
    @Test
    public void skinPartsRoundTrip() {
        byte[] payload = ModProtocol.createSkinPartsPayload("Player", 0x8F);
        ModProtocol.SkinPartsInfo info = ModProtocol.readSkinPartsPayload(payload);
        assertEquals("Player", info.username);
        assertEquals(0x8F, info.modelPartMask);
    }

    @Test
    public void helloAckRoundTrip() {
        byte[] payload = ModProtocol.createHelloAckPayload(ModProtocol.PROTOCOL_VERSION, ModProtocol.FEATURE_SKIN_PARTS_SYNC);
        ModProtocol.HelloInfo info = ModProtocol.readHelloInfo(payload);
        assertEquals(ModProtocol.PROTOCOL_VERSION, info.version);
        assertEquals(ModProtocol.FEATURE_SKIN_PARTS_SYNC, info.featureBits);
    }
}
