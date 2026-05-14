package net.mcose.friendsapi.mojang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Presence response wrapper.
 */
public final class PresenceResponse {
    private final List<PresenceStatusDto> presence;

    public PresenceResponse(List<PresenceStatusDto> presence) {
        if (presence == null || presence.isEmpty()) {
            this.presence = Collections.emptyList();
        } else {
            this.presence = Collections.unmodifiableList(new ArrayList<PresenceStatusDto>(presence));
        }
    }

    public static PresenceResponse empty() {
        return new PresenceResponse(Collections.<PresenceStatusDto>emptyList());
    }

    public List<PresenceStatusDto> presence() {
        return presence;
    }
}
