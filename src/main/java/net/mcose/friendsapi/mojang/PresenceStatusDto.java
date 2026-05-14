package net.mcose.friendsapi.mojang;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Presence row for a friend profile.
 */
public final class PresenceStatusDto {
    private final UUID profileId;
    private final UUID pmid;
    private final PresenceStatus status;
    private final JoinInfo joinInfo;
    private final Instant lastUpdated;

    public PresenceStatusDto(UUID profileId, UUID pmid, PresenceStatus status, JoinInfo joinInfo, Instant lastUpdated) {
        this.profileId = profileId;
        this.pmid = pmid;
        this.status = status == null ? PresenceStatus.OFFLINE : status;
        this.joinInfo = joinInfo;
        this.lastUpdated = lastUpdated;
    }

    public UUID profileId() { return profileId; }
    public UUID pmid() { return pmid; }
    public PresenceStatus status() { return status; }
    public JoinInfo joinInfo() { return joinInfo; }
    public Instant lastUpdated() { return lastUpdated; }

    public static final class JoinInfo {
        private final String value;
        private final Set<UUID> invites;

        public JoinInfo(String value, Set<UUID> invites) {
            this.value = value;
            this.invites = invites == null ? Collections.<UUID>emptySet() : Collections.unmodifiableSet(new HashSet<UUID>(invites));
        }

        public String value() { return value; }
        public Set<UUID> invites() { return invites; }
    }
}
