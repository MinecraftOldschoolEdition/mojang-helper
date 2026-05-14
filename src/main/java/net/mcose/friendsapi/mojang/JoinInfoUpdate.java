package net.mcose.friendsapi.mojang;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Join-info update posted with presence.
 *
 * `value` is the opaque join token/address and `invites` are Mojang profile
 * UUIDs that may see or use that join target.
 */
public final class JoinInfoUpdate {
    private final String value;
    private final Set<UUID> invites;

    public JoinInfoUpdate(String value, Set<UUID> invites) {
        this.value = value;
        this.invites = invites == null ? Collections.<UUID>emptySet() : Collections.unmodifiableSet(new HashSet<UUID>(invites));
    }

    public String value() { return value; }
    public Set<UUID> invites() { return invites; }
}
