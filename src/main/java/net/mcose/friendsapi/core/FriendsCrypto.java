package net.mcose.friendsapi.core;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cryptographic helpers for local trust metadata.
 *
 * Java 8 does not have built-in Ed25519, so the real backport used BouncyCastle.
 * Signatures are over canonical UTF-8 text, never over pretty-printed JSON.
 */
public final class FriendsCrypto {
    private static final SecureRandom RANDOM = new SecureRandom();

    private FriendsCrypto() {
    }

    public static KeyPairData generateKeyPair(String ownerUuid) {
        Ed25519KeyPairGenerator generator = new Ed25519KeyPairGenerator();
        generator.init(new Ed25519KeyGenerationParameters(RANDOM));
        AsymmetricCipherKeyPair pair = generator.generateKeyPair();
        Ed25519PrivateKeyParameters privateKey = (Ed25519PrivateKeyParameters) pair.getPrivate();
        Ed25519PublicKeyParameters publicKey = (Ed25519PublicKeyParameters) pair.getPublic();
        return new KeyPairData(
                ownerUuid,
                Base64.getEncoder().encodeToString(publicKey.getEncoded()),
                Base64.getEncoder().encodeToString(privateKey.getEncoded()),
                "Ed25519");
    }

    public static String signContent(String content, String privateKeyBase64) {
        try {
            byte[] privateBytes = Base64.getDecoder().decode(privateKeyBase64);
            Ed25519PrivateKeyParameters privateKey = new Ed25519PrivateKeyParameters(privateBytes, 0);
            Ed25519Signer signer = new Ed25519Signer();
            signer.init(true, privateKey);
            byte[] message = content.getBytes(StandardCharsets.UTF_8);
            signer.update(message, 0, message.length);
            return Base64.getEncoder().encodeToString(signer.generateSignature());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to sign content", e);
        }
    }

    public static boolean verifySignature(String signatureBase64, String content, String publicKeyBase64) {
        try {
            byte[] publicBytes = Base64.getDecoder().decode(publicKeyBase64);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            Ed25519PublicKeyParameters publicKey = new Ed25519PublicKeyParameters(publicBytes, 0);
            Ed25519Signer verifier = new Ed25519Signer();
            verifier.init(false, publicKey);
            byte[] message = content.getBytes(StandardCharsets.UTF_8);
            verifier.update(message, 0, message.length);
            return verifier.verifySignature(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    public static String signFriendClaim(String ownerUuid, String friendUuid, long addedAt, String privateKeyBase64) {
        return signContent(friendClaimPayload(ownerUuid, friendUuid, addedAt), privateKeyBase64);
    }

    public static boolean verifyFriendClaim(String signatureBase64, String ownerUuid, String friendUuid, long addedAt, String publicKeyBase64) {
        return verifySignature(signatureBase64, friendClaimPayload(ownerUuid, friendUuid, addedAt), publicKeyBase64);
    }

    public static String friendClaimPayload(String ownerUuid, String friendUuid, long addedAt) {
        return FriendEntry.normalizeUuid(ownerUuid) + ":" + FriendEntry.normalizeUuid(friendUuid) + ":" + addedAt;
    }

    public static String computeSha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                out.append(String.format("%02x", b & 0xFF));
            }
            return out.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static String generateNonce() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
