package net.mcose.friendsapi.p2p;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small orchestration shell matching the production FriendsP2PManager API.
 *
 * The real client owns UI toasts and local-server lifecycle. This reference
 * manager owns only session/signaling state and pending join acceptance.
 */
public final class FriendsP2PManager {
    private final Map<String, UUID> pendingJoinRequests = new ConcurrentHashMap<String, UUID>();
    private SignalingServiceClient signaling;
    private boolean hosting;
    private int hostingLocalPort;

    public void startHosting(ModernSessionInfo session, int localServerPort, SignalingServiceClient.Transport transport) throws IOException {
        if (session == null) {
            throw new IOException("Modern Mojang session required to host a friends world");
        }
        this.signaling = new SignalingServiceClient(session, transport);
        this.hosting = true;
        this.hostingLocalPort = localServerPort;
    }

    public CompletableFuture<Void> requestJoin(UUID peerPmid, String sessionId) {
        if (signaling == null) {
            CompletableFuture<Void> failed = new CompletableFuture<Void>();
            failed.completeExceptionally(new IOException("Not connected to signaling"));
            return failed;
        }
        return signaling.sendClientMessage(peerPmid, SignalingMessage.joinRequest(sessionId));
    }

    public void rememberIncomingJoin(String profileId, UUID pmid) {
        if (profileId != null && pmid != null) {
            pendingJoinRequests.put(profileId.replace("-", "").toLowerCase(java.util.Locale.ROOT), pmid);
        }
    }

    public boolean hasPendingJoinRequest(String profileId) {
        return profileId != null && pendingJoinRequests.containsKey(profileId.replace("-", "").toLowerCase(java.util.Locale.ROOT));
    }

    public UUID acceptPendingJoin(String profileId) {
        return profileId == null ? null : pendingJoinRequests.remove(profileId.replace("-", "").toLowerCase(java.util.Locale.ROOT));
    }

    public void stopHosting() {
        hosting = false;
        hostingLocalPort = 0;
        pendingJoinRequests.clear();
        if (signaling != null) {
            signaling.close();
            signaling = null;
        }
    }

    public boolean isHosting() { return hosting; }
    public int getHostingLocalPort() { return hostingLocalPort; }
}
