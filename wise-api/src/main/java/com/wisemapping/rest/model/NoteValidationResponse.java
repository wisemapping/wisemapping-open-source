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

package com.wisemapping.rest.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response class for note content validation.
 * Provides character count information and validation results for note content.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoteValidationResponse {

    private int rawLength;
    private int textLength;
    private boolean isHtml;
    private int remainingChars;
    private boolean isOverLimit;
    private double usagePercentage;

    public NoteValidationResponse() {
        // Default constructor for Jackson
    }

    public NoteValidationResponse(int rawLength, int textLength, boolean isHtml, 
                                 int remainingChars, boolean isOverLimit, double usagePercentage) {
        this.rawLength = rawLength;
        this.textLength = textLength;
        this.isHtml = isHtml;
        this.remainingChars = remainingChars;
        this.isOverLimit = isOverLimit;
        this.usagePercentage = usagePercentage;
    }

    /**
     * Gets the raw character length of the note content (including HTML tags).
     * This is the actual length stored in the database.
     */
    public int getRawLength() {
        return rawLength;
    }

    public void setRawLength(int rawLength) {
        this.rawLength = rawLength;
    }

    /**
     * Gets the text-only character length (HTML tags removed).
     * This represents the readable text content.
     */
    public int getTextLength() {
        return textLength;
    }

    public void setTextLength(int textLength) {
        this.textLength = textLength;
    }

    /**
     * Indicates whether the note content contains HTML markup.
     */
    public boolean isHtml() {
        return isHtml;
    }

    public void setHtml(boolean html) {
        isHtml = html;
    }

    /**
     * Gets the number of characters remaining before hitting the limit.
     */
    public int getRemainingChars() {
        return remainingChars;
    }

    public void setRemainingChars(int remainingChars) {
        this.remainingChars = remainingChars;
    }

    /**
     * Indicates whether the note content exceeds the maximum allowed length.
     */
    public boolean isOverLimit() {
        return isOverLimit;
    }

    public void setOverLimit(boolean overLimit) {
        isOverLimit = overLimit;
    }

    /**
     * Gets the percentage of the character limit that is currently being used.
     * Returns a value between 0.0 and 100.0+ (can exceed 100 if over limit).
     */
    public double getUsagePercentage() {
        return usagePercentage;
    }

    public void setUsagePercentage(double usagePercentage) {
        this.usagePercentage = usagePercentage;
    }

    /**
     * Gets a user-friendly status message for the character count.
     */
    public String getStatusMessage() {
        if (isOverLimit) {
            return String.format("Note exceeds limit by %d characters", -remainingChars);
        } else if (usagePercentage >= 90) {
            return String.format("Note is near limit (%d characters remaining)", remainingChars);
        } else {
            return String.format("%d characters remaining", remainingChars);
        }
    }

    /**
     * Gets a CSS class name for styling the character counter based on usage.
     */
    public String getStatusClass() {
        if (isOverLimit) {
            return "note-counter-over-limit";
        } else if (usagePercentage >= 90) {
            return "note-counter-warning";
        } else {
            return "note-counter-normal";
        }
    }
}
