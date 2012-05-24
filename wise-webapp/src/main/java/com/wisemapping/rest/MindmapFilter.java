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
            return mindmap.getOwner().equals(user);
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