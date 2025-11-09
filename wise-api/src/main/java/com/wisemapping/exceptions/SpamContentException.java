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

package com.wisemapping.exceptions;

import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
import jakarta.validation.constraints.NotNull;
import org.springframework.util.StringUtils;

public class SpamContentException extends ClientException {
    private static final String SPAM_CONTENT_NOT_ALLOWED = "SPAM_CONTENT_NOT_ALLOWED";
    private static final String SUPPORT_EMAIL = "support@wisemapping.com";

    private final String strategyName;
    private final String strategyDetails;

    public SpamContentException() {
        this((SpamStrategyType) null, null);
    }

    public SpamContentException(Mindmap mindmap) {
        this(mindmap != null ? mindmap.getSpamTypeCode() : null,
             mindmap != null ? mindmap.getSpamDescription() : null);
    }

    public SpamContentException(SpamStrategyType strategyType, String details) {
        super(buildDefaultMessage(strategyType, details), Severity.WARNING);
        this.strategyName = strategyType != null ? strategyType.getStrategyName() : "Unknown strategy";
        this.strategyDetails = normalizeDetails(details);
    }

    private static String buildDefaultMessage(SpamStrategyType strategyType, String details) {
        String readableStrategy = strategyType != null ? strategyType.getStrategyName() : "Unknown strategy";
        String detailText = normalizeDetails(details);
        return String.format(
                "Spam content detected in mindmap (%s). Details: %s. If you believe this is a mistake, please contact %s.",
                readableStrategy,
                detailText,
                SUPPORT_EMAIL
        );
    }

    private static String normalizeDetails(String details) {
        if (!StringUtils.hasText(details)) {
            return "No additional diagnostic information was recorded.";
        }
        return details.trim();
    }

    @NotNull
    @Override
    protected String getMsgBundleKey() {
        return SPAM_CONTENT_NOT_ALLOWED;
    }

    @Override
    protected Object[] getMsgBundleArgs() {
        return new Object[] {
                StringUtils.hasText(strategyName) ? strategyName : "Unknown strategy",
                strategyDetails,
                SUPPORT_EMAIL
        };
    }
}