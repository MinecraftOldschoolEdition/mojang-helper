package net.mcose.friendsapi.core;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class CoreStorageTest {
    private static final String OWNER = "11111111222233334444555555555555";
    private static final String FRIEND_A = "aaaaaaaa222233334444555555555555";
    private static final String FRIEND_B = "bbbbbbbb222233334444555555555555";

    @Test
    public void signsAndVerifiesContent() {
        KeyPairData keyPair = FriendsCrypto.generateKeyPair(OWNER);
        String message = "friend-proof";
        String signature = FriendsCrypto.signContent(message, keyPair.getPrivateKey());
        assertTrue(FriendsCrypto.verifySignature(signature, message, keyPair.getPublicKey()));
        assertFalse(FriendsCrypto.verifySignature(signature, message + "!", keyPair.getPublicKey()));
    }

    @Test
    public void canonicalJsonSortsFriendRows() {
        FriendsList list = new FriendsList(OWNER);
        list.addFriend(new FriendEntry(FRIEND_B, "Beta", 2L));
        list.addFriend(new FriendEntry(FRIEND_A, "Alpha", 1L));

        String canonical = list.toUnsignedCanonicalJson();
        assertTrue(canonical.indexOf(FRIEND_A) < canonical.indexOf(FRIEND_B));
    }

    @Test
    public void fileIntegrityDetectsTamper() throws Exception {
        File root = Files.createTempDirectory("friends-api-test").toFile();
        FriendsFileManager files = new FriendsFileManager(root);
        KeyPairData keyPair = FriendsCrypto.generateKeyPair(OWNER);
        FriendsList list = new FriendsList(OWNER);
        list.setOwnerPublicKey(keyPair.getPublicKey());
        list.addFriend(new FriendEntry(FRIEND_A, "Alpha", 1L));

        assertTrue(files.saveKeyPair(keyPair));
        assertTrue(files.saveFriendsList(list, keyPair.getPrivateKey()));

        FriendsList loaded = files.loadFriendsList(OWNER);
        assertEquals(FriendsFileManager.IntegrityResult.VALID, files.verifyIntegrity(loaded, keyPair.getPublicKey()));

        loaded.getFriend(FRIEND_A).setLastKnownName("Changed");
        assertEquals(FriendsFileManager.IntegrityResult.HASH_MISMATCH, files.verifyIntegrity(loaded, keyPair.getPublicKey()));
    }
}
