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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Base64;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LegacyPasswordEncoder implements PasswordEncoder {
    final private static Logger logger = LogManager.getLogger();

    public static final String ENC_PREFIX = "ENC:";
    private final ShaPasswordEncoder sha1Encoder = new ShaPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        final String hash = sha1Encoder.encode(rawPassword.toString(), "");
        return ENC_PREFIX + hash;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        final String computedHash = encode(rawPassword);
        boolean matches = computedHash.equals(encodedPassword);
        
        if (!matches) {
            logger.debug("Legacy password match failed");
        }
        
        return matches;
    }
}

/**
 * Just copied to keep compatibility with Spring 3.
 */
class ShaPasswordEncoder {

    private final String algorithm;
    private final boolean encodeHashAsBase64;


    /**
     * The digest algorithm to use
     * Supports the named <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppA">
     * Message Digest Algorithms</a> in the Java environment.
     **/
    ShaPasswordEncoder() {

        this("SHA-1", false);
    }

    /**
     * Convenience constructor for specifying the algorithm and whether or not to enable base64 encoding
     *
     * @param algorithm
     * @param encodeHashAsBase64
     * @throws IllegalArgumentException if an unknown
     */
    private ShaPasswordEncoder(String algorithm, boolean encodeHashAsBase64) throws IllegalArgumentException {
        this.algorithm = algorithm;
        this.encodeHashAsBase64 = encodeHashAsBase64;
        getMessageDigest();
    }

    /**
     * Encodes the rawPass using a MessageDigest.
     * If a salt is specified it will be merged with the password before encoding.
     *
     * @param rawPass The plain text password
     * @param salt    The salt to sprinkle
     * @return Hex string of password digest (or base64 encoded string if encodeHashAsBase64 is enabled.
     */
    public String encode(String rawPass, Object salt) {
        String saltedPass = mergePasswordAndSalt(rawPass, salt, false);

        MessageDigest messageDigest = getMessageDigest();

        byte[] digest = messageDigest.digest(saltedPass.getBytes(StandardCharsets.UTF_8));

        if (getEncodeHashAsBase64()) {
            return Base64.getEncoder().encodeToString(digest);
        } else {
            return bytesToHex(digest);
        }
    }

    /**
     * Get a MessageDigest instance for the given algorithm.
     * Throws an IllegalArgumentException if <i>algorithm</i> is unknown
     *
     * @return MessageDigest instance
     * @throws IllegalArgumentException if NoSuchAlgorithmException is thrown
     */
    private final MessageDigest getMessageDigest() throws IllegalArgumentException {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm [" + algorithm + "]");
        }
    }

    //~ Methods ========================================================================================================

    private boolean getEncodeHashAsBase64() {
        return encodeHashAsBase64;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String mergePasswordAndSalt(String password, Object salt, boolean strict) {
        if (password == null) {
            password = "";
        }

        if (strict && (salt != null)) {
            if ((salt.toString().lastIndexOf("{") != -1)
                    || (salt.toString().lastIndexOf("}") != -1)) {
                throw new IllegalArgumentException("Cannot use { or } in salt.toString()");
            }
        }

        if ((salt == null) || "".equals(salt)) {
            return password;
        } else {
            return password + "{" + salt.toString() + "}";
        }
    }
}
