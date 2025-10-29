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

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DefaultPasswordEncoderFactories {

    public static final String ENCODING_ID = "bcrypt";

    public static PasswordEncoder createDelegatingPasswordEncoder() {
        // Use a custom delegating encoder that handles legacy ENC: format
        return new WiseMappingPasswordEncoder();
    }

}

/**
 * Custom password encoder that handles both modern BCrypt and legacy ENC: password formats.
 * Database passwords can be in two formats:
 * - Legacy: ENC:6c69d1e41a95462be1ff01decc9c4d4022c6a082 (SHA-1 with ENC: prefix)
 * - Modern: {bcrypt}$2a$12$... (BCrypt with {bcrypt} prefix)
 */
class WiseMappingPasswordEncoder implements PasswordEncoder {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
    private final LegacyPasswordEncoder legacyEncoder = new LegacyPasswordEncoder();
    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder(12);
    
    @Override
    public String encode(CharSequence rawPassword) {
        // New passwords use BCrypt
        return "{bcrypt}" + bcryptEncoder.encode(rawPassword);
    }
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        
        // Check if it's a legacy ENC: password
        if (encodedPassword.startsWith(LegacyPasswordEncoder.ENC_PREFIX)) {
            return legacyEncoder.matches(rawPassword, encodedPassword);
        }
        
        // Check if it's a BCrypt password
        if (encodedPassword.startsWith("{bcrypt}")) {
            String hash = encodedPassword.substring(8); // Remove {bcrypt} prefix
            return bcryptEncoder.matches(rawPassword, hash);
        }
        
        // No recognized prefix - try legacy encoder as default
        logger.debug("Unknown password format (no ENC: or {{bcrypt}} prefix), trying legacy encoder");
        return legacyEncoder.matches(rawPassword, encodedPassword);
    }
}