package net.mcose.friendsapi.mojang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Full social graph response: accepted friends plus pending request buckets.
 */
public final class FriendData {
    private final List<FriendDto> friends;
    private final List<FriendDto> incomingRequests;
    private final List<FriendDto> outgoingRequests;

    public FriendData(List<FriendDto> friends, List<FriendDto> incomingRequests, List<FriendDto> outgoingRequests) {
        this.friends = immutableCopy(friends);
        this.incomingRequests = immutableCopy(incomingRequests);
        this.outgoingRequests = immutableCopy(outgoingRequests);
    }

    public static FriendData empty() {
        return new FriendData(Collections.<FriendDto>emptyList(), Collections.<FriendDto>emptyList(), Collections.<FriendDto>emptyList());
    }

    public List<FriendDto> friends() { return friends; }
    public List<FriendDto> incomingRequests() { return incomingRequests; }
    public List<FriendDto> outgoingRequests() { return outgoingRequests; }

    private static List<FriendDto> immutableCopy(List<FriendDto> value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<FriendDto>(value));
    }
}
