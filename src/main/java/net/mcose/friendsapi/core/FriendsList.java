package net.mcose.friendsapi.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * In-memory mirror of Mojang's current friend list.
 */
public final class FriendsList {
    private final String ownerUuid;
    private final List<FriendEntry> friends;

    public FriendsList(String ownerUuid) {
        this.ownerUuid = FriendEntry.normalizeUuid(ownerUuid);
        this.friends = new ArrayList<FriendEntry>();
    }

    public boolean addFriend(FriendEntry friend) {
        if (friend == null || friend.getFriendUuid().length() == 0 || hasFriend(friend.getFriendUuid())) {
            return false;
        }
        friends.add(friend);
        sortFriends();
        return true;
    }

    public boolean removeFriend(String friendUuid) {
        String normalized = FriendEntry.normalizeUuid(friendUuid);
        for (int i = 0; i < friends.size(); i++) {
            if (friends.get(i).getFriendUuid().equals(normalized)) {
                friends.remove(i);
                return true;
            }
        }
        return false;
    }

    public FriendEntry getFriend(String friendUuid) {
        String normalized = FriendEntry.normalizeUuid(friendUuid);
        for (FriendEntry friend : friends) {
            if (friend.getFriendUuid().equals(normalized)) {
                return friend;
            }
        }
        return null;
    }

    public boolean hasFriend(String friendUuid) {
        return getFriend(friendUuid) != null;
    }

    public String getOwnerUuid() { return ownerUuid; }
    public int getFriendCount() { return friends.size(); }

    public List<FriendEntry> getFriends() {
        return Collections.unmodifiableList(sortedCopy());
    }

    private void sortFriends() {
        Collections.sort(friends, FRIEND_ORDER);
    }

    private List<FriendEntry> sortedCopy() {
        List<FriendEntry> copy = new ArrayList<FriendEntry>(friends);
        Collections.sort(copy, FRIEND_ORDER);
        return copy;
    }

    private static final Comparator<FriendEntry> FRIEND_ORDER = new Comparator<FriendEntry>() {
        public int compare(FriendEntry a, FriendEntry b) {
            return a.getFriendUuid().compareTo(b.getFriendUuid());
        }
    };
}
