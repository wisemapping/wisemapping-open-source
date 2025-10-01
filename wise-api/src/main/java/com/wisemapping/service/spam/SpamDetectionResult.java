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

package com.wisemapping.service.spam;

import com.wisemapping.model.SpamStrategyType;

public class SpamDetectionResult {
    private final boolean isSpam;
    private final String reason;
    private final String details;
    private final SpamStrategyType strategyName;

    public SpamDetectionResult(boolean isSpam, String reason, String details) {
        this.isSpam = isSpam;
        this.reason = reason;
        this.details = details;
        this.strategyName = null;
    }

    public SpamDetectionResult(boolean isSpam, String reason, String details, SpamStrategyType strategyName) {
        this.isSpam = isSpam;
        this.reason = reason;
        this.details = details;
        this.strategyName = strategyName;
    }

    public static SpamDetectionResult notSpam() {
        return new SpamDetectionResult(false, null, null, null);
    }


    public static SpamDetectionResult spam(String reason, String details, SpamStrategyType type) {
        return new SpamDetectionResult(true, reason, details, type);
    }

    public boolean isSpam() {
        return isSpam;
    }

    public String getReason() {
        return reason;
    }

    public String getDetails() {
        return details;
    }

    public SpamStrategyType getStrategyType() {
        return strategyName;
    }
}