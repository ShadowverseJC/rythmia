package com.example.rythemia.account;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PasswordHasherTest {

    // Few iterations keep the tests fast; the real count is PasswordHasher.DEFAULT_ITERATIONS
    private final PasswordHasher hasher = new PasswordHasher(1_000);

    @Test
    public void derive_matchesRfc6070TestVectors() {
        byte[] salt = "salt".getBytes(StandardCharsets.US_ASCII);
        assertEquals("0c60c80f961f0e71f3a9b524af6012062fe037a6",
                hex(PasswordHasher.derive("password".toCharArray(), salt, 1)));
        assertEquals("ea6c014dc72d6f8ccd1ed92ace1d41f0d8de8957",
                hex(PasswordHasher.derive("password".toCharArray(), salt, 2)));
    }

    @Test
    public void verify_acceptsTheSamePassword() {
        PasswordHasher.Hash hash = hasher.hash("correct horse".toCharArray());

        assertTrue(hasher.verify("correct horse".toCharArray(), hash));
    }

    @Test
    public void verify_rejectsADifferentPassword() {
        PasswordHasher.Hash hash = hasher.hash("correct horse".toCharArray());

        assertFalse(hasher.verify("correct horsE".toCharArray(), hash));
        assertFalse(hasher.verify("correct horse ".toCharArray(), hash));
    }

    @Test
    public void hash_neverStoresThePasswordAndSaltsEachHash() {
        PasswordHasher.Hash first = hasher.hash("same password".toCharArray());
        PasswordHasher.Hash second = hasher.hash("same password".toCharArray());

        assertEquals(16, first.salt.length);
        assertFalse(Arrays.equals(first.salt, second.salt));
        assertFalse(Arrays.equals(first.hash, second.hash));
        assertFalse(new String(first.hash, StandardCharsets.UTF_8).contains("same password"));
    }

    @Test
    public void hash_recordsTheIterationCountUsed() {
        PasswordHasher.Hash hash = hasher.hash("pw12345678".toCharArray());

        assertEquals(1_000, hash.iterations);
        assertArrayEquals(hash.hash,
                PasswordHasher.derive("pw12345678".toCharArray(), hash.salt, hash.iterations));
    }

    private static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) {
            out.append(String.format("%02x", b));
        }
        return out.toString();
    }
}
