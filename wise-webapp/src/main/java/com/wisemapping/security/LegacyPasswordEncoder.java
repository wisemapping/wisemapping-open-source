/*
 *    Copyright [2015] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.security;

import org.apache.log4j.Logger;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SuppressWarnings("deprecation")
public class LegacyPasswordEncoder implements PasswordEncoder {
    final private static Logger logger = Logger.getLogger("com.wisemapping.security.LegacyPasswordEncoder");

    private static final String ENC_PREFIX = "ENC:";
    private static final PasswordEncoder sha1Encoder = new MessageDigestPasswordEncoder("SHA-1");

    @Override
    public String encode(CharSequence rawPassword) {

        logger.info("LegacyPasswordEncoder encode executed.");
        return ENC_PREFIX + sha1Encoder.encode(rawPassword);

    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {

        final String encode = encode(rawPassword);
        logger.info("LegacyPasswordEncoder encode executed ->" + encode + ":" + encodedPassword);
        return encode.equals(encodedPassword);
    }
}
