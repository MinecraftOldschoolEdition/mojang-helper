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
 * Engine-neutral version of the production FriendsManager orchestration.
 *
 * Original integration source: net.minecraft.src.friends.FriendsManager. The
 * real singleton also updates GUI toasts and presence on world transitions.
 * This reference class focuses on account switching, signed local metadata,
 * Mojang service synchronization, and username/UUID caching.
 */
public class FriendsManager {
    private final FriendsFileManager fileManager;
    private final Map<String, String> usernameToUuidCache = new HashMap<String, String>();

    private GameSession session;
    private FriendsService mojangFriendsService;
    private FriendsList currentFriendsList;
    private KeyPairData currentKeyPair;
    private FriendData mojangFriendData = FriendData.empty();
    private FriendPresence.VisibilityMode visibilityMode = FriendPresence.VisibilityMode.ONLINE;

    public FriendsManager(File baseDirectory) {
        this.fileManager = new FriendsFileManager(baseDirectory);
    }

    public void onSessionReady(GameSession session) {
        this.session = session;
        if (session == null || session.getProfileId() == null) {
            this.currentFriendsList = null;
            this.currentKeyPair = null;
            this.mojangFriendsService = null;
            return;
        }

        String ownerUuid = session.getProfileId();
        this.currentKeyPair = fileManager.loadKeyPair(ownerUuid);
        if (this.currentKeyPair == null || !this.currentKeyPair.isValid() || !this.currentKeyPair.matchesOwner(ownerUuid)) {
            this.currentKeyPair = FriendsCrypto.generateKeyPair(ownerUuid);
            fileManager.saveKeyPair(this.currentKeyPair);
        }

        this.currentFriendsList = fileManager.loadFriendsList(ownerUuid);
        if (this.currentFriendsList == null) {
            this.currentFriendsList = new FriendsList(ownerUuid);
            this.currentFriendsList.setOwnerPublicKey(this.currentKeyPair.getPublicKey());
            saveFriendsList();
        }

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
            mirrorMojangFriendsIntoLocalCache();
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
            if (profileId != null && currentFriendsList != null) {
                currentFriendsList.removeFriend(profileId.toString());
                saveFriendsList();
            }
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

    public boolean saveFriendsList() {
        return currentFriendsList != null
                && currentKeyPair != null
                && fileManager.saveFriendsList(currentFriendsList, currentKeyPair.getPrivateKey());
    }

    public void setVisibilityMode(FriendPresence.VisibilityMode visibilityMode) {
        this.visibilityMode = visibilityMode == null ? FriendPresence.VisibilityMode.ONLINE : visibilityMode;
    }

    public FriendPresence.VisibilityMode getVisibilityMode() {
        return visibilityMode;
    }

    public FriendsList getFriendsList() { return currentFriendsList; }
    public KeyPairData getKeyPair() { return currentKeyPair; }
    public FriendData getMojangFriendData() { return mojangFriendData; }
    public GameSession getSession() { return session; }

    public String getUuidByUsername(String username) {
        return username == null ? null : usernameToUuidCache.get(username.toLowerCase(java.util.Locale.ROOT));
    }

    private void mirrorMojangFriendsIntoLocalCache() {
        if (currentFriendsList == null) {
            return;
        }
        for (FriendDto friend : mojangFriendData.friends()) {
            String uuid = friend.profileId().toString().replace("-", "");
            usernameToUuidCache.put(friend.name().toLowerCase(java.util.Locale.ROOT), uuid);
            FriendEntry entry = currentFriendsList.getFriend(uuid);
            if (entry == null) {
                currentFriendsList.addFriend(new FriendEntry(uuid, friend.name()));
            } else {
                entry.setLastKnownName(friend.name());
            }
        }
        saveFriendsList();
    }
}
