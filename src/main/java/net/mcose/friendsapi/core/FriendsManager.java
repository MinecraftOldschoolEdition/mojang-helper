package net.mcose.friendsapi.core;

import net.mcose.friendsapi.adapter.GameSession;
import net.mcose.friendsapi.mojang.FriendData;
import net.mcose.friendsapi.mojang.FriendDto;
import net.mcose.friendsapi.mojang.FriendsService;
import net.mcose.friendsapi.mojang.YggdrasilFriendsService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Engine-neutral Mojang friends orchestration.
 * The official service owns friend state; this class keeps only an in-memory
 * mirror for old-engine UI and join logic.
 */
public class FriendsManager {
    private final Map<String, String> usernameToUuidCache = new HashMap<String, String>();

    private GameSession session;
    private FriendsService mojangFriendsService;
    private FriendsList currentFriendsList;
    private FriendData mojangFriendData = FriendData.empty();
    private FriendPresence.VisibilityMode visibilityMode = FriendPresence.VisibilityMode.ONLINE;

    public FriendsManager() {
    }

    public FriendsManager(File ignoredBaseDirectory) {
    }

    public void onSessionReady(GameSession session) {
        this.session = session;
        this.usernameToUuidCache.clear();
        this.mojangFriendData = FriendData.empty();
        this.mojangFriendsService = null;

        if (session == null || session.getProfileId() == null) {
            this.currentFriendsList = null;
            return;
        }

        this.currentFriendsList = new FriendsList(session.getProfileId());
        if (session.hasModernToken()) {
            this.mojangFriendsService = new YggdrasilFriendsService(session.getAccessToken());
            refreshMojangFriendData();
        }
    }

    public FriendsService.ResultCode refreshMojangFriendData() {
        if (mojangFriendsService == null) {
            return FriendsService.ResultCode.ERROR;
        }
        FriendsService.ResultCode result = mojangFriendsService.getFriendData(data -> mojangFriendData = data == null ? FriendData.empty() : data);
        if (result == FriendsService.ResultCode.SUCCESS) {
            mirrorMojangFriends();
        }
        return result;
    }

    public FriendsService.ResultCode addFriendByName(String name) {
        if (mojangFriendsService == null) {
            return FriendsService.ResultCode.ERROR;
        }
        FriendsService.ResultCode result = mojangFriendsService.sendFriendRequest(name);
        if (result == FriendsService.ResultCode.SUCCESS) {
            refreshMojangFriendData();
        }
        return result;
    }

    public FriendsService.ResultCode removeFriend(UUID profileId) {
        if (mojangFriendsService == null) {
            return FriendsService.ResultCode.ERROR;
        }
        FriendsService.ResultCode result = mojangFriendsService.removeFriend(profileId);
        if (result == FriendsService.ResultCode.SUCCESS) {
            refreshMojangFriendData();
        }
        return result;
    }

    public FriendsService.ResultCode applySocialFriendSettings(boolean friendsListEnabled, boolean allowInvites) {
        if (mojangFriendsService == null) {
            return FriendsService.ResultCode.ERROR;
        }
        return mojangFriendsService.updateFriendSettings(friendsListEnabled, allowInvites);
    }

    public void setVisibilityMode(FriendPresence.VisibilityMode visibilityMode) {
        this.visibilityMode = visibilityMode == null ? FriendPresence.VisibilityMode.ONLINE : visibilityMode;
    }

    public FriendPresence.VisibilityMode getVisibilityMode() {
        return visibilityMode;
    }

    public FriendsList getFriendsList() { return currentFriendsList; }
    public FriendData getMojangFriendData() { return mojangFriendData; }
    public GameSession getSession() { return session; }

    public String getUuidByUsername(String username) {
        return username == null ? null : usernameToUuidCache.get(username.toLowerCase(java.util.Locale.ROOT));
    }

    private void mirrorMojangFriends() {
        if (session == null || session.getProfileId() == null) {
            this.currentFriendsList = null;
            return;
        }
        FriendsList rebuilt = new FriendsList(session.getProfileId());
        usernameToUuidCache.clear();
        for (FriendDto friend : mojangFriendData.friends()) {
            String uuid = friend.profileId().toString().replace("-", "");
            String name = friend.name();
            if (name != null && name.length() > 0) {
                usernameToUuidCache.put(name.toLowerCase(java.util.Locale.ROOT), uuid);
            }
            rebuilt.addFriend(new FriendEntry(uuid, name));
        }
        currentFriendsList = rebuilt;
    }
}
