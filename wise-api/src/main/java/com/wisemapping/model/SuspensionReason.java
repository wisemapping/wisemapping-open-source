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

package com.wisemapping.model;

public enum SuspensionReason {
    ABUSE("A"),
    TERMS_VIOLATION("T"),
    SECURITY_CONCERN("S"),
    MANUAL_REVIEW("M"),
    INACTIVITY("I"),
    OTHER("O");

    private final String code;

    SuspensionReason(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static SuspensionReason fromCode(String code) {
        for (SuspensionReason reason : values()) {
            if (reason.code.equals(code)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Invalid suspension reason code: " + code);
    }
}