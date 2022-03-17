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

package com.wisemapping.exceptions;


import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

public class InvalidMindmapException
        extends ClientException {
    private static final String EMPTY_MINDMAP = "MINDMAP_EMPTY_ERROR";
    private static final String INVALID_MINDMAP_FORMAT = "INVALID_MINDMAP_FORMAT";
    private static final String TOO_BIG_MINDMAP = "TOO_BIG_MINDMAP";

    private final String bundleKey;

    private InvalidMindmapException(@NotNull String bundleKey, @Nullable String xmlDoc) {
        super("Invalid mindmap format:" + xmlDoc, Severity.SEVERE);
        this.bundleKey = bundleKey;
    }

    static public InvalidMindmapException emptyMindmap() {
        return new InvalidMindmapException(EMPTY_MINDMAP, "<empty string>");
    }

    static public InvalidMindmapException invalidFormat(@Nullable String xmlDoc) {
        return new InvalidMindmapException(INVALID_MINDMAP_FORMAT, xmlDoc);
    }

    static public InvalidMindmapException tooBigMindnap(int numberOfTopics) {
        return new InvalidMindmapException(TOO_BIG_MINDMAP, "<too-big " + numberOfTopics + ">");
    }

    @NotNull
    @Override
    protected String getMsgBundleKey() {
        return bundleKey;
    }
}
