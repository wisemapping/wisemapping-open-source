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

class RestCollaborationListTest {

    @Test
    void testMessageSanitization_RemovesHtmlTags() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with HTML tags
        collabList.setMessage("<p>Hello</p> <b>World</b>");
        assertEquals("Hello World", collabList.getMessage());
    }

    @Test
    void testMessageSanitization_RemovesLinksCompletely() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with links - both the tag AND the content should be removed
        collabList.setMessage("Check this <a href='https://evil.com'>link</a> out");
        assertEquals("Check this out", collabList.getMessage());
    }

    @Test
    void testMessageSanitization_RemovesPlainUrls() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with plain URLs
        collabList.setMessage("Visit https://example.com for more info");
        assertEquals("Visit for more info", collabList.getMessage());
        
        collabList.setMessage("Check http://test.com and www.example.org");
        assertEquals("Check and", collabList.getMessage());
    }

    @Test
    void testMessageSanitization_RemovesScriptTags() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with script tags
        collabList.setMessage("Hello <script>alert('xss')</script>World");
        assertEquals("Hello World", collabList.getMessage());
    }

    @Test
    void testMessageSanitization_PreservesPlainText() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with plain text
        collabList.setMessage("Just plain text message");
        assertEquals("Just plain text message", collabList.getMessage());
    }

    @Test
    void testMessageSanitization_HandlesEmptyString() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with empty string
        collabList.setMessage("");
        assertEquals("", collabList.getMessage());
    }

    @Test
    void testMessageSanitization_HandlesNull() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with null
        collabList.setMessage(null);
        assertNull(collabList.getMessage());
    }

    @Test
    void testMessageSanitization_HandlesWhitespace() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with only whitespace
        collabList.setMessage("   ");
        assertEquals("   ", collabList.getMessage());
    }

    @Test
    void testMessageSanitization_RemovesComplexHtml() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with complex HTML - link and its content should be removed
        // Note: Jsoup doesn't add spaces between block elements like h1 and p
        collabList.setMessage("<div><h1>Title</h1> <p>Paragraph with <em>emphasis</em> and <a href='http://example.com'>link</a></p></div>");
        assertEquals("Title Paragraph with emphasis and", collabList.getMessage());
    }

    @Test
    void testMessageSanitization_RemovesMultipleLinks() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with multiple links
        collabList.setMessage("First <a href='http://one.com'>link</a> and second <a href='http://two.com'>link</a>");
        assertEquals("First and second", collabList.getMessage());
    }

    @Test
    void testMessageSanitization_RemovesFtpUrls() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with FTP URLs
        collabList.setMessage("Download from ftp://files.example.com/file.zip please");
        assertEquals("Download from please", collabList.getMessage());
    }

    @Test
    void testMessageSanitization_HandlesHtmlEntities() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with HTML entities
        collabList.setMessage("&lt;div&gt;Content&lt;/div&gt; &amp; more");
        assertEquals("<div>Content</div> & more", collabList.getMessage());
    }

    @Test
    void testMessageSanitization_RemovesInlineStyles() {
        RestCollaborationList collabList = new RestCollaborationList();
        
        // Test with inline styles
        collabList.setMessage("<p style='color:red'>Styled text</p>");
        assertEquals("Styled text", collabList.getMessage());
    }
}

