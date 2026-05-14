package net.mcose.friendsapi.mojang;

import java.util.UUID;

/**
 * Mojang profile entry returned by the 1.22 friends endpoint.
 */
public final class FriendDto {
    private final UUID profileId;
    private final String name;

    public FriendDto(UUID profileId, String name) {
        this.profileId = profileId;
        this.name = name == null ? "" : name;
    }

    public UUID profileId() {
        return profileId;
    }

    public String name() {
        return name;
    }

    public boolean equals(Object other) {
        if (!(other instanceof FriendDto)) {
            return false;
        }
        FriendDto that = (FriendDto) other;
        return java.util.Objects.equals(this.profileId, that.profileId)
                && java.util.Objects.equals(this.name, that.name);
    }

    public int hashCode() {
        return java.util.Objects.hash(profileId, name);
    }
}
