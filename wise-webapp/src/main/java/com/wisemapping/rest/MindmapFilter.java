/*
*    Copyright [2011] [wisemapping]
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


package com.wisemapping.rest;

import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MindmapFilter {
    ALL("all") {
        @Override
        public boolean accept(@NotNull MindMap mindmap, @NotNull User user) {
            return true;
        }

    },
    MY_MAPS("my_maps") {
        @Override
        boolean accept(@NotNull MindMap mindmap, @NotNull User user) {
            return mindmap.getCreator().equals(user);
        }
    },
    STARRED("starred") {
        @Override
        boolean accept(@NotNull MindMap mindmap, @NotNull User user) {
            return mindmap.isStarred(user);
        }
    },
    SHARED_WITH_ME("shared_with_me") {
        @Override
        boolean accept(@NotNull MindMap mindmap, @NotNull User user) {
            return !MY_MAPS.accept(mindmap, user);
        }
    },
    PUBLIC("public") {
        @Override
        boolean accept(@NotNull MindMap mindmap, @NotNull User user) {
            return mindmap.isPublic();
        }
    };

    private String id;

    MindmapFilter(@NotNull String id) {
        this.id = id;
    }

    static public MindmapFilter parse(@Nullable String valueStr) {
        MindmapFilter result = ALL;
        final MindmapFilter[] values = MindmapFilter.values();
        for (MindmapFilter value : values) {
            if (value.id.equals(valueStr)) {
                result = value;
                break;
            }
        }
        return result;
    }

    abstract boolean accept(@NotNull MindMap mindmap, @NotNull User user);

}