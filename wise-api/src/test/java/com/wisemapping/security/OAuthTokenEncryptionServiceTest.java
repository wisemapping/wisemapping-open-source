package com.wisemapping.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class OAuthTokenEncryptionServiceTest {

    private OAuthTokenEncryptionService service;

    @BeforeEach
    void setUp() {
        service = new OAuthTokenEncryptionService();
        // Inject a fixed secret so the test is deterministic and self-contained.
        ReflectionTestUtils.setField(service, "secretKey", "test-secret-for-oauth-encryption-32ch");
    }

    /** New values are encrypted (carry the prefix) and decrypt back to the original. */
    @Test
    void encryptThenDecrypt_roundTripsProviderIdCorrectly() {
        String facebookId = "123456789012345";

        String encrypted = service.encrypt(facebookId);

        assertTrue(service.isEncrypted(encrypted),
                "Encrypted value should start with " + OAuthTokenEncryptionService.ENC_PREFIX);
        assertNotEquals(facebookId, encrypted,
                "Encrypted value must differ from plaintext");

        String decrypted = service.decrypt(encrypted);
        assertEquals(facebookId, decrypted,
                "Decrypting the encrypted value should recover the original provider ID");
    }

    /**
     * Legacy rows stored without the {enc} prefix must be returned unchanged by
     * decrypt(), so old accounts keep working without any data migration.
     */
    @Test
    void decrypt_legacyPlaintextValue_returnedUnchanged() {
        String legacyToken = "987654321098765";

        String result = service.decrypt(legacyToken);

        assertEquals(legacyToken, result,
                "A plaintext value without the {enc} prefix must pass through decrypt() unchanged");
    }

    /**
     * When no secret key is configured (empty string) the service acts as a no-op:
     * encrypt() returns the raw value and decrypt() returns the stored value as-is.
     * This preserves the original behaviour for deployments that have not opted in.
     */
    @Test
    void encryptAndDecrypt_whenDisabled_areNoOps() {
        ReflectionTestUtils.setField(service, "secretKey", "");

        String providerId = "111222333444555";

        assertFalse(service.isEnabled(), "Service should report disabled when secret is blank");
        assertEquals(providerId, service.encrypt(providerId),
                "encrypt() should be a no-op when disabled");
        assertEquals(providerId, service.decrypt(providerId),
                "decrypt() should be a no-op when disabled");
    }
}
