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

package com.wisemapping.rest.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RestAppConfigTest {

    @Test
    void testBuilder_UsesConfiguredJwtExpiration() {
        RestAppConfig config = new RestAppConfig.RestAppConfigBuilder()
                .setJwtExpirationMin(1440)
                .build();

        assertEquals(1440, config.getJwtExpirationMin());
    }

    @Test
    void testBuilder_DefaultsJwtExpirationToOneWeek() {
        RestAppConfig config = new RestAppConfig.RestAppConfigBuilder().build();

        assertEquals(10080, config.getJwtExpirationMin());
    }
}
