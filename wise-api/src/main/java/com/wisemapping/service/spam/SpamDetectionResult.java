/*
 *    Copyright [2022] [wisemapping]
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

package com.wisemapping.service.spam;

public class SpamDetectionResult {
    private final boolean isSpam;
    private final String reason;
    private final String details;

    public SpamDetectionResult(boolean isSpam, String reason, String details) {
        this.isSpam = isSpam;
        this.reason = reason;
        this.details = details;
    }

    public static SpamDetectionResult notSpam() {
        return new SpamDetectionResult(false, null, null);
    }

    public static SpamDetectionResult spam(String reason, String details) {
        return new SpamDetectionResult(true, reason, details);
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
}