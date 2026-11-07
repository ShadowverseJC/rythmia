package com.example.rythemia.account;

import androidx.annotation.NonNull;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Turns passwords into salted PBKDF2 hashes so the password itself is never stored.
 *
 * <p>PBKDF2 is deliberately slow, which makes guessing passwords from a copied database
 * expensive. HMAC-SHA1 is used because the SHA-256 variant only exists from API 26 and the app
 * supports API 24; for PBKDF2 that is still sound. The 20-byte output is one SHA-1 block:
 * asking for more would double our cost without slowing an attacker down.
 */
public final class PasswordHasher {

    /**
     * Iterations for new hashes: about half a second per hash on the test emulator, which is
     * as long as a login should take. Each hash records its own count, so this can rise later
     * without breaking existing accounts.
     */
    public static final int DEFAULT_ITERATIONS = 210_000;

    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 160;

    private final SecureRandom random = new SecureRandom();
    private final int iterations;

    /** A stored password: the derived hash plus what is needed to check a guess against it. */
    public static final class Hash {
        @NonNull public final byte[] hash;
        @NonNull public final byte[] salt;
        public final int iterations;

        public Hash(@NonNull byte[] hash, @NonNull byte[] salt, int iterations) {
            this.hash = hash;
            this.salt = salt;
            this.iterations = iterations;
        }
    }

    public PasswordHasher() {
        this(DEFAULT_ITERATIONS);
    }

    /** Tests use a small count to stay fast. */
    PasswordHasher(int iterations) {
        this.iterations = iterations;
    }

    /** Hashes {@code password} with a new random salt. */
    @NonNull
    public Hash hash(@NonNull char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return new Hash(derive(password, salt, iterations), salt, iterations);
    }

    /** Whether {@code password} matches {@code stored}, compared in constant time. */
    public boolean verify(@NonNull char[] password, @NonNull Hash stored) {
        return MessageDigest.isEqual(derive(password, stored.salt, stored.iterations), stored.hash);
    }

    static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(ALGORITHM + " is unavailable", e);
        } finally {
            spec.clearPassword();
        }
    }
}
