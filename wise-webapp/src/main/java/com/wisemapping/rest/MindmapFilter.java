/*
*    Copyright [2012] [wisemapping]
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

import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MindmapFilter {

    public static MindmapFilter ALL = new MindmapFilter("all") {
        @Override
        boolean accept(@NotNull Mindmap mindmap, @NotNull User user) {
            return true;
        }
    };
    public static MindmapFilter MY_MAPS = new MindmapFilter("my_maps") {
        @Override
        boolean accept(@NotNull Mindmap mindmap, @NotNull User user) {
            return mindmap.getCreator().identityEquality(user);
        }
    };
    public static MindmapFilter STARRED = new MindmapFilter("starred") {
        @Override
        boolean accept(@NotNull Mindmap mindmap, @NotNull User user) {
            return mindmap.isStarred(user);
        }
    };
    public static MindmapFilter SHARED_WITH_ME = new MindmapFilter("shared_with_me") {
        @Override
        boolean accept(@NotNull Mindmap mindmap, @NotNull User user) {
            return !MY_MAPS.accept(mindmap, user);
        }
    };
    public static MindmapFilter PUBLIC = new MindmapFilter("public") {
        @Override
        boolean accept(@NotNull Mindmap mindmap, @NotNull User user) {
            return mindmap.isPublic();
        }
    };

    protected String id;
    private static MindmapFilter[] values = {ALL, MY_MAPS, PUBLIC, STARRED, SHARED_WITH_ME};

    private MindmapFilter(@NotNull String id) {
        this.id = id;
    }

    static public MindmapFilter parse(@Nullable final String valueStr) {
        MindmapFilter result = valueStr == null ? ALL : null;
        for (MindmapFilter value : MindmapFilter.values) {
            if (value.id.equals(valueStr)) {
                result = value;
                break;
            }
        }
        if (result == null) {
            assert valueStr != null;
            result = new LabelFilter(valueStr);
        }
        return result;
    }

    abstract boolean accept(@NotNull Mindmap mindmap, @NotNull User user);

    private static final class LabelFilter extends MindmapFilter {

        private LabelFilter(@NotNull String id) {
            super(id);
        }

        @Override
        boolean accept(@NotNull Mindmap mindmap, @NotNull User user) {
            return mindmap.hasLabel(this.id);
        }
    }

}