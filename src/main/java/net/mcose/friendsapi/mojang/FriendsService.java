package net.mcose.friendsapi.mojang;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Engine-facing abstraction for the official friends service.
 */
public interface FriendsService {
    enum ResultCode {
        SUCCESS,
        REQUEST_LIMITED,
        NOT_ALLOWED,
        NOT_FOUND,
        CONNECTION_ISSUE,
        ERROR
    }

    ResultCode getFriendData(Consumer<FriendData> consumer);
    ResultCode removeFriend(UUID id);
    ResultCode acceptIncomingFriendRequest(UUID id);
    ResultCode declineIncomingFriendRequest(UUID id);
    ResultCode sendFriendRequest(String name);
    ResultCode sendFriendRequest(UUID id);
    ResultCode revokeOutgoingFriendRequest(UUID id);
    ResultCode updateFriendSettings(boolean friendsListEnabled, boolean allowInvites);
    PresenceResponse presence(String status, JoinInfoUpdate joinInfoUpdate);
}
