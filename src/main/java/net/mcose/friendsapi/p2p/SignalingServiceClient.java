package net.mcose.friendsapi.p2p;

import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Adapter for the signaling service used to exchange WebRTC offers/answers.
 *
 * The production client used a JSON-RPC websocket. This interface-shaped class
 * keeps the behavior clear without baking one websocket implementation into the
 * reference library.
 */
public class SignalingServiceClient {
    public interface Transport {
        CompletableFuture<Void> sendClientMessage(UUID peerPmid, JSONObject message);
        void close();
    }

    private final ModernSessionInfo session;
    private final Transport transport;

    public SignalingServiceClient(ModernSessionInfo session, Transport transport) {
        this.session = session;
        this.transport = transport;
    }

    public ModernSessionInfo getSession() {
        return session;
    }

    public CompletableFuture<Void> sendClientMessage(UUID peerPmid, SignalingMessage message) {
        if (transport == null) {
            CompletableFuture<Void> failed = new CompletableFuture<Void>();
            failed.completeExceptionally(new SignalingException("No signaling transport configured"));
            return failed;
        }
        return transport.sendClientMessage(peerPmid, message.toJson());
    }

    public void close() {
        if (transport != null) {
            transport.close();
        }
    }
}
