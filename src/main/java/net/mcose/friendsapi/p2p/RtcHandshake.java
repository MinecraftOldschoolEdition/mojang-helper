package net.mcose.friendsapi.p2p;

import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebRTC handshake state machine with engine-specific WebRTC objects abstracted.
 *
 * In client-source this is backed by dev.onvoid.webrtc. For open-source
 * portability, the peer object is an adapter that knows how to create offers,
 * apply answers, and emit ICE candidates.
 */
public final class RtcHandshake {
    public interface PeerAdapter {
        CompletableFuture<JSONObject> createOffer();
        CompletableFuture<JSONObject> createAnswer(JSONObject offer);
        CompletableFuture<Void> setRemoteAnswer(JSONObject answer);
        CompletableFuture<Void> addIceCandidate(JSONObject candidate);
        void close();
    }

    private final String sessionId;
    private final PeerAdapter peer;
    private final AtomicBoolean aborted = new AtomicBoolean(false);

    public RtcHandshake(String sessionId, PeerAdapter peer) {
        this.sessionId = sessionId;
        this.peer = peer;
    }

    public String getSessionId() {
        return sessionId;
    }

    public CompletableFuture<SignalingMessage> createOfferMessage() {
        return peer.createOffer().thenApply(offer -> new SignalingMessage(SignalingMessage.OFFER, sessionId, offer));
    }

    public CompletableFuture<SignalingMessage> createAnswerMessage(JSONObject offer) {
        return peer.createAnswer(offer).thenApply(answer -> new SignalingMessage(SignalingMessage.ANSWER, sessionId, answer));
    }

    public CompletableFuture<Void> acceptAnswer(JSONObject answer) {
        return peer.setRemoteAnswer(answer);
    }

    public CompletableFuture<Void> addIceCandidate(JSONObject candidate) {
        return peer.addIceCandidate(candidate);
    }

    public void abort(String reason) {
        if (aborted.compareAndSet(false, true)) {
            peer.close();
        }
    }

    public boolean isAborted() {
        return aborted.get();
    }
}
