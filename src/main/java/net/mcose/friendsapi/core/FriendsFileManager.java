package net.mcose.friendsapi.core;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * File layout helper for the local friend metadata cache.
 *
 * Backport layout: one account directory per normalized UUID. This prevents a
 * shared old `.minecraft` directory from mixing private keys between accounts.
 */
public final class FriendsFileManager {
    public enum IntegrityResult {
        VALID,
        HASH_MISMATCH,
        SIGNATURE_MISMATCH,
        MISSING_INTEGRITY_DATA
    }

    private final File baseDir;

    public FriendsFileManager(File baseDir) {
        this.baseDir = baseDir;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getAccountDir(String ownerUuid) {
        return new File(baseDir, FriendEntry.normalizeUuid(ownerUuid));
    }

    public File getFriendsFile(String ownerUuid) {
        return new File(getAccountDir(ownerUuid), "friends.json");
    }

    public File getKeysFile(String ownerUuid) {
        return new File(getAccountDir(ownerUuid), "keys.json");
    }

    public boolean saveFriendsList(FriendsList friendsList, String privateKeyBase64) {
        if (friendsList == null || privateKeyBase64 == null) {
            return false;
        }
        String unsigned = friendsList.toUnsignedCanonicalJson();
        friendsList.setContentHash(FriendsCrypto.computeSha256(unsigned));
        friendsList.setFileSignature(FriendsCrypto.signContent(unsigned, privateKeyBase64));
        return writeFile(getFriendsFile(friendsList.getOwnerUuid()), friendsList.toJson(true).toString(2));
    }

    public FriendsList loadFriendsList(String ownerUuid) {
        String text = readFile(getFriendsFile(ownerUuid));
        if (text == null) {
            return null;
        }
        // Do not mutate lastLoadedAt on read. It is part of the signed payload,
        // so changing it before verification would make a valid file look
        // tampered. Callers can update and save it after a successful verify.
        return new FriendsList(new JSONObject(text));
    }

    public IntegrityResult verifyIntegrity(FriendsList friendsList, String publicKeyBase64) {
        if (friendsList == null || friendsList.getContentHash() == null || friendsList.getFileSignature() == null) {
            return IntegrityResult.MISSING_INTEGRITY_DATA;
        }
        String unsigned = friendsList.toUnsignedCanonicalJson();
        String expectedHash = FriendsCrypto.computeSha256(unsigned);
        if (!expectedHash.equalsIgnoreCase(friendsList.getContentHash())) {
            return IntegrityResult.HASH_MISMATCH;
        }
        if (publicKeyBase64 == null || !FriendsCrypto.verifySignature(friendsList.getFileSignature(), unsigned, publicKeyBase64)) {
            return IntegrityResult.SIGNATURE_MISMATCH;
        }
        return IntegrityResult.VALID;
    }

    public boolean saveKeyPair(KeyPairData keyPair) {
        if (keyPair == null || !keyPair.isValid()) {
            return false;
        }
        return writeFile(getKeysFile(keyPair.getOwnerUuid()), keyPair.toJson().toString(2));
    }

    public KeyPairData loadKeyPair(String ownerUuid) {
        String text = readFile(getKeysFile(ownerUuid));
        return text == null ? null : new KeyPairData(new JSONObject(text));
    }

    private boolean writeFile(File file, String text) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                return false;
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            try {
                writer.write(text);
            } finally {
                writer.close();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String readFile(File file) {
        if (file == null || !file.isFile()) {
            return null;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            try {
                StringBuilder out = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line).append('\n');
                }
                return out.toString();
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
