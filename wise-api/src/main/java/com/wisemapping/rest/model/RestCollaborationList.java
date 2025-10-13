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


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class RestCollaborationList {

    private List<RestCollaboration> collaborations;
    private String message;

    public RestCollaborationList() {
        collaborations = new ArrayList<RestCollaboration>();
    }

    public int getCount() {
        return this.collaborations.size();
    }

    public void setCount(int count) {

    }

    public List<RestCollaboration> getCollaborations() {
        return collaborations;
    }

    public void addCollaboration(@NotNull RestCollaboration collaboration) {
        collaborations.add(collaboration);
    }

    public void setCollaborations(@NotNull List<RestCollaboration> collaborations) {
        this.collaborations = collaborations;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        // Sanitize message to plain text only - remove all HTML tags and links completely
        if (message != null && !message.trim().isEmpty()) {
            this.message = sanitizeToPlainText(message);
        } else {
            this.message = message;
        }
    }

    /**
     * Sanitizes message to plain text by removing all HTML tags and links.
     * Links (both HTML anchor tags and plain URLs) are completely removed.
     * 
     * @param content The content to sanitize
     * @return Plain text with all HTML and links removed
     */
    private String sanitizeToPlainText(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        // First, remove all <a> tags and their content completely
        String sanitized = content.replaceAll("<a[^>]*>.*?</a>", "");
        
        // Remove any remaining HTML tags but keep the text
        sanitized = Jsoup.clean(sanitized, Safelist.none());
        
        // Get plain text (this also decodes HTML entities)
        sanitized = Jsoup.parse(sanitized).text();
        
        // Remove URLs (http://, https://, ftp://, www., etc.)
        sanitized = sanitized.replaceAll("https?://\\S+", "");
        sanitized = sanitized.replaceAll("ftp://\\S+", "");
        sanitized = sanitized.replaceAll("www\\.\\S+", "");
        
        // Clean up multiple spaces and trim
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        
        return sanitized;
    }
}
