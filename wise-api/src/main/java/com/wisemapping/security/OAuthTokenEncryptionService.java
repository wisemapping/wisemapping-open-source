/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.wisemapping.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Service for encrypting and decrypting OAuth provider user IDs (stored in the
 * {@code oauth_token} database column).
 *
 * <h3>Backward-compatibility</h3>
 * <p>Encrypted values carry an <b>{@value #ENC_PREFIX}</b> prefix. Any value that
 * lacks this prefix is treated as legacy plaintext and returned unchanged by
 * {@link #decrypt}. This allows old rows and new rows to coexist in the same table
 * without a schema migration.
 *
 * <h3>Deterministic encryption</h3>
 * <p>AES-256-CBC is used with a <em>fixed</em> IV derived from the secret key.
 * The same plaintext therefore always produces the same ciphertext for a given key.
 * This is intentional: it lets JPQL {@code IN} queries match both the plaintext and
 * encrypted forms of a provider ID without requiring an extra hash column.
 *
 * <h3>Disabled mode</h3>
 * <p>When {@code app.oauth.token.secret} is empty (the default) the service acts as
 * a no-op: {@link #encrypt} returns the original value and {@link #decrypt} returns
 * the stored value unchanged. This preserves existing behaviour for deployments that
 * have not yet opted in to encryption.
 */
@Service
public class OAuthTokenEncryptionService {

    /** Prefix prepended to every encrypted value to distinguish it from plaintext. */
    public static final String ENC_PREFIX = "{enc}";

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final Logger logger = LoggerFactory.getLogger(OAuthTokenEncryptionService.class);

    @Value("${app.oauth.token.secret:}")
    private String secretKey;

    // Derived lazily and cached after first use (guarded by synchronized helpers).
    private volatile SecretKey aesKey;
    private volatile IvParameterSpec iv;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Encrypt a plaintext OAuth provider user ID.
     *
     * @param plaintext the raw provider ID (e.g. a Google subject or Facebook user ID)
     * @return {@code {enc}<base64-ciphertext>} when encryption is enabled,
     *         or {@code plaintext} unchanged when no secret key is configured
     */
    @Nullable
    public String encrypt(@Nullable String plaintext) {
        if (plaintext == null) return null;
        if (!isEnabled()) return plaintext;
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, resolveKey(), resolveIv());
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return ENC_PREFIX + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            logger.error("Failed to encrypt OAuth token", e);
            throw new IllegalStateException("OAuth token encryption failed", e);
        }
    }

    /**
     * Decrypt a value previously produced by {@link #encrypt}.
     *
     * <p>If the value does not start with the {@value #ENC_PREFIX} prefix it is
     * returned as-is (legacy plaintext / already-decrypted value).
     *
     * @param value the stored column value
     * @return the original provider ID, or {@code value} unchanged for plaintext rows
     */
    @Nullable
    public String decrypt(@Nullable String value) {
        if (value == null || !value.startsWith(ENC_PREFIX)) return value;
        if (!isEnabled()) {
            logger.warn("Encountered an encrypted OAuth token but no secret key is configured — returning raw value");
            return value;
        }
        try {
            byte[] cipherBytes = Base64.getDecoder().decode(value.substring(ENC_PREFIX.length()));
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, resolveKey(), resolveIv());
            return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Failed to decrypt OAuth token", e);
            throw new IllegalStateException("OAuth token decryption failed", e);
        }
    }

    /**
     * Returns {@code true} when the value carries the encrypted prefix.
     */
    public boolean isEncrypted(@Nullable String value) {
        return value != null && value.startsWith(ENC_PREFIX);
    }

    /**
     * Returns {@code true} when a non-empty secret key has been configured.
     */
    public boolean isEnabled() {
        return secretKey != null && !secretKey.isBlank();
    }

    // -------------------------------------------------------------------------
    // Key / IV derivation
    // -------------------------------------------------------------------------

    private SecretKey resolveKey() throws Exception {
        if (aesKey == null) {
            synchronized (this) {
                if (aesKey == null) {
                    aesKey = deriveKey(secretKey);
                }
            }
        }
        return aesKey;
    }

    private IvParameterSpec resolveIv() throws Exception {
        if (iv == null) {
            synchronized (this) {
                if (iv == null) {
                    iv = deriveIv(secretKey);
                }
            }
        }
        return iv;
    }

    /**
     * Derive a 256-bit AES key from the secret string via SHA-256.
     */
    private static SecretKey deriveKey(@NotNull String secret) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(secret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Derive a deterministic 128-bit IV from the secret via MD5.
     *
     * <p>The IV is intentionally fixed for a given key so that encryption is
     * deterministic (same plaintext → same ciphertext). This is a deliberate
     * trade-off that makes JPQL equality and IN-list lookups possible without
     * storing a separate hash column.
     */
    private static IvParameterSpec deriveIv(@NotNull String secret) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] ivBytes = md5.digest(secret.getBytes(StandardCharsets.UTF_8));
        return new IvParameterSpec(ivBytes);
    }
}
