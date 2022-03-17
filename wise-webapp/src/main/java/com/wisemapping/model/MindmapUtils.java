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
package com.wisemapping.model;


import com.wisemapping.exceptions.InvalidMindmapException;
import org.jetbrains.annotations.Nullable;

abstract public class MindmapUtils {

    private static final int MAX_SUPPORTED_NODES = 4000;

    public static void verifyMindmap(@Nullable String xmlDoc) throws InvalidMindmapException {
        if (xmlDoc == null || xmlDoc.trim().isEmpty()) {
            // Perform basic structure validation. Must have a map node and
            throw InvalidMindmapException.emptyMindmap();
        }

        // Perform basic structure validation without parsing the XML.
        if (!xmlDoc.trim().endsWith("</map>") || !xmlDoc.trim().startsWith("<map")) {
            throw InvalidMindmapException.invalidFormat(xmlDoc);
        }

        // Validate that the number of nodes is not bigger 500 nodes.
        int numberOfTopics = xmlDoc.split("<topic").length;
        if (numberOfTopics > MAX_SUPPORTED_NODES) {
            throw InvalidMindmapException.tooBigMindnap(numberOfTopics);
        }
    }
}
